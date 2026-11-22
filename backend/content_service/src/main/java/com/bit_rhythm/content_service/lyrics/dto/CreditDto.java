package com.bit_rhythm.content_service.lyrics.dto;

import com.bit_rhythm.content_service.lyrics.TrackCredit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreditDto(@NotBlank @Size(max = 40) String role, @NotBlank @Size(max = 150) String name) {


    public static CreditDto from(TrackCredit credit) {
        return new CreditDto(credit.getRole(), credit.getName());
    }
}
