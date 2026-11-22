package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.content_service.storage.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JaudiotaggerAudioMetadataReader implements AudioMetadataReader
{
    private final StorageClient storageClient;

    @Override
    public Optional<AudioInfo> read(String bucket, String key, String extension) {

        Path temp = null;
        try {
            temp = Files.createTempFile("upload-audio-", "," + extension);
            storageClient.download(bucket, key, temp);

            AudioFile file = AudioFileIO.read(temp.toFile());
            AudioHeader header = file.getAudioHeader();

            int seconds = header.getTrackLength();
            long kbps = header.getBitRateAsNumber();
            if (seconds <= 0) {
                return Optional.empty();
            }

            return Optional.of(new AudioInfo(seconds, (int) Math.max(kbps, 0)));


        } catch (CannotReadException | InvalidAudioFrameException | TagException | ReadOnlyFileException e) {
            log.debug("not valid audio: {}", e.toString());
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // best effort: the OS temp cleaner takes it
                }
            }
        }
    }
}
