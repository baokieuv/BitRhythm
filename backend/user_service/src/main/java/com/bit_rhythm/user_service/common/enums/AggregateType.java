package com.bit_rhythm.user_service.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AggregateType {
    USER("USER"),
    ARTIST("ARTIST"),
    NOTIFICATION("NOTIFICATION");

    private String name;

    AggregateType(String name) {
        this.name = name;
    }

    public static AggregateType fromName(String name) {
        for (AggregateType type : AggregateType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

}
