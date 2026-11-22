package com.bit_rhythm.content_service.track;

import com.bit_rhythm.content_service.common.ContentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {

    List<Track> findByIdInAndIsDeleted(Collection<Long> ids, Integer isDeleted);

    Optional<Track> findByIdAndIsDeleted(Long id, Integer isDeleted);

    // Keyset pagination (cursor = id of the last row received, Long.MAX_VALUE for the first page). Ask for size + 1 rows.
    List<Track> findByStatusAndIdLessThanOrderByIdDesc(ContentStatus status, Long cursor, Pageable pageable);   // new releases

    List<Track> findByStatusAndGenreIdAndIdLessThanOrderByIdDesc(ContentStatus status, Long genreId, Long cursor, Pageable pageable);

    List<Track> findByArtistIdAndStatusAndIdLessThanOrderByIdDesc(Long artistId, ContentStatus status, Long cursor, Pageable pageable);   // artist page

    List<Track> findByArtistIdAndIdLessThanOrderByIdDesc(Long artistId, Long cursor, Pageable pageable);   // studio: all statuses

    List<Track> findByAlbumIdAndStatusOrderByTrackNumberAscIdAsc(Long albumId, ContentStatus status);   // an album is short: no pagination

    List<Track> findByAlbumIdOrderByTrackNumberAscIdAsc(Long albumId);   // studio

    long countByArtistId(Long artistId);

    long countByArtistIdAndStatus(Long artistId, ContentStatus status);

    /**
     * Full-text search on the title of PUBLISHED tracks (FULLTEXT index ft_track_title). booleanQuery is built by the service from
     * the user's words, e.g. "+hello* +world*": strip the operators the user typed (+ - ~ < > ( ) " @) first.
     */
    @Query(value = "SELECT * FROM tracks WHERE status = 'PUBLISHED' AND id < :cursor "
            + "AND MATCH(title) AGAINST(:query IN BOOLEAN MODE) ORDER BY id DESC LIMIT :limit", nativeQuery = true)
    List<Track> searchPublished(@Param("query") String booleanQuery, @Param("cursor") Long cursor, @Param("limit") int limit);

    /** Atomic counters: "SET x = x + :delta" runs inside the database, so concurrent plays or likes cannot lose an update. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Track t SET t.playCount = t.playCount + :delta WHERE t.id = :id")
    int addPlays(@Param("id") Long id, @Param("delta") long delta);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Track t SET t.likeCount = t.likeCount + :delta WHERE t.id = :id")
    int addLikes(@Param("id") Long id, @Param("delta") long delta);

    // [S2] discovery: most played (offset pagination is fine for a short, slowly changing top list)
    List<Track> findByStatusOrderByPlayCountDescIdDesc(ContentStatus status, Pageable pageable);

    List<Track> findByArtistId(Long artistId);   // [S2] account deleted: hide everything of the artist
}
