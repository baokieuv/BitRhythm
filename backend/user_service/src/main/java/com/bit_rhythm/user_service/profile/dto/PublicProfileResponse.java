package com.bit_rhythm.user_service.profile.dto;

/** DTO công khai: không có email, không có cài đặt. Hồ sơ PRIVATE chỉ có displayName + avatarUrl. */
public record PublicProfileResponse(Long userId, String displayName, String avatarUrl, String bio, String country,
                                    Boolean verified, Long followerCount, Long followingCount, boolean restricted) {}
