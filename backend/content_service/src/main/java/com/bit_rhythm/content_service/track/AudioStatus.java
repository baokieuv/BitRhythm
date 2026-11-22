package com.bit_rhythm.content_service.track;

public enum AudioStatus {
    PENDING_UPLOAD,   // the track has no usable audio: either no upload URL was issued yet or the file is not confirmed
    READY             // audio uploaded, checked and measured: the track can be published
}
