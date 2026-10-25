package com.bit_rhythm.core.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class CommonEnums {
    @Getter
    @RequiredArgsConstructor
    public static enum DeletedStatus {
        DELETED(1),
        NOT_DELETED(0);

        private final Integer code;
    }
}
