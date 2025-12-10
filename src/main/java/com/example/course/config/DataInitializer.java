package com.example.course.config;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.course.entity.User;
import com.example.course.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在管理员账户
        User adminUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));

        if (adminUser == null) {
            // 创建默认管理员账户
            User admin = new User();
            admin.setId("adm00001"); // 设置格式化ID作为主键
            admin.setUsername("admin");
            admin.setRealName("系统管理员");
            admin.setPassword(SecureUtil.md5("admin123")); // 密码加密
            admin.setRole(1); // 管理员角色

            userService.save(admin);
            System.out.println("默认管理员账户已创建: admin/admin123 (ID: adm00001)");
        } else {
            System.out.println("管理员账户已存在");
        }
    }
}