package com.pet.community.enums;

import lombok.Getter;

@Getter
public enum PostMediaType {
    IMAGE(1),
    VIDEO(2);

    private final int code;

    PostMediaType(int code) {
        this.code = code;
    }
}
