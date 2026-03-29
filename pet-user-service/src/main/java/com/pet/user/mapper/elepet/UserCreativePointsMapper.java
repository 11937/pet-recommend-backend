package com.pet.user.mapper.elepet;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.user.entity.elepet.UserCreativePoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
@Mapper
public interface UserCreativePointsMapper extends BaseMapper<UserCreativePoints> {

    @Update("UPDATE user_creative_points SET points = points + #{points} WHERE user_id = #{userId}")
    int addPoints(@Param("userId") Long userId, @Param("points") int points);

    @Update("UPDATE user_creative_points SET points = points - #{points} WHERE user_id = #{userId} AND points >= #{points}")
    int deductPoints(@Param("userId") Long userId, @Param("points") int points);
}