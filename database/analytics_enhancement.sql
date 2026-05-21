-- ============================================================
-- 复旦校园百事通 · 智能查询推荐与统计分析模块
-- 包含：视图、存储过程、触发器、额外索引
-- ============================================================

USE fudan_campus_info;

-- -----------------------------------------------------------
-- 1. 创建视图：热门查询类别统计
-- -----------------------------------------------------------
CREATE OR REPLACE VIEW v_popular_query_categories AS
SELECT 
    category,
    COUNT(*) AS query_count,
    COUNT(DISTINCT user_id) AS unique_users,
    MAX(query_time) AS last_query_time,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM query_record), 2) AS percentage
FROM query_record
WHERE category IS NOT NULL AND category != ''
GROUP BY category
ORDER BY query_count DESC;

-- -----------------------------------------------------------
-- 2. 创建视图：每日查询趋势
-- -----------------------------------------------------------
CREATE OR REPLACE VIEW v_daily_query_trend AS
SELECT 
    DATE(query_time) AS query_date,
    COUNT(*) AS daily_count,
    COUNT(DISTINCT user_id) AS unique_users,
    SUM(CASE WHEN used_nl2sql = TRUE THEN 1 ELSE 0 END) AS nl2sql_count,
    SUM(CASE WHEN used_nl2sql = FALSE THEN 1 ELSE 0 END) AS manual_count
FROM query_record
GROUP BY DATE(query_time)
ORDER BY query_date DESC;

-- -----------------------------------------------------------
-- 3. 创建视图：活跃用户排行
-- -----------------------------------------------------------
CREATE OR REPLACE VIEW v_active_users AS
SELECT 
    u.user_id,
    u.username,
    u.name,
    COUNT(qr.record_id) AS total_queries,
    COUNT(DISTINCT qr.category) AS categories_explored,
    MIN(qr.query_time) AS first_query_time,
    MAX(qr.query_time) AS last_query_time,
    DATEDIFF(MAX(qr.query_time), MIN(qr.query_time)) AS active_days
FROM user u
LEFT JOIN query_record qr ON u.user_id = qr.user_id
GROUP BY u.user_id, u.username, u.name
HAVING total_queries > 0
ORDER BY total_queries DESC;

-- -----------------------------------------------------------
-- 4. 创建视图：各校区设施热度统计
-- -----------------------------------------------------------
CREATE OR REPLACE VIEW v_campus_facility_popularity AS
SELECT 
    c.campus_id,
    c.name AS campus_name,
    COUNT(DISTINCT b.building_id) AS building_count,
    COUNT(DISTINCT f.facility_id) AS facility_count,
    GROUP_CONCAT(DISTINCT f.type ORDER BY f.type SEPARATOR ', ') AS facility_types
FROM campus c
LEFT JOIN building b ON c.campus_id = b.campus_id
LEFT JOIN facility f ON b.building_id = f.building_id
GROUP BY c.campus_id, c.name
ORDER BY facility_count DESC;

-- -----------------------------------------------------------
-- 5. 创建视图：课程受欢迎程度（基于教师授课数量）
-- -----------------------------------------------------------
CREATE OR REPLACE VIEW v_course_popularity AS
SELECT 
    co.course_id,
    co.name AS course_name,
    d.name AS department_name,
    co.credits,
    COUNT(DISTINCT ct.teacher_id) AS teacher_count,
    COUNT(ct.semester) AS total_semesters,
    GROUP_CONCAT(DISTINCT ct.semester ORDER BY ct.semester SEPARATOR ', ') AS offered_semesters
FROM course co
LEFT JOIN department d ON co.department_id = d.department_id
LEFT JOIN course_teacher ct ON co.course_id = ct.course_id
GROUP BY co.course_id, co.name, d.name, co.credits
ORDER BY teacher_count DESC, total_semesters DESC;

