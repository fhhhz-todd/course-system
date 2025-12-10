
-- 创建数据库
CREATE DATABASE IF NOT EXISTS course_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE course_system;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` varchar(20) NOT NULL COMMENT '主键ID（格式化ID：stu00001/tea00001/adm00001格式）',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(MD5加密)',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `role` int NOT NULL COMMENT '角色: 1-管理员, 2-教师, 3-学生',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建课程表
CREATE TABLE IF NOT EXISTS `course` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_code` varchar(20) NOT NULL COMMENT '课程编号',
  `course_name` varchar(100) NOT NULL COMMENT '课程名称',
  `credits` int NOT NULL COMMENT '学分',
  `teacher_id` varchar(20) NOT NULL COMMENT '教师ID（格式化ID）',
  `description` text COMMENT '课程描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_teacher_id` (`teacher_id`),
  FOREIGN KEY (`teacher_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 创建学生选课表
CREATE TABLE IF NOT EXISTS `student_course` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` varchar(20) NOT NULL COMMENT '学生ID（格式化ID）',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_course` (`student_id`, `course_id`),
  KEY `idx_course_id` (`course_id`),
  FOREIGN KEY (`student_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`course_id`) REFERENCES `course`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生选课表';

-- 插入默认管理员账户 (密码: admin123)
INSERT IGNORE INTO `user` (id, username, password, real_name, role) 
VALUES ('adm00001', 'admin', '0192023a7bbd73250516f069df18b500', '系统管理员', 1);

-- 插入示例教师账户 (密码: 123456)
INSERT IGNORE INTO `user` (id, username, password, real_name, role) 
VALUES ('tea00001', 'teacher1', 'e10adc3949ba59abbe56e057f20f883e', '张老师', 2);

-- 插入示例学生账户 (密码: 123456)
INSERT IGNORE INTO `user` (id, username, password, real_name, role) 
VALUES ('stu00001', 'student1', 'e10adc3949ba59abbe56e057f20f883e', '张学生', 3);

-- 插入示例课程数据
INSERT IGNORE INTO `course` (course_code, course_name, credits, teacher_id, description) 
VALUES ('CS101', '计算机科学导论', 3, 'tea00001', '计算机科学的基础课程');

INSERT IGNORE INTO `course` (course_code, course_name, credits, teacher_id, description) 
VALUES ('MATH201', '高等数学', 4, 'tea00001', '微积分和线性代数');

-- 创建作业表
CREATE TABLE IF NOT EXISTS `assignment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) NOT NULL COMMENT '作业标题',
  `description` text COMMENT '作业描述',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `deadline` datetime COMMENT '截止时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  FOREIGN KEY (`course_id`) REFERENCES `course`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业表';

-- 创建作业提交表
CREATE TABLE IF NOT EXISTS `submission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `assignment_id` bigint NOT NULL COMMENT '作业ID',
  `student_id` varchar(20) NOT NULL COMMENT '学生ID（格式化ID）',
  `content` text COMMENT '提交内容',
  `attachment_path` varchar(500) COMMENT '附件路径',
  `score` decimal(5,2) COMMENT '分数',
  `comment` text COMMENT '评语',
  `reply` text COMMENT '学生回复',
  `status` int DEFAULT 0 COMMENT '状态: 0-已提交, 1-已批阅',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_assignment_id` (`assignment_id`),
  KEY `idx_student_id` (`student_id`),
  FOREIGN KEY (`assignment_id`) REFERENCES `assignment`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`student_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业提交表';

-- 插入示例选课数据
INSERT IGNORE INTO `student_course` (student_id, course_id) 
VALUES ('stu00001', 1);

-- 创建消息表
CREATE TABLE IF NOT EXISTS `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(20) NOT NULL COMMENT '用户ID（格式化ID）',
  `title` varchar(200) NOT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `sender` varchar(100) NOT NULL COMMENT '发件人',
  `status` int DEFAULT 0 COMMENT '状态: 0-未读, 1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';