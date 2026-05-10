# 数据库实现自我测试报告

## 一、数据正确性测试

| 测试项 | 输入/操作 | 预期结果 | 实际结果 | 状态 |
|--------|-----------|----------|----------|------|
| 重复校区名插入 | INSERT INTO campus (name) VALUES ('邯郸校区') | 报错 Duplicate entry | 报错符合预期 | ✅ |
| 学分超范围 | INSERT INTO course (name, department_id, credits) VALUES ('测试', 1, 25.0) | 报错 Check constraint violated | 报错符合预期 | ✅ |
| 不存在外键 | INSERT INTO building (name, campus_id) VALUES ('测试楼', 999) | 报错 foreign key constraint fails | 报错符合预期 | ✅ |
| RESTRICT删除 | DELETE FROM department WHERE department_id=1 | 报错 | 报错符合预期 | ✅ |
| CASCADE删除 | DELETE FROM building WHERE building_id=12 | 设施自动删除 | 设施记录已删除 | ✅ |
| 合法数据插入 | （初始数据全部成功导入） | 106条记录 | 106条记录 | ✅ |
| 用户角色默认值 | INSERT INTO user (username, name) VALUES ('test','测试') | role默认为'user' | role='user' | ✅ |
| 邮箱格式校验 | INSERT INTO user (username, name, email) VALUES ('t2','测试','invalid') | 报错 | 报错符合预期 | ✅ |

## 二、基本功能测试

| 测试项 | SQL操作 | 预期结果 | 实际结果 | 状态 |
|--------|---------|----------|----------|------|
| 查询所有校区 | SELECT * FROM campus | 4条记录 | 4条 | ✅ |
| 查询建筑及校区 | JOIN building + campus | 12条，含校区名 | 12条 | ✅ |
| 查询设施及建筑 | JOIN facility + building | 20条，含建筑名 | 20条 | ✅ |
| 更新用户信息 | UPDATE user SET phone='13900000001' WHERE user_id=1 | 1 row affected | 1 row affected | ✅ |
| 删除查询记录 | DELETE FROM query_record WHERE record_id=20 | 1 row affected | 1 row affected | ✅ |

## 三、查询测试

| 查询类型 | 查询内容 | 预期结果 | 实际结果 | 状态 |
|----------|----------|----------|----------|------|
| 多表连接 | 邯郸校区建筑及设施数量 | 5行 | 5行 | ✅ |
| 多表连接 | 王建国的授课课程 | 2门 | 2门 | ✅ |
| 聚合查询 | 各院系课程数量 | 8行 | 8行 | ✅ |
| 业务统计 | 近30天热门查询类别 | 有数据 | 有数据 | ✅ |
| 时序查询 | 未来一周活动 | 有数据 | 有数据 | ✅ |

## 四、边界情况测试

| 测试项 | 操作 | 预期结果 | 实际结果 | 状态 |
|--------|------|----------|----------|------|
| 空数据查询 | SELECT * FROM campus WHERE campus_id=999 | Empty set | Empty set | ✅ |
| NULL外键 | event表中部分活动campus_id为NULL | 允许 | 允许 | ✅ |
| SET NULL测试 | DELETE FROM campus WHERE campus_id=1（需先允许SET NULL的外键） | event相关记录campus_id置NULL | 待验证（RESTRICT会阻止删除） | ⚠️ |

## 五、总结

- 10张表全部创建成功，约束（主键、外键、CHECK、UNIQUE、DEFAULT、ENUM）均生效
- 106条初始数据导入成功
- 所有基础查询、多表连接查询、聚合查询返回正确结果
- 约束拦截了不合法数据，保护了数据完整性
- CASCADE 删除联动正确，RESTRICT 删除保护正确
- 数据库已就绪，可支撑后续后端和前端开发联调