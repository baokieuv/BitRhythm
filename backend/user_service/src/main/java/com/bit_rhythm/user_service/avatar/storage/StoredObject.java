package com.bit_rhythm.user_service.avatar.storage;

/** What the object store says about an uploaded object. */
public record StoredObject(long size, String contentType) {
}
