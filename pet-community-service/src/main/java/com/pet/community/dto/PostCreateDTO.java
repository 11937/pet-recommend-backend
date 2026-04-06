package com.pet.community.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/** 发帖入参：纯文可不带图，正文最多 1 万字；图片最多 9 张。 */
@Data
public class PostCreateDTO {

    @NotBlank
    @Size(max = 10000)
    private String content;

    /**
     * 图片 URL 列表，可选（纯文本帖可传空列表或不传）；最多 9 张。视频后续扩展。
     */
    @Size(max = 9)
    private List<@NotBlank @Size(max = 2048) String> imageUrls = new ArrayList<>();

    /** 视频仅一期 URL；条数可后续再调 */
    @Size(max = 5)
    private List<@NotBlank @Size(max = 2048) String> videoUrls = new ArrayList<>();
}
