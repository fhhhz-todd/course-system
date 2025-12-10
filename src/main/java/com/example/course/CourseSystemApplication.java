package com.example.course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
public class CourseSystemApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CourseSystemApplication.class, args);
        
        // 应用启动后自动打开默认浏览器
        openBrowser(context);
    }

    private static void openBrowser(ConfigurableApplicationContext context) {
        try {
            // 等待应用完全启动
            Thread.sleep(2000);
            
            // 获取服务器端口
            String port = context.getEnvironment().getProperty("server.port", "8080");
            String url = "http://localhost:" + port + "/login.html";
            
            // 使用默认浏览器打开
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                    System.out.println("浏览器已打开: " + url);
                } else {
                    System.out.println("当前环境不支持自动打开浏览器，请手动访问: " + url);
                }
            } else {
                System.out.println("当前环境不支持自动打开浏览器，请手动访问: " + url);
            }
        } catch (Exception e) {
            System.err.println("无法自动打开浏览器: " + e.getMessage());
            String port = context.getEnvironment().getProperty("server.port", "8080");
            System.out.println("请手动访问: http://localhost:" + port + "/login.html");
        }
    }
}