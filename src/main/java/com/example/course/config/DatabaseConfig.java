package com.example.course.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class DatabaseConfig implements InitializingBean {
    
    private final DataSource dataSource;
    
    public DatabaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName().toLowerCase();
            
            String schemaResource;
            if (databaseProductName.contains("mysql")) {
                schemaResource = "db/schema.sql";
            } else if (databaseProductName.contains("h2")) {
                schemaResource = "db/h2_schema.sql";
            } else {
                schemaResource = "db/schema.sql"; // 默认使用MySQL版本
            }
            
            // 检查是否已有用户数据，如果已有则跳过初始化
            if (!hasUserData(connection)) {
                System.out.println("正在初始化数据库...");
                // 执行数据库初始化脚本
                ScriptUtils.executeSqlScript(connection, new ClassPathResource(schemaResource));
                System.out.println("数据库初始化完成");
            } else {
                System.out.println("检测到已有用户数据，跳过数据库初始化");
            }
        } catch (SQLException e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            throw e; // 重新抛出异常，以便Spring能正确处理
        }
    }
    
    private boolean hasUserData(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'USER'");) {
            if (rs.next() && rs.getInt(1) > 0) {
                // 表存在，检查是否有数据
                try (Statement dataStmt = connection.createStatement();
                     ResultSet dataRs = dataStmt.executeQuery("SELECT COUNT(*) FROM \"USER\" WHERE role IN (1, 2, 3)");) {
                    if (dataRs.next()) {
                        return dataRs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            // 如果检查表存在性时出错，返回false让初始化继续
            System.out.println("检查用户表是否存在时出错: " + e.getMessage());
        }
        return false;
    }
}