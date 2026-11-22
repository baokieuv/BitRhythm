package com.bit_rhythm.content_service.common;

/** Lifecycle of a track or an album. Listeners only ever see PUBLISHED. */
public enum ContentStatus {
    DRAFT,        // being prepared by its owner
    PUBLISHED,    // visible to everybody
    HIDDEN,       // unpublished by its owner: the owner can publish it again
    TAKEN_DOWN    // removed by an admin (report, copyright...): the owner can NOT publish it again, only an admin can restore it
}
