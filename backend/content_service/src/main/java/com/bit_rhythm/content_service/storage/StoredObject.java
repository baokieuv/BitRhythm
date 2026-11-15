package com.bit_rhythm.content_service.storage;

/** What the object store says about an uploaded object. */
public record StoredObject(long size, String contentType) {
}
