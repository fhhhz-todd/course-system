package com.example.course.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户登录请求参数
 */
@Data
public class LoginRequest implements Serializable {

    // @NotBlank 校验：用户名不能为空
    @NotBlank(message = "用户名不能为空")
    private String username;

    // @NotBlank 校验：密码不能为空
    @NotBlank(message = "密码不能为空")
    private String password;
}