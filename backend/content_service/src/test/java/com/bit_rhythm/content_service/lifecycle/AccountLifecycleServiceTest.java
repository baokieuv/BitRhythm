package com.bit_rhythm.content_service.lifecycle;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** [S2] Cases to cover when AccountLifecycleService is implemented. */
@Disabled("TODO: implement the service first")
class AccountLifecycleServiceTest {

    @Test void aDuplicateEventIsIgnored() { }

    @Test void anUnknownEventTypeIsIgnored() { }

    @Test void aDisabledArtistsPublishedMusicIsHiddenNotTakenDown() { }

    @Test void aDeletedUsersPlaylistsLikesSavedAlbumsAndHistoryAreRemoved() { }

    @Test void likeCountersAreCorrectedBeforeTheLikesAreDeleted() { }

    @Test void storageObjectsAreDeletedOnlyAfterTheCommit() { }
}
