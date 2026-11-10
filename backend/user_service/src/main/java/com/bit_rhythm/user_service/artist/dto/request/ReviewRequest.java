package com.bit_rhythm.user_service.artist.dto.request;

import jakarta.validation.constraints.Size;

public record ReviewRequest(@Size(max = 500) String note) {
}
