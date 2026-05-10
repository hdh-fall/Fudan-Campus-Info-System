-- 强制会话字符集，避免客户端编码不一致导致中文报错
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

USE fudan_campus_info;

-- 1. 邯郸校区所有建筑及其设施数量（多表连接）
SELECT b.name AS building, COUNT(f.facility_id) AS facility_count
FROM building b
LEFT JOIN facility f ON b.building_id = f.building_id
JOIN campus c ON b.campus_id = c.campus_id
WHERE c.name = '邯郸校区'
GROUP BY b.building_id;

-- 2. 各院系课程数量（聚合查询）
SELECT d.name AS department, COUNT(c.course_id) AS course_count
FROM department d
LEFT JOIN course c ON d.department_id = c.department_id
GROUP BY d.department_id;

-- 3. 王建国老师的所有授课课程（多表连接）
SELECT t.name AS teacher, c.name AS course, ct.semester, ct.role
FROM teacher t
JOIN course_teacher ct ON t.teacher_id = ct.teacher_id
JOIN course c ON ct.course_id = c.course_id
WHERE t.teacher_id = 1;

-- 4. 近30天热门查询类别（业务统计）
SELECT category, COUNT(*) AS query_count
FROM query_record
WHERE query_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY category
ORDER BY query_count DESC
LIMIT 10;

-- 5. 未来一周即将举办的活动
SELECT name, event_time, location_desc, organizer
FROM event
WHERE event_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY)
ORDER BY event_time;

-- 6. 约束验证：尝试插入无效数据（应被拒绝）
-- 该语句应当失败：学分超出范围
INSERT INTO course (name, department_id, credits) VALUES ('测试课程', 1, 25.0);
-- 该语句应当失败：年级格式错误
INSERT INTO user (username, name, grade) VALUES ('testuser', '测试', '大二');
-- 该语句应当失败：引用不存在的外键
INSERT INTO building (name, campus_id) VALUES ('幽灵楼', 999);

-- 各表记录数确认
SELECT 'department' AS table_name, COUNT(*) AS cnt FROM department
UNION ALL SELECT 'user', COUNT(*) FROM user
UNION ALL SELECT 'campus', COUNT(*) FROM campus
UNION ALL SELECT 'building', COUNT(*) FROM building
UNION ALL SELECT 'facility', COUNT(*) FROM facility
UNION ALL SELECT 'teacher', COUNT(*) FROM teacher
UNION ALL SELECT 'course', COUNT(*) FROM course
UNION ALL SELECT 'course_teacher', COUNT(*) FROM course_teacher
UNION ALL SELECT 'event', COUNT(*) FROM event
UNION ALL SELECT 'query_record', COUNT(*) FROM query_record;