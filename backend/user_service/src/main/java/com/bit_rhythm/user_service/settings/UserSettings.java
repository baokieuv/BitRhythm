package com.bit_rhythm.user_service.settings;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_settings")
@Getter @Setter @NoArgsConstructor
public class UserSettings {

    @Id
    private Long userId;                       // cùng id với auth-service

    @Column(nullable = false)
    private String locale = "vi";              // "vi" | "en"

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_visibility", nullable = false)
    private ProfileVisibility visibility = ProfileVisibility.PUBLIC;

    @Column(nullable = false)
    private boolean showListeningActivity = true;

    public static UserSettings defaults(Long userId) {
        var s = new UserSettings();
        s.userId = userId;
        return s;
    }
}
