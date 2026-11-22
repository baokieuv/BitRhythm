package com.bit_rhythm.content_service.library;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Cases to cover when LibraryService is implemented. */
@Disabled("TODO: implement the service first")
class LibraryServiceTest {

    @Test void likingTwiceCountsOnce() { }

    @Test void likeInsertsTheRowAndTheCounterInOneTransaction() { }

    @Test void aConcurrentDuplicateLikeIsIgnored() { }

    @Test void unlikeCorrectsTheCounterOnlyWhenARowWasDeleted() { }

    @Test void likingAnUnpublishedTrackIsNotFound() { }

    @Test void likedTracksPageUsesTheLikeIdAsTheCursor() { }

    @Test void savedAlbumsAreIdempotent() { }
}
