package com.bit_rhythm.user_service.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserEventType {
    USER_REGISTERED("USER_REGISTERED"),
    USER_DISABLED("USER_DISABLED"),
    USER_DELETED("USER_DELETED");

    private String name;

    UserEventType(String name) {
        this.name = name;
    }

    public static UserEventType fromName(String name) {
        for (UserEventType type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