-- -----------------------------------------------------------
-- 6. 创建统计表：查询热点缓存（提升性能）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS query_statistics_cache (
    stat_id INT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL,
    category VARCHAR(50),
    query_count INT DEFAULT 0,
    unique_users INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date_category (stat_date, category),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 7. 创建存储过程：更新查询统计缓存
-- -----------------------------------------------------------
DELIMITER $$

CREATE PROCEDURE sp_update_query_statistics(IN p_date DATE)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_category VARCHAR(50);
    DECLARE v_count INT;
    DECLARE v_users INT;
    
    -- 游标：获取指定日期的各类别统计
    DECLARE cur_stats CURSOR FOR
        SELECT 
            category,
            COUNT(*) AS cnt,
            COUNT(DISTINCT user_id) AS usr_cnt
        FROM query_record
        WHERE DATE(query_time) = p_date
          AND category IS NOT NULL 
          AND category != ''
        GROUP BY category;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur_stats;
    
    read_loop: LOOP
        FETCH cur_stats INTO v_category, v_count, v_users;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 插入或更新统计数据
        INSERT INTO query_statistics_cache (stat_date, category, query_count, unique_users)
        VALUES (p_date, v_category, v_count, v_users)
        ON DUPLICATE KEY UPDATE
            query_count = v_count,
            unique_users = v_users;
    END LOOP;
    
    CLOSE cur_stats;
    
    SELECT CONCAT('Statistics updated for date: ', p_date) AS result;
END$$

DELIMITER ;

-- -----------------------------------------------------------
-- 8. 创建存储过程：获取个性化推荐（基于用户历史）
-- -----------------------------------------------------------
DELIMITER $$

CREATE PROCEDURE sp_get_personalized_recommendations(
    IN p_user_id INT,
    IN p_limit INT
)
BEGIN
    -- 基于用户最常查询的类别，推荐相关内容
    
    -- 首先获取用户最常查询的类别
    CREATE TEMPORARY TABLE IF NOT EXISTS temp_user_preferences AS
    SELECT 
        category,
        COUNT(*) AS pref_count
    FROM query_record
    WHERE user_id = p_user_id
      AND category IS NOT NULL
    GROUP BY category
    ORDER BY pref_count DESC
    LIMIT 3;
    
    -- 根据用户偏好推荐相关活动
    SELECT 
        'event' AS recommendation_type,
        e.event_id AS item_id,
        e.name AS item_name,
        e.event_time AS item_time,
        e.location_desc AS item_location,
        e.category AS item_category,
        '近期活动推荐' AS reason
    FROM event e
    WHERE e.category IN (SELECT category FROM temp_user_preferences)
      AND e.event_time >= NOW()
    ORDER BY e.event_time ASC
    LIMIT p_limit;
    
    DROP TEMPORARY TABLE IF EXISTS temp_user_preferences;
END$$

DELIMITER ;

-- -----------------------------------------------------------
-- 9. 创建触发器：自动更新查询统计（每次新增查询记录时）
-- -----------------------------------------------------------
DELIMITER $$

CREATE TRIGGER trg_after_query_insert
AFTER INSERT ON query_record
FOR EACH ROW
BEGIN
    -- 异步更新当天的统计数据（简化版，实际生产环境可用事件调度器）
    INSERT INTO query_statistics_cache (stat_date, category, query_count, unique_users)
    VALUES (
        DATE(NEW.query_time),
        NEW.category,
        1,
        1
    )
    ON DUPLICATE KEY UPDATE
        query_count = query_count + 1,
        unique_users = (
            SELECT COUNT(DISTINCT user_id) 
            FROM query_record 
            WHERE DATE(query_time) = DATE(NEW.query_time)
              AND category = NEW.category
        );
END$$

DELIMITER ;

-- -----------------------------------------------------------
-- 10. 创建事件：每天凌晨自动更新统计（需要开启事件调度器）
-- -----------------------------------------------------------
-- 注意：需要先执行 SET GLOBAL event_scheduler = ON;

DELIMITER $$

CREATE EVENT IF NOT EXISTS evt_daily_statistics_update
ON SCHEDULE EVERY 1 DAY
STARTS TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY, '00:00:00')
DO
BEGIN
    -- 更新昨天的统计数据
    CALL sp_update_query_statistics(DATE(NOW() - INTERVAL 1 DAY));
END$$

DELIMITER ;

-- -----------------------------------------------------------
-- 11. 额外索引优化（针对统计分析查询）
-- -----------------------------------------------------------
-- 如果索引不存在则创建
CREATE INDEX IF NOT EXISTS idx_query_category ON query_record(category);
CREATE INDEX IF NOT EXISTS idx_query_time_only ON query_record(query_time);
CREATE INDEX IF NOT EXISTS idx_event_category_time ON event(category, event_time);

-- -----------------------------------------------------------
-- 12. 示例查询：展示如何使用这些视图和存储过程
-- -----------------------------------------------------------

-- 查看热门查询类别
-- SELECT * FROM v_popular_query_categories LIMIT 10;

-- 查看最近7天的查询趋势
-- SELECT * FROM v_daily_query_trend WHERE query_date >= DATE(NOW() - INTERVAL 7 DAY);

-- 查看活跃用户TOP 10
-- SELECT * FROM v_active_users LIMIT 10;

-- 获取个性化推荐（用户ID=1，推荐5条）
-- CALL sp_get_personalized_recommendations(1, 5);

-- 手动更新今天的统计
-- CALL sp_update_query_statistics(CURDATE());
