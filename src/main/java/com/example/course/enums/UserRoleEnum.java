package com.example.course.enums;

public enum UserRoleEnum {
    ADMIN(1, "管理员"),
    TEACHER(2, "教师"),
    STUDENT(3, "学生");

    private final Integer code;
    private final String description;

    UserRoleEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserRoleEnum fromCode(Integer code) {
        for (UserRoleEnum role : UserRoleEnum.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}