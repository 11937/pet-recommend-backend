package com.pet.community.enums;

import lombok.Getter;

@Getter
public enum AdminActionType {
    AUDIT_APPROVE(1),
    AUDIT_REJECT(2),
    ADMIN_OFFLINE(3);

    private final int code;

    AdminActionType(int code) {
        this.code = code;
    }
}
