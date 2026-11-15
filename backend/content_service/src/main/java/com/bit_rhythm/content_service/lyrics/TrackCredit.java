package com.bit_rhythm.content_service.lyrics;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** [S2] Who contributed to a track: writer, producer, composer... */
@Entity
@Table(name = "track_credits")
@Getter
@Setter
@NoArgsConstructor
public class TrackCredit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String name;

    public TrackCredit(Long trackId, String role, String name) {
        this.trackId = trackId;
        this.role = role;
        this.name = name;
    }
}
