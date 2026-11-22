package com.bit_rhythm.content_service.studio;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Cases to cover when StudioTrackService is implemented (mock the repositories and ArtistService). */
@Disabled("TODO: implement the service first")
class StudioTrackServiceTest {

    @Test void aUserWithoutAnArtistProfileCannotCreateTracks() { }

    @Test void createRefusesAnAlbumOfAnotherArtist() { }

    @Test void createRefusesAnUnknownGenre() { }

    @Test void aTrackOfAnotherArtistIsNotFound() { }

    @Test void publishNeedsAudioThatIsReady() { }

    @Test void publishKeepsTheFirstPublicationDate() { }

    @Test void publishWritesTheEventOnlyTheFirstTime() { }

    @Test void aTakenDownTrackCannotBePublishedByItsOwner() { }

    @Test void unpublishHidesAPublishedTrack() { }

    @Test void deleteRemovesTheTrackFromPlaylistsLikesAndHistory() { }
}
