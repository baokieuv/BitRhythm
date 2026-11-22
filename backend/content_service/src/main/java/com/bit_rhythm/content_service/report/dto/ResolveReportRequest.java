package com.bit_rhythm.content_service.report.dto;

import jakarta.validation.constraints.Size;

/** takeDown = true also takes the reported content down (TRACK / ALBUM; a playlist is not taken down by this service). */
public record ResolveReportRequest(@Size(max = 500) String note, boolean takeDown) {
}
