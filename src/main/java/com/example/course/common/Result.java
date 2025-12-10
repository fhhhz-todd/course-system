package com.example.course.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一API返回结果封装
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; // 状态码：200 成功，500 失败
    private String msg;   // 提示信息
    private T data;       // 返回的数据对象

    // 私有化构造器
    private Result() {}

    // 成功（不带数据）
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        return result;
    }

    // 成功（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.setData(data);
        return result;
    }

    // 失败（带自定义消息）
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500); // 默认失败状态码
        result.setMsg(msg);
        return result;
    }

    // 失败（带自定义状态码和消息）
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}