-- H2 Database Initialization Script

-- IMPORTANT:
-- 如果你之前用过旧 schema（例如 USER.id 还是数字类型），请手动删除 ./data/course_system_db*.db 进行重建。
-- 为了实现“数据持久化”，这里不再在每次启动时 DROP 表。

-- Create user table (using quoted identifier to handle reserved word)
CREATE TABLE IF NOT EXISTS "USER" (
  id varchar(20) PRIMARY KEY,
  username varchar(50) NOT NULL UNIQUE,
  password varchar(100) NOT NULL,
  real_name varchar(50) NOT NULL,
  role int NOT NULL,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Create course table
CREATE TABLE IF NOT EXISTS "COURSE" (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  course_code varchar(20) NOT NULL,
  course_name varchar(100) NOT NULL,
  credits int NOT NULL,
  teacher_id varchar(20) NOT NULL,
  description clob,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Create student_course table
CREATE TABLE IF NOT EXISTS "STUDENT_COURSE" (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  student_id varchar(20) NOT NULL,
  course_id bigint NOT NULL,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (student_id, course_id)
);

-- Create assignment table
CREATE TABLE IF NOT EXISTS "ASSIGNMENT" (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  title varchar(100) NOT NULL,
  description clob,
  course_id bigint NOT NULL,
  deadline timestamp,
  attachment_path varchar(500),
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Create submission table
CREATE TABLE IF NOT EXISTS "SUBMISSION" (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  assignment_id bigint NOT NULL,
  student_id varchar(20) NOT NULL,
  content clob,
  attachment_path varchar(500),
  score decimal(5,2),
  comment clob,
  reply clob,
  status int DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin account if not exists (password: admin123)
MERGE INTO "USER" (id, username, password, real_name, role) 
KEY(id) 
VALUES ('adm00001', 'admin', '0192023a7bbd73250516f069df18b500', 'System Administrator', 1);

-- Insert sample teacher account if not exists (password: 123456)
MERGE INTO "USER" (id, username, password, real_name, role) 
KEY(id) 
VALUES ('tea00001', 'teacher1', 'e10adc3949ba59abbe56e057f20f883e', 'Teacher Zhang', 2);

-- Insert sample student account if not exists (password: 123456)
MERGE INTO "USER" (id, username, password, real_name, role) 
KEY(id) 
VALUES ('stu00001', 'student1', 'e10adc3949ba59abbe56e057f20f883e', 'Student Zhang', 3);



-- Create message table
CREATE TABLE IF NOT EXISTS "MESSAGES" (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  user_id varchar(20) NOT NULL,
  title varchar(200) NOT NULL,
  content clob,
  sender varchar(100) NOT NULL,
  status int DEFAULT 0,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES "USER"(id)
);