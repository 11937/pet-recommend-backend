package com.pet.common.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @BelongsProject: user-mybatis-plus
 * @BelongsPackage: com.jk.util
 * @Author: wmh
 * @CreateTime: 2024-09-20  10:22
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = -1612980093063478312L;

    private int code;   // 状态码: 200成功、 500代码错误
    private String msg; // 返回信息: ***成功、 ***失败
    private T data;     // 接口数据: 返回的数据

    // 无参构造
    public Result() {
    }

    // 查询 回显.....
    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    //
    public Result(String msg, T data) {
        this.code=200;
        this.msg = msg;
        this.data = data;
    }

    // 删除、新增、修改
    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

