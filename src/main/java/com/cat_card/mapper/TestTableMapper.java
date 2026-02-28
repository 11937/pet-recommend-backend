package com.cat_card.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cat_card.entity.TestTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper // 必须加这个注解，让MyBatis扫描到
public interface TestTableMapper extends BaseMapper<TestTable> {
    // 新增：自定义原生SQL查询MySQL版本
    @Select("SELECT VERSION() AS version")
    String getDbVersion();
}
