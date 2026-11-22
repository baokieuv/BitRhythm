package com.bit_rhythm.content_service.library;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface SavedAlbumRepository extends JpaRepository<SavedAlbum, Long> {

    boolean existsByUserIdAndAlbumId(Long userId, Long albumId);

    List<SavedAlbum> findByUserIdAndAlbumIdIn(Long userId, Collection<Long> albumIds);   // "saved" flag of a page of albums

    List<SavedAlbum> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long cursor, Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SavedAlbum s WHERE s.userId = :userId AND s.albumId = :albumId")
    int deletePair(@Param("userId") Long userId, @Param("albumId") Long albumId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SavedAlbum s WHERE s.albumId = :albumId")
    int deleteAllOfAlbum(@Param("albumId") Long albumId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SavedAlbum s WHERE s.userId = :userId")
    int deleteAllOfUser(@Param("userId") Long userId);   // [S2] account deleted
}
