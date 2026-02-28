package com.cat_card.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cat_card.mapper.CatMapper;
import com.cat_card.pojo.Cat;
import com.cat_card.service.CatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatServiceImpl implements CatService {
    // 注入mapper接口（推荐用@Resource，和@Autowired等效，更规范）
    @Autowired
    private CatMapper catMapper;

    @Override
    public List<Cat> findCatList(Cat cat) {
        QueryWrapper<Cat> query = new QueryWrapper<>();

        // 关键词非空时，执行全维度模糊匹配
        if (null != cat.getBreedName() && !"".equals(cat.getBreedName().trim())) {
            String keyword = cat.getBreedName().trim();

            // 核心改造：拼接所有需要匹配的字段（枚举字段转文字 + 普通字段）
            // CONCAT函数将多个字段拼接成一个字符串，再用LIKE匹配关键词
            query.apply("CONCAT(" +
                    // 1. 品种名（原有的单字段）
                    "breed_name, " +
                    // 2. 饲养环境（枚举转文字：1=小户型，2=中户型，3=大户型/别墅）
                    "CASE suitable_housing WHEN 1 THEN '小户型' WHEN 2 THEN '中户型' WHEN 3 THEN '大户型' WHEN 3 THEN '别墅' ELSE '' END, " +
                    // 3. 性格特点（枚举转文字：1=活泼粘人，2=温顺安静，3=亲人不粘人，4=慵懒温顺）
                    "CASE personality_type WHEN 1 THEN '活泼粘人' WHEN 2 THEN '温顺安静' WHEN 3 THEN '亲人不粘人' WHEN 4 THEN '慵懒温顺' ELSE '' END, " +
                    // 4. 适合饲养经验（枚举转文字：1=新手，2=有一定经验，3=资深铲屎官）
                    "CASE suitable_experience WHEN 1 THEN '新手' WHEN 2 THEN '有一定经验' WHEN 3 THEN '资深铲屎官' ELSE '' END, " +
                    // 5. 饲养预算（枚举转文字：1=低，2=中，3=高）
                    "CASE budget_level WHEN 1 THEN '低' WHEN 2 THEN '中' WHEN 3 THEN '高' ELSE '' END, " +
                    // 6. 掉毛程度（枚举转文字：1=轻度，2=中度，3=重度）
                    "CASE shedding_degree WHEN 1 THEN '轻度' WHEN 2 THEN '中度' WHEN 3 THEN '重度' ELSE '' END, " +
                    // 7. 每日护理时长（拼接“小时”，方便匹配“2小时”）
                    "CONCAT(required_care_hours, '小时')" +
                    ") LIKE {0}", "%" + keyword + "%");
        }
        // 关键词为空时，返回全量数据（根据前端需求调整，前端传空时拉全量）
        // else {
        //
        // }

        return catMapper.selectList(query);
    }
}