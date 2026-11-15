package com.bit_rhythm.content_service.studio;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Cases to cover when UploadService is implemented (mock StorageClient and the repositories). */
@Disabled("TODO: implement the service first")
class UploadServiceTest {

    @Test void audioUploadRefusesATypeThatIsNotAllowed() { }

    @Test void audioUploadRefusesAFileOverTheLimit() { }

    @Test void audioUploadKeyIsChosenByTheServiceUnderThePrefixOfTheTrack() { }

    @Test void aSecondAudioUploadReplacesThePreviousObject() { }

    @Test void confirmRefusesAnObjectThatWasNeverUploaded() { }

    @Test void confirmReadsTheRealDurationFromTheFile() { }

    @Test void confirmDeletesAFileThatIsNotValidAudio() { }

    @Test void confirmIsIdempotentWhenAlreadyReady() { }

    @Test void coverUploadRefusesAnImageThatIsTooLarge() { }
}
