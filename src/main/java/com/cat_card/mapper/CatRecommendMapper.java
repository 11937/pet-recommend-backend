package com.cat_card.mapper;

import com.cat_card.pojo.Cat;
import com.cat_card.pojo.CatRecommendDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface CatRecommendMapper {
    // 方法名必须MyBatis XML里的select标签id一致（recommendCats）
    List<Cat> recommendCats(CatRecommendDTO dto);

    List<Cat> selectAllCats();
}