package com.cat_card.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cat_card.entity.TestTable;
import com.cat_card.mapper.TestTableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class TestController {

    // 注入MyBatis-Plus的Mapper（适配你的技术栈）
    @Autowired
    private TestTableMapper testTableMapper;

    /**
     * 测试：创建表+插入数据+查询总数（MyBatis-Plus版）
     */
    @GetMapping("/test/db/mybatis")
    public String testMybatisPlus() {
        // 1. 插入测试数据
        TestTable testTable = new TestTable();
        testTable.setContent("MyBatis-Plus测试数据-" + System.currentTimeMillis());
        testTable.setCreateTime(LocalDateTime.now());
        testTableMapper.insert(testTable);

        // 2. 查询数据总数
        long total = testTableMapper.selectCount(new LambdaQueryWrapper<TestTable>());

        return "MyBatis-Plus连接数据库成功！测试表总数据量：" + total;
    }

    /**
     * 测试：查询MySQL版本（直接执行原生SQL）
     */
    @GetMapping("/test/db/version")
    public String testDbVersion() {
        String version = testTableMapper.getDbVersion();
        return "数据库连接成功！MySQL版本：" + version;
    }
}