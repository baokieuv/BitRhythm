package com.bit_rhythm.content_service.stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Cases to cover when StreamService is implemented. */
@Disabled("TODO: implement the service first")
class StreamServiceTest {

    @Test void aPublishedTrackWithReadyAudioGivesAShortLivedUrl() { }

    @Test void aDraftIsStreamableByItsOwnerOnly() { }

    @Test void aTakenDownTrackIsNotFoundForListeners() { }

    @Test void aTrackWithoutReadyAudioIsNotFound() { }

    @Test void streamingDoesNotCountAPlay() { }
}
