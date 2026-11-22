package com.bit_rhythm.content_service.search;

import com.bit_rhythm.content_service.album.Album;
import com.bit_rhythm.content_service.album.AlbumRepository;
import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.artist.ArtistRepository;
import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.common.Paging;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.search.dto.SearchHit;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Search over PUBLISHED content: tracks (FULLTEXT on title), albums (title prefix) and artists (name prefix). */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_QUERY_LENGTH = 100;
    /** Bounds the work of one FULLTEXT query: extra words add nothing for a title search. */
    private static final int MAX_WORDS = 8;

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final StorageClient storageClient;
    private final ContentProperties properties;

    /** Keyset pagination by id (newest first); cursor = id of the last hit received. */
    @Transactional(readOnly = true)
    public CursorPage<SearchHit> search(String q, SearchType type, Long cursor, int size) {

        String text = normalize(q);
        if (text.length() < MIN_QUERY_LENGTH) {
            return empty();
        }

        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());
        long start = Paging.startCursor(cursor);
        Pageable pageable = PageRequest.ofSize(limit + 1);

        return switch (type == null ? SearchType.TRACK : type) {
            case TRACK -> searchTracks(text, start, limit);
            case ALBUM -> {
                List<Album> rows = albumRepository.searchPublishedByTitlePrefix(escapeLike(text), start, pageable);
                yield page(rows, limit, Album::getId, this::albumHits);
            }
            case ARTIST -> {
                List<Artist> rows = artistRepository.searchByNamePrefix(escapeLike(text), start, pageable);
                yield page(rows, limit, Artist::getId, this::artistHits);
            }
        };
    }

    // ---------------------------------------------------------------------------------------------------------------

    private CursorPage<SearchHit> searchTracks(String text, long start, int limit) {

        String booleanQuery = toBooleanQuery(text);
        if (booleanQuery.isEmpty()) {
            return empty();   // nothing but operators was typed
        }

        List<Track> rows = trackRepository.searchPublished(booleanQuery, start, limit + 1);
        return page(rows, limit, Track::getId, this::trackHits);
    }

    /** Page of hits from size + 1 rows: the extra row only says there is a next page and is never returned. */
    private <T> CursorPage<SearchHit> page(List<T> rows, int limit, Function<T, Long> idOf,
                                           Function<List<T>, List<SearchHit>> toHits) {
        boolean hasMore = rows.size() > limit;
        List<T> pageRows = hasMore ? rows.subList(0, limit) : rows;
        Long nextCursor = hasMore ? idOf.apply(pageRows.getLast()) : null;
        return new CursorPage<>(pageRows.isEmpty() ? List.of() : toHits.apply(pageRows), nextCursor);
    }

    private List<SearchHit> trackHits(List<Track> tracks) {
        Map<Long, String> artistNames = artistNames(tracks.stream().map(Track::getArtistId));

        // one query for the albums of the page: a track without its own cover falls back to the album cover
        List<Long> albumIds = tracks.stream().map(Track::getAlbumId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Album> albums = albumIds.isEmpty() ? Map.of() : albumRepository.findByIdIn(albumIds).stream()
                .collect(Collectors.toMap(Album::getId, Function.identity(), (a, b) -> a));

        return tracks.stream().map(track -> {
            Album album = track.getAlbumId() == null ? null : albums.get(track.getAlbumId());
            String coverKey = track.getCoverKey() != null ? track.getCoverKey() : (album == null ? null : album.getCoverKey());
            return SearchHit.track(track, artistNames.get(track.getArtistId()), coverUrl(coverKey));
        }).toList();
    }

    private List<SearchHit> albumHits(List<Album> albums) {
        Map<Long, String> artistNames = artistNames(albums.stream().map(Album::getArtistId));
        return albums.stream()
                .map(album -> SearchHit.album(album, artistNames.get(album.getArtistId()), coverUrl(album.getCoverKey())))
                .toList();
    }

    private List<SearchHit> artistHits(List<Artist> artists) {
        return artists.stream()
                .map(artist -> SearchHit.artist(artist, coverUrl(artist.getImageKey())))
                .toList();
    }

    /** ONE query for the artist names of a whole page. */
    private Map<Long, String> artistNames(java.util.stream.Stream<Long> artistIds) {
        List<Long> ids = artistIds.filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return artistRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Artist::getId, Artist::getName, (a, b) -> a));
    }

    private String coverUrl(String key) {
        return key == null ? null : storageClient.publicUrl(properties.cover().bucket(), key);
    }

    private static CursorPage<SearchHit> empty() {
        return new CursorPage<>(List.of(), null);
    }

    // ---------------------------------------------------------------------------------------------------------------

    /** Trim, NFC-normalize (Vietnamese diacritics typed as combining marks must match the stored form), cap the length. */
    private static String normalize(String q) {
        if (q == null) {
            return "";
        }
        String text = Normalizer.normalize(q.trim(), Normalizer.Form.NFC);
        return text.length() > MAX_QUERY_LENGTH ? text.substring(0, MAX_QUERY_LENGTH).trim() : text;
    }

    /**
     * "Hello, World!" -> "+hello* +world*". Everything that is not a letter, mark or digit is dropped, which removes every
     * boolean-mode operator the user could type (+ - ~ < > ( ) " @ *), so the input can never change the structure of the query.
     */
    static String toBooleanQuery(String text) {
        return Arrays.stream(text.split("[^\\p{L}\\p{M}\\p{N}]+"))
                .filter(word -> !word.isEmpty())
                .limit(MAX_WORDS)
                .map(word -> "+" + word + "*")
                .collect(Collectors.joining(" "));
    }

    /** Escape for LIKE ... ESCAPE '!': the user's '!', '%' and '_' are literal characters, not wildcards. */
    static String escapeLike(String text) {
        return text.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}