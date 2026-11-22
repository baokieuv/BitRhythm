package com.bit_rhythm.content_service.studio;

import java.util.Optional;

/**
 * Reads the REAL duration and bitrate of an uploaded audio object (never trust numbers sent by the client).
 * The library is an implementation detail: see JaudiotaggerAudioMetadataReader.
 */
public interface AudioMetadataReader {

    /**
     * @param extension file extension without the dot ("mp3", "flac", ...): some libraries pick the parser from it
     * @return empty when the object is not valid audio; a storage/network failure is thrown, NOT reported as "invalid audio"
     */
    Optional<AudioInfo> read(String bucket, String key, String extension);

    record AudioInfo(int durationSeconds, int bitrateKbps) {
    }
}