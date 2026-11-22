package com.bit_rhythm.content_service.lyrics;

import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.artist.ArtistService;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.lyrics.dto.CreditDto;
import com.bit_rhythm.content_service.lyrics.dto.LyricsRequest;
import com.bit_rhythm.content_service.lyrics.dto.LyricsResponse;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import com.bit_rhythm.content_service.track.TrackService;
import com.bit_rhythm.core.common.CommonEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.regex.Pattern;

/** Lyrics (plain text and time-synced LRC) and credits of a track. */
@Slf4j
@Service
@RequiredArgsConstructor
public class LyricsService {

    private static final int NOT_DELETED = CommonEnums.DeletedStatus.NOT_DELETED.getCode();

    private static final int MAX_PLAIN_CHARS = 20_000;
    private static final int MAX_SYNCED_CHARS = 40_000;
    private static final int MAX_CREDITS = 50;
    private static final int MAX_CREDIT_FIELD_CHARS = 200;

    /** A synced line starts with a timestamp "[mm:ss" ("[01:23.45]", "[1:23]"), or is an LRC id tag like "[ar:Name]", "[offset:500]". */
    private static final Pattern LRC_LINE = Pattern.compile("^\\[(\\d{1,3}:\\d{2}|[A-Za-z]{2,}:).*");

    private final TrackRepository trackRepository;
    private final TrackService trackService;
    private final ArtistService artistService;
    private final LyricsRepository lyricsRepository;
    private final CreditRepository creditRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * Lyrics and credits of a track, with the same visibility as the track itself (TRACK_NOT_FOUND for a draft of somebody else).
     * A track without lyrics answers an EMPTY response (null texts, no credits), not a 404.
     */
    public LyricsResponse get(Long trackId, Long viewerId) {

        trackService.findVisible(trackId, viewerId);

        return lyricsRepository.findByTrackId(trackId)
                .map(lyrics -> LyricsResponse.from(lyrics, creditRepository.findByTrackIdOrderByIdAsc(trackId)))
                .orElseGet(() -> {
                    // credits can exist without lyrics text
                    List<TrackCredit> credits = creditRepository.findByTrackIdOrderByIdAsc(trackId);
                    return credits.isEmpty() ? LyricsResponse.empty() : LyricsResponse.from(new TrackLyrics(), credits);
                });
    }

    /**
     * Replace the lyrics and credits of a track of the logged-in artist. The track must belong to the artist of userId.
     * NOT @Transactional on purpose (design doc, section 12): the first save of a track can race on the unique track_id of the
     * lyrics row, so the write runs in a TransactionTemplate and, if it loses that race, is run once more (now it is an update).
     */
    public LyricsResponse save(Long userId, Long trackId, LyricsRequest request) {

        Artist artist = artistService.requireOwnedBy(userId);

        Track track = trackRepository.findByIdAndIsDeleted(trackId, NOT_DELETED)
                .filter(t -> t.getArtistId().equals(artist.getId()))
                .orElseThrow(() -> new ContentException(ContentErrorCode.TRACK_NOT_FOUND));

        String plainText = blankToNull(request.plainText());
        String syncedLrc = blankToNull(request.syncedLrc());
        validate(plainText, syncedLrc, request.credits());

        try {
            return transactionTemplate.execute(status -> write(track.getId(), plainText, syncedLrc, request));
        } catch (DataIntegrityViolationException e) {
            log.debug("lyrics insert raced for track {}, retrying as update", trackId);
            return transactionTemplate.execute(status -> write(track.getId(), plainText, syncedLrc, request));
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    /** ONE transaction: upsert the lyrics row, replace the credits. */
    private LyricsResponse write(Long trackId, String plainText, String syncedLrc, LyricsRequest request) {

        TrackLyrics lyrics = lyricsRepository.findByTrackId(trackId).orElseGet(() -> new TrackLyrics(trackId));
        lyrics.update(plainText, syncedLrc);
        lyricsRepository.saveAndFlush(lyrics);   // a duplicate from a concurrent first save fails HERE

        creditRepository.deleteAllOfTrack(trackId);

        List<TrackCredit> credits = request.credits() == null ? List.of() : request.credits().stream()
                .map(c -> new TrackCredit(trackId, c.role().trim(), c.name().trim()))
                .toList();
        List<TrackCredit> saved = creditRepository.saveAll(credits);

        return LyricsResponse.from(lyrics, saved);
    }

    /** Loose checks only: refuse obvious garbage and oversized input, do not try to parse every dialect of LRC. */
    private void validate(String plainText, String syncedLrc, List<? extends CreditDto> credits) {

        if (plainText != null && plainText.length() > MAX_PLAIN_CHARS) {
            throw new ContentException(ContentErrorCode.INVALID_LYRICS, "plain text too long");
        }

        if (syncedLrc != null) {
            if (syncedLrc.length() > MAX_SYNCED_CHARS) {
                throw new ContentException(ContentErrorCode.INVALID_LYRICS, "synced lyrics too long");
            }
            for (String line : syncedLrc.split("\\R")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !LRC_LINE.matcher(trimmed).matches()) {
                    throw new ContentException(ContentErrorCode.INVALID_LYRICS, "line is not LRC: " + abbreviate(trimmed));
                }
            }
        }

        if (credits != null) {
            if (credits.size() > MAX_CREDITS) {
                throw new ContentException(ContentErrorCode.INVALID_LYRICS, "too many credits");
            }
            for (CreditDto c : credits) {
                if (isBlank(c.role()) || isBlank(c.name())
                        || c.role().trim().length() > MAX_CREDIT_FIELD_CHARS
                        || c.name().trim().length() > MAX_CREDIT_FIELD_CHARS) {
                    throw new ContentException(ContentErrorCode.INVALID_LYRICS, "credit needs a role and a name");
                }
            }
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String abbreviate(String s) {
        return s.length() <= 40 ? s : s.substring(0, 40) + "...";
    }
}