package com.bit_rhythm.core.dto;

import com.bit_rhythm.core.common.EmailType;

import java.util.Map;

public record EmailEventDto(
        EmailType emailType,

        Map<String, Object> data
) {
}
