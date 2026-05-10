-- ============================================================
-- 复旦校园百事通问答系统 · 数据库模式定义
-- 适用环境：MySQL 8.0+
-- 字符集：utf8mb4
-- ============================================================

-- 强制会话字符集，避免客户端编码不一致导致中文报错
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

CREATE DATABASE IF NOT EXISTS fudan_campus_info
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE fudan_campus_info;

-- -----------------------------------------------------------
-- 1. 院系表
-- -----------------------------------------------------------
CREATE TABLE department (
    department_id   INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 2. 用户表
-- -----------------------------------------------------------
CREATE TABLE user (
    user_id         INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(50)  NOT NULL,
    grade           VARCHAR(20)  CHECK (grade REGEXP '^[0-9]{4}级$'),
    department_id   INT,
    role            ENUM('user','admin') NOT NULL DEFAULT 'user',
    email           VARCHAR(100) CHECK (email LIKE '%@_%._%'),
    phone           VARCHAR(20),
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES department(department_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 3. 校区表
-- -----------------------------------------------------------
CREATE TABLE campus (
    campus_id       INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50)  NOT NULL UNIQUE,
    address         VARCHAR(200),
    contact_phone   VARCHAR(20),
    latitude        DECIMAL(10,8) CHECK (latitude BETWEEN -90 AND 90),
    longitude       DECIMAL(11,8) CHECK (longitude BETWEEN -180 AND 180),
    description     TEXT
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 4. 建筑表
-- -----------------------------------------------------------
CREATE TABLE building (
    building_id     INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    type            VARCHAR(50),
    campus_id       INT          NOT NULL,
    floors          INT          CHECK (floors >= 0),
    open_time       TIME,
    close_time      TIME,
    description     TEXT,
    FOREIGN KEY (campus_id) REFERENCES campus(campus_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 5. 设施表
-- -----------------------------------------------------------
CREATE TABLE facility (
    facility_id     INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    type            VARCHAR(50)  NOT NULL,
    building_id     INT          NOT NULL,
    open_time       VARCHAR(100),
    location_desc   VARCHAR(200),
    capacity        INT          CHECK (capacity >= 0),
    contact         VARCHAR(50),
    description     TEXT,
    FOREIGN KEY (building_id) REFERENCES building(building_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 6. 教师表
-- -----------------------------------------------------------
CREATE TABLE teacher (
    teacher_id      INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50)  NOT NULL,
    department_id   INT,
    title           VARCHAR(50),
    email           VARCHAR(100) CHECK (email LIKE '%@_%._%'),
    phone           VARCHAR(20),
    FOREIGN KEY (department_id) REFERENCES department(department_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 7. 课程表
-- -----------------------------------------------------------
CREATE TABLE course (
    course_id       INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    department_id   INT,
    credits         DECIMAL(3,1) CHECK (credits > 0 AND credits <= 20),
    description     TEXT,
    semester_offered VARCHAR(20),
    FOREIGN KEY (department_id) REFERENCES department(department_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 8. 课程-教师授课关系表（多对多）
-- -----------------------------------------------------------
CREATE TABLE course_teacher (
    course_id       INT,
    teacher_id      INT,
    semester        VARCHAR(20),
    role            VARCHAR(30) DEFAULT '主讲',
    remarks         TEXT,
    PRIMARY KEY (course_id, teacher_id, semester),
    FOREIGN KEY (course_id) REFERENCES course(course_id)
        ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 9. 活动表
-- -----------------------------------------------------------
CREATE TABLE event (
    event_id        INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    event_time      DATETIME     NOT NULL,
    location_desc   VARCHAR(200),
    campus_id       INT,
    building_id     INT,
    organizer       VARCHAR(100),
    category        VARCHAR(50),
    description     TEXT,
    FOREIGN KEY (campus_id) REFERENCES campus(campus_id)
        ON DELETE SET NULL,
    FOREIGN KEY (building_id) REFERENCES building(building_id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 10. 查询记录表
-- -----------------------------------------------------------
CREATE TABLE query_record (
    record_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT          NOT NULL,
    question        TEXT         NOT NULL,
    query_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    category        VARCHAR(50),
    result_summary  VARCHAR(200),
    used_nl2sql     BOOLEAN      DEFAULT FALSE,
    client_ip       VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 索引（除主键与外键自动索引外，额外显式创建）
-- ============================================================
CREATE INDEX idx_building_campus        ON building(campus_id);
CREATE INDEX idx_facility_building      ON facility(building_id);
CREATE INDEX idx_course_department      ON course(department_id);
CREATE INDEX idx_teacher_department     ON teacher(department_id);
CREATE INDEX idx_event_time             ON event(event_time);
CREATE INDEX idx_query_user_time        ON query_record(user_id, query_time);
CREATE INDEX idx_course_teacher_teacher ON course_teacher(teacher_id);