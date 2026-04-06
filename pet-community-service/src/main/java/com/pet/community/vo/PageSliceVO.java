package com.pet.community.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 简易分页片：含是否还有下一页（热度合并场景 total 为合并列表长度）。 */
@Data
public class PageSliceVO<T> {

    private List<T> records = new ArrayList<>();
    private long page;
    private long size;
    private long total;
    private boolean hasMore;
}
