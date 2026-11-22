package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.content_service.album.AlbumRepository;
import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.studio.dto.StudioStatsResponse;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import com.bit_rhythm.content_service.track.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudioStatsService {

    private static final int TOP_TRACKS_PAGE_SIZE = 5;

    private final StudioOwnership studioOwnership;
    private final TrackRepository trackRepository;
    private final TrackService trackService;
    private final AlbumRepository albumRepository;


    public StudioStatsResponse stats(Long userId) {
        Artist artist = studioOwnership.artist(userId);
        Long artistId = artist.getId();

        long totalAlbums = albumRepository.countByArtistIdAndStatus(artistId, ContentStatus.PUBLISHED);
        long totalTracks = trackRepository.countByArtistId(artistId);
        long publishedTracks = trackRepository.countByArtistIdAndStatus(artistId, ContentStatus.PUBLISHED);

        // ONE aggregate query for both sums
        TrackRepository.ArtistTotals totals = trackRepository.totalsOfArtist(artistId);

        List<Track> top = trackRepository.findByArtistIdOrderByPlayCountDescIdDesc(artistId, PageRequest.ofSize(TOP_TRACKS_PAGE_SIZE));

        return new StudioStatsResponse(
                totalTracks,
                publishedTracks,
                totalAlbums,
                totals.getPlays(),
                totals.getLikes(),
                trackService.toResponses(top, userId).stream().map(StudioStatsResponse.TrackStat::from).toList()
        );
    }
}
