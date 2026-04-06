package com.pet.community.enums;

import lombok.Getter;

/** 审核状态：当前发帖默认通过，后续可接审核接口。 */
@Getter
public enum PostAuditStatus {
    PENDING(0),
    APPROVED(1),
    REJECTED(2);

    private final int code;

    PostAuditStatus(int code) {
        this.code = code;
    }
}
