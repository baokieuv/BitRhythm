package com.bit_rhythm.user_service.profile.dto;

/** DTO của chính chủ. */
public record ProfileResponse(Long userId, String email, String displayName, String bio, String country,
                              String avatarUrl, boolean verified, long followerCount, long followingCount) {}
