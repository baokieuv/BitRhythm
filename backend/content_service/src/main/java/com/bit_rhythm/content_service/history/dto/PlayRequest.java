package com.bit_rhythm.content_service.history.dto;

import jakarta.validation.constraints.Min;

/** How many seconds of the track the listener has really heard. The client sends it once, when it passes content.playback.min-play-seconds. */
public record PlayRequest(@Min(0) int positionSeconds) {
}
