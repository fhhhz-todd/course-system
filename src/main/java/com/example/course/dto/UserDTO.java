package com.example.course.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String realName;
    private Integer role;
    private String roleName;
}