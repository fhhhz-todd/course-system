package com.example.course.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 用户注册请求参数
 */
@Data
public class RegisterRequest implements Serializable {

    // 用户名不能为空
    @NotBlank(message = "用户名不能为空")
    private String username;

    // 真实姓名不能为空
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    // 密码不能为空
    @NotBlank(message = "密码不能为空")
    private String password;

    // 角色不能为空
    @NotNull(message = "角色不能为空")
    private Integer role;
}