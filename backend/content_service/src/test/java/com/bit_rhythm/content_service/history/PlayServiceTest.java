package com.bit_rhythm.content_service.history;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Cases to cover when PlayService and PlayCounter are implemented. */
@Disabled("TODO: implement the service first")
class PlayServiceTest {

    @Test void aPlayBelowTheThresholdIsIgnored() { }

    @Test void aRepeatedPlayInTheWindowIsNotCountedTwice() { }

    @Test void aCountedPlayWritesTheHistoryRowAndIncrementsTheCounter() { }

    @Test void playingAnUnpublishedTrackIsNotFound() { }

    @Test void historyLeavesOutTracksThatAreNoLongerPublished() { }
}
