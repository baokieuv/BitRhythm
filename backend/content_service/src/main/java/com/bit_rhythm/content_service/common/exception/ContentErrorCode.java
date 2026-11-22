package com.bit_rhythm.content_service.common.exception;

import com.bit_rhythm.core.exception.ApiResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Error codes of content-service. core's GlobalExceptionHandler turns an ApiException carrying one of them into the standard
 * ApiResponseFormat. The Lombok getters implement ApiResponseCode (getCode, getMessage, getHttpStatus).
 * Hint: for something a user is not allowed to see (a draft of somebody else), answer *_NOT_FOUND, not FORBIDDEN.
 */
@Getter
@RequiredArgsConstructor
public enum ContentErrorCode implements ApiResponseCode {

    GENRE_NOT_FOUND(false, "CONTENT0001", "Genre not found", HttpStatus.NOT_FOUND),
    ARTIST_NOT_FOUND(false, "CONTENT0002", "Artist not found", HttpStatus.NOT_FOUND),
    ALBUM_NOT_FOUND(false, "CONTENT0003", "Album not found", HttpStatus.NOT_FOUND),
    TRACK_NOT_FOUND(false, "CONTENT0004", "Track not found", HttpStatus.NOT_FOUND),
    PLAYLIST_NOT_FOUND(false, "CONTENT0005", "Playlist not found", HttpStatus.NOT_FOUND),
    REPORT_NOT_FOUND(false, "CONTENT0006", "Report not found", HttpStatus.NOT_FOUND),

    TAKEN_DOWN(false, "CONTENT0007", "This content was removed by a moderator", HttpStatus.FORBIDDEN),
    NOT_OWNER(false, "CONTENT0008", "You are not the owner of this content", HttpStatus.FORBIDDEN),
    ARTIST_PROFILE_REQUIRED(false, "CONTENT0009", "Create your artist profile first", HttpStatus.CONFLICT),

    AUDIO_TYPE_NOT_ALLOWED(false, "CONTENT0010", "This audio format is not supported", HttpStatus.BAD_REQUEST),
    AUDIO_TOO_LARGE(false, "CONTENT0011", "The audio file is too large", HttpStatus.PAYLOAD_TOO_LARGE),
    AUDIO_NOT_UPLOADED(false, "CONTENT0012", "No uploaded audio was found for this track", HttpStatus.UNPROCESSABLE_ENTITY),
    AUDIO_NOT_READY(false, "CONTENT0013", "The track has no audio yet, it cannot be published", HttpStatus.UNPROCESSABLE_ENTITY),
    COVER_TYPE_NOT_ALLOWED(false, "CONTENT0014", "Cover must be a JPEG, PNG or WebP image", HttpStatus.BAD_REQUEST),
    COVER_TOO_LARGE(false, "CONTENT0015", "The cover image is too large", HttpStatus.PAYLOAD_TOO_LARGE),
    COVER_NOT_UPLOADED(false, "CONTENT0016", "No uploaded cover was found for this key", HttpStatus.UNPROCESSABLE_ENTITY),

    PLAYLIST_FULL(false, "CONTENT0017", "This playlist has reached its maximum number of tracks", HttpStatus.CONFLICT),
    TRACK_ALREADY_IN_PLAYLIST(false, "CONTENT0018", "The track is already in this playlist", HttpStatus.CONFLICT),
    INVALID_TRACK_ORDER(false, "CONTENT0019", "The new order must contain exactly the tracks of the playlist", HttpStatus.BAD_REQUEST),

    REPORT_ALREADY_OPEN(false, "CONTENT0020", "You already reported this content", HttpStatus.CONFLICT),
    REPORT_ALREADY_CLOSED(false, "CONTENT0021", "This report is already closed", HttpStatus.CONFLICT),

    GENRE_ALREADY_EXISTS(false, "CONTENT0022", "This genre already exists", HttpStatus.CONFLICT),
    PLAYLIST_LIMIT_REACHED(false, "CONTENT0023", "You have reached the maximum number of playlists per user", HttpStatus.CONFLICT),
    CANNOT_REPORT_OWN_CONTENT(false, "CONTENT0024", "You cannot report your own content", HttpStatus.BAD_REQUEST),
    INVALID_LYRICS(false, "CONTENT0025", "The lyrics are invalid", HttpStatus.BAD_REQUEST),;

    private final boolean success;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
