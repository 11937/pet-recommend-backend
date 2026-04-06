package com.pet.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.community.entity.CommunityPost;
import org.apache.ibatis.annotations.Mapper;

/** 帖子主表 Mapper。 */
@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {
}
