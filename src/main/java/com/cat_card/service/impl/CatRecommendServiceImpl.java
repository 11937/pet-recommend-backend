package com.cat_card.service.impl;

import com.cat_card.mapper.CatRecommendMapper;
import com.cat_card.pojo.Cat;
import com.cat_card.pojo.CatRecommendDTO;
import com.cat_card.service.CatRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

// 注意：加@Service注解，让Spring扫描并管理这个类
@Service
public class CatRecommendServiceImpl implements CatRecommendService {

    // 注入Mapper（下一步实现Mapper接口）
    @Autowired
    private CatRecommendMapper catRecommendMapper;

    @Override
    public List<Cat> recommendCats(CatRecommendDTO dto) {
        // 直接调用Mapper的方法
        return catRecommendMapper.recommendCats(dto);
    }
}