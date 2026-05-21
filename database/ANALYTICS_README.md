# 智能查询分析与趋势统计功能说明

## 📊 功能概述

这是一个基于数据库高级特性的智能分析系统，展示了数据库在数据统计、趋势分析和个性化推荐方面的强大能力。

## ✨ 核心特性

### 1. **数据库视图（Views）**
- `v_popular_query_categories` - 热门查询类别统计
- `v_daily_query_trend` - 每日查询趋势
- `v_active_users` - 活跃用户排行
- `v_campus_facility_popularity` - 校区设施热度
- `v_course_popularity` - 课程受欢迎程度

### 2. **存储过程（Stored Procedures）**
- `sp_update_query_statistics` - 更新查询统计缓存
- `sp_get_personalized_recommendations` - 获取个性化推荐

### 3. **触发器（Triggers）**
- `trg_after_query_insert` - 自动更新统计数据

### 4. **事件调度器（Event Scheduler）**
- `evt_daily_statistics_update` - 每日自动更新统计

### 5. **索引优化**
- 针对统计分析查询的专用索引
- 复合索引提升多表连接性能

## 🚀 部署步骤

### 1. 执行数据库增强脚本

```bash
mysql -u root -p fudan_campus_info < database/analytics_enhancement.sql
```

### 2. 开启MySQL事件调度器

```sql
SET GLOBAL event_scheduler = ON;
```

可以在 MySQL 配置文件 (`my.cnf` 或 `my.ini`) 中永久启用：

```ini
[mysqld]
event_scheduler=ON
```

### 3. 安装前端图表库

```bash
cd src/fudan-campus-frontend
npm install echarts
```

### 4. 重启后端服务

确保Spring Boot应用已启动，新的API接口会自动注册。

### 5. 访问分析页面

在前端路由中添加AnalyticsView组件，访问 `/analytics` 路径即可查看。

## 📈 展示亮点

### 数据库技术展示点

1. **复杂聚合查询**
   - 多表JOIN统计
   - GROUP BY分组聚合
   - 窗口函数应用

2. **视图的优势**
   - 简化复杂查询
   - 提高代码复用性
   - 数据抽象层

3. **存储过程的威力**
   - 业务逻辑封装
   - 减少网络传输
   - 事务控制

4. **触发器的自动化**
   - 实时数据同步
   - 数据一致性保证
   - 自动维护缓存

5. **索引优化策略**
   - 覆盖索引
   - 复合索引
   - 查询性能对比

### 演示建议

1. **先展示基础数据**
   - 展示query_record表的原始数据
   
2. **然后展示视图**
   ```sql
   SELECT * FROM v_popular_query_categories;
   SELECT * FROM v_daily_query_trend LIMIT 7;
   ```

3. **演示存储过程**
   ```sql
   CALL sp_update_query_statistics(CURDATE());
   CALL sp_get_personalized_recommendations(1, 5);
   ```

4. **展示触发器效果**
   - 插入一条新查询记录
   - 立即查看统计表自动更新

5. **前端可视化**
   - 展示ECharts图表
   - 实时数据更新效果
   - 交互式数据探索

## 🎯 加分点说明

### 为什么这个功能能加分？

1. **数据库深度应用**
   - 不只是简单的CRUD
   - 展示了数据库的高级特性
   - 体现了对数据库原理的理解

2. **实际业务价值**
   - 帮助用户了解系统使用情况
   - 为决策提供数据支持
   - 提升用户体验

3. **技术完整性**
   - 前后端完整实现
   - 数据库优化考虑
   - 可视化展示

4. **可扩展性**
   - 易于添加新的统计维度
   - 支持自定义时间范围
   - 可集成机器学习推荐

## 📝 相关SQL示例

### 查看热门查询类别
```sql
SELECT * FROM v_popular_query_categories LIMIT 10;
```

### 查看最近7天趋势
```sql
SELECT * FROM v_daily_query_trend 
WHERE query_date >= DATE(NOW() - INTERVAL 7 DAY);
```

### 查看活跃用户TOP 10
```sql
SELECT * FROM v_active_users LIMIT 10;
```

### 手动更新统计
```sql
CALL sp_update_query_statistics(CURDATE());
```

### 获取个性化推荐
```sql
CALL sp_get_personalized_recommendations(1, 5);
```

## 🔧 故障排查

### 问题1：事件调度器未启动
```sql
-- 检查状态
SHOW VARIABLES LIKE 'event_scheduler';

-- 启动
SET GLOBAL event_scheduler = ON;
```

### 问题2：视图查询慢
```sql
-- 检查索引
EXPLAIN SELECT * FROM v_daily_query_trend;

-- 添加索引
CREATE INDEX idx_query_time ON query_record(query_time);
```

### 问题3：存储过程执行失败
```sql
-- 检查delimiter设置
-- 确保使用 DELIMITER $$ ... $$ DELIMITER ;

-- 删除重建
DROP PROCEDURE IF EXISTS sp_update_query_statistics;
```

## 📚 学习资源

- MySQL官方文档 - Views
- MySQL官方文档 - Stored Procedures
- MySQL官方文档 - Triggers
- MySQL官方文档 - Event Scheduler
- ECharts官方文档

---

**提示**: 在课程项目答辩时，重点强调数据库设计思想和优化策略，而不仅仅是功能展示。
