package com.pet.community.enums;

import lombok.Getter;

/** 帖子上架状态：与作者/管理员下架区分，库内不物理删除。 */
@Getter
public enum PostVisibilityStatus {
    ONLINE(1),
    AUTHOR_OFFLINE(2),
    ADMIN_OFFLINE(3);

    private final int code;

    PostVisibilityStatus(int code) {
        this.code = code;
    }

    public static PostVisibilityStatus fromCode(int code) {
        for (PostVisibilityStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown visibility: " + code);
    }
}
