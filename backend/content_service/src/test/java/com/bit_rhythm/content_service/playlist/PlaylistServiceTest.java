package com.bit_rhythm.content_service.playlist;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Cases to cover when PlaylistService is implemented. */
@Disabled("TODO: implement the service first")
class PlaylistServiceTest {

    @Test void aPrivatePlaylistOfSomebodyElseIsNotFound() { }

    @Test void onlyTheOwnerCanChangeAPlaylist() { }

    @Test void addingATrackAppendsAtTheEndAndCountsIt() { }

    @Test void addingTheSameTrackTwiceIsRefused() { }

    @Test void aFullPlaylistRefusesMoreTracks() { }

    @Test void addingAnUnpublishedTrackIsRefused() { }

    @Test void removingATrackCorrectsTheCounterOnlyWhenARowWasDeleted() { }

    @Test void reorderNeedsExactlyTheTracksOfThePlaylist() { }

    @Test void tracksPageUsesThePositionAsTheCursor() { }

    @Test void deletingAPlaylistRemovesItsTracks() { }
}
