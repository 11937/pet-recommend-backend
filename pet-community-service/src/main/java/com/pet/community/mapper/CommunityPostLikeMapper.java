package com.pet.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.community.entity.CommunityPostLike;
import org.apache.ibatis.annotations.Mapper;

/** 点赞表 Mapper。 */
@Mapper
public interface CommunityPostLikeMapper extends BaseMapper<CommunityPostLike> {
}
