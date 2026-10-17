package com.bit_rhythm.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecaptchaResponse(
        boolean success,
        double score,
        String action,
        @JsonProperty("error-codes") List<String> errorCodes
) {}
