package com.bit_rhythm.content_service.discovery;

import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.history.PlayHistory;
import com.bit_rhythm.content_service.history.PlayHistoryRepository;
import com.bit_rhythm.content_service.library.TrackLike;
import com.bit_rhythm.content_service.library.TrackLikeRepository;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import com.bit_rhythm.content_service.track.TrackService;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import com.bit_rhythm.core.common.CommonEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Simple, explainable recommendations: no machine learning.
 *
 * Shared results (the popular list, the trending set) are cached as lists of track IDS only; the viewer's own view (liked flag,
 * covers, what is still PUBLISHED) is built after reading, never cached (design doc, section 15).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoveryService {

    private static final int NOT_DELETED = CommonEnums.DeletedStatus.NOT_DELETED.getCode();

    private static final String POPULAR_KEY = "discover:popular";
    private static final Duration POPULAR_TTL = Duration.ofMinutes(5);
    /** The cached popular list is this long; a request for less takes a prefix of it. */
    private static final int POPULAR_CACHE_SIZE = 100;

    private static final int SEED_LIKES = 30;
    private static final int SEED_PLAYS = 30;
    private static final int MAX_CANDIDATES = 200;

    private final TrackRepository trackRepository;
    private final TrackLikeRepository trackLikeRepository;
    private final PlayHistoryRepository playHistoryRepository;
    private final TrackService trackService;
    private final StringRedisTemplate redis;
    private final ContentProperties properties;

    /** The most played PUBLISHED tracks of all time. The id list is cached in Redis for a few minutes. */
    public List<TrackResponse> popular(int size, Long viewerId) {
        int limit = clamp(size);
        List<Long> ids = popularIds().stream().limit(limit).toList();
        return trackService.toResponses(loadOrdered(ids), viewerId);
    }

    /**
     * What is rising now: TrendingJob keeps the sorted set "trending:7d" (score = plays of the last 7 days). Read the top of it,
     * load the tracks in one query and keep the order of the set. Falls back to popular() when the set is empty, unreadable, or
     * nothing in it is visible any more.
     */
    public List<TrackResponse> trending(int size, Long viewerId) {
        int limit = clamp(size);

        List<Long> ids = trendingIds(limit * 2);   // a few extra: some may have been unpublished since the job ran
        if (!ids.isEmpty()) {
            List<Track> tracks = loadOrdered(ids).stream().limit(limit).toList();
            if (!tracks.isEmpty()) {
                return trackService.toResponses(tracks, viewerId);
            }
        }
        return popular(size, viewerId);
    }

    /**
     * "Because you liked": the genres and artists of the user's last liked and played tracks, then the most played PUBLISHED tracks
     * of those genres and artists, minus what the user already liked. A new user (no data) gets popular(); a short result is
     * topped up from the popular list.
     */
    public List<TrackResponse> forYou(Long userId, int size) {
        int limit = clamp(size);

        List<Long> seedIds = new ArrayList<>();
        for (TrackLike like : trackLikeRepository.findByUserIdAndIdLessThanOrderByIdDesc(
                userId, Long.MAX_VALUE, PageRequest.ofSize(SEED_LIKES))) {
            seedIds.add(like.getTrackId());
        }
        for (PlayHistory played : playHistoryRepository.findByUserIdOrderByPlayedAtDesc(userId, PageRequest.ofSize(SEED_PLAYS))) {
            seedIds.add(played.getTrackId());
        }
        if (seedIds.isEmpty()) {
            return popular(size, userId);
        }

        List<Track> seeds = trackRepository.findByIdInAndIsDeleted(seedIds.stream().distinct().toList(), NOT_DELETED);
        Set<Long> genreIds = seeds.stream().map(Track::getGenreId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> artistIds = seeds.stream().map(Track::getArtistId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (genreIds.isEmpty() && artistIds.isEmpty()) {
            return popular(size, userId);
        }

        List<Track> candidates = trackRepository.findRecommendationCandidates(
                ContentStatus.PUBLISHED, orSentinel(genreIds), orSentinel(artistIds),
                PageRequest.ofSize(Math.min(limit * 4, MAX_CANDIDATES)));

        List<Long> popularIds = popularIds();

        // ONE query for "already liked" over every id we may return
        Set<Long> toCheck = candidates.stream().map(Track::getId).collect(Collectors.toCollection(HashSet::new));
        toCheck.addAll(popularIds);
        Set<Long> liked = likedAmong(userId, toCheck);

        List<Track> picked = candidates.stream()
                .filter(track -> !liked.contains(track.getId()))
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));

        if (picked.size() < limit) {
            Set<Long> have = picked.stream().map(Track::getId).collect(Collectors.toSet());
            List<Long> fillIds = popularIds.stream()
                    .filter(id -> !liked.contains(id) && !have.contains(id))
                    .limit(limit - picked.size())
                    .toList();
            picked.addAll(loadOrdered(fillIds));
        }

        return trackService.toResponses(picked, userId);
    }

    // ---------------------------------------------------------------------------------------------------------------

    /** Ids of the most played PUBLISHED tracks (cached). Redis trouble only costs the cache, never the answer. */
    private List<Long> popularIds() {
        try {
            String cached = redis.opsForValue().get(POPULAR_KEY);
            if (cached != null) {
                return parseIds(cached);
            }
        } catch (Exception e) {
            log.warn("popular cache unreadable: {}", e.toString());
        }

        List<Long> ids = trackRepository
                .findByStatusOrderByPlayCountDescIdDesc(ContentStatus.PUBLISHED, PageRequest.of(0, POPULAR_CACHE_SIZE))
                .stream().map(Track::getId).toList();

        try {
            redis.opsForValue().set(POPULAR_KEY, ids.stream().map(String::valueOf).collect(Collectors.joining(",")), POPULAR_TTL);
        } catch (Exception e) {
            log.warn("popular cache not written: {}", e.toString());
        }
        return ids;
    }

    /** Top of the trending sorted set, highest score first; empty when the set is missing or Redis fails. */
    private List<Long> trendingIds(int count) {
        try {
            Set<String> top = redis.opsForZSet().reverseRange(TrendingJob.KEY, 0, count - 1L);
            if (top == null) {
                return List.of();
            }
            return top.stream().map(Long::valueOf).toList();
        } catch (Exception e) {
            log.warn("trending set unreadable: {}", e.toString());
            return List.of();
        }
    }

    /** ONE query for the tracks; keeps the order of ids; leaves out what is not PUBLISHED (the id list may be a few minutes old). */
    private List<Track> loadOrdered(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Track> byId = trackRepository.findByIdInAndIsDeleted(ids, NOT_DELETED).stream()
                .collect(Collectors.toMap(Track::getId, Function.identity(), (a, b) -> a));

        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .filter(track -> track.getStatus() == ContentStatus.PUBLISHED)
                .toList();
    }

    private Set<Long> likedAmong(Long userId, Set<Long> trackIds) {
        if (trackIds.isEmpty()) {
            return Set.of();
        }
        return trackLikeRepository.findByUserIdAndTrackIdIn(userId, trackIds).stream()
                .map(TrackLike::getTrackId)
                .collect(Collectors.toSet());
    }

    private int clamp(int size) {
        return Math.max(1, Math.min(size, properties.pagination().maxPageSize()));
    }

    private static List<Long> parseIds(String csv) {
        if (csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")).filter(s -> !s.isBlank()).map(Long::valueOf).toList();
    }

    /** An empty collection in "IN (...)" is not portable: pass an id that matches nothing instead. */
    private static Set<Long> orSentinel(Set<Long> ids) {
        return ids.isEmpty() ? new LinkedHashSet<>(List.of(-1L)) : ids;
    }
}