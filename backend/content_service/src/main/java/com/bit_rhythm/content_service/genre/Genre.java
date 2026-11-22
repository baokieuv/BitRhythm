package com.bit_rhythm.content_service.genre;

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

/** Music genre. A short fixed list (seeded by Flyway), managed by admins. */
@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
public class Genre extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;   // "hip-hop": stable key for URLs

    public Genre(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
}
