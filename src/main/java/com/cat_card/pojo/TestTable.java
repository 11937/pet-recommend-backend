package com.cat_card.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("test_table") // 对应数据库表名
public class TestTable {
    @TableId(type = IdType.AUTO)
    private Integer id;         // 主键自增
    private String content;     // 测试内容
    private LocalDateTime createTime; // 创建时间
}