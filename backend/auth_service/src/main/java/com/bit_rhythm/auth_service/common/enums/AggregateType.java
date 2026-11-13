package com.bit_rhythm.auth_service.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AggregateType {
    USER("USER"),
    NOTIFICATION("NOTIFICATION");

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

    private String name;
}
