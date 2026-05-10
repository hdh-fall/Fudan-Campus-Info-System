# 复旦校园百事通问答系统

> 数据库系统课程项目 | 复旦大学

## 项目简介

复旦校园百事通是一个以数据库为核心的校园信息问答系统。用户可通过关键词或自然语言查询校区、建筑、设施、课程、活动等信息，系统记录查询历史并支持热门统计。管理员可进行基础数据管理与批量导入。

## 主要功能

- 用户信息查看与身份切换（普通用户/管理员）
- 校园信息搜索与浏览（建筑、设施、课程、活动）
- 查询历史记录与热门统计
- 管理员后台数据管理（增删改、CSV 批量导入）

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 |（待定） |
| 后端 |（待定） |
| 数据库 | MySQL 8.0+ |

## 数据库设计概况

- 表数量：10 张（含实体表、关系表）
- 包含一对多关系（校区→建筑、建筑→设施、用户→查询记录）
- 包含多对多关系（课程↔教师，通过中间表 `course_teacher` 实现）
- 满足第三范式（3NF），已做规范化分析
- 数据库名：`fudan_campus_info`

### 核心表一览

| 序号 | 表名 | 说明 |
|------|------|------|
| 1 | department | 院系信息 |
| 2 | user | 用户信息（普通用户/管理员） |
| 3 | campus | 校区信息 |
| 4 | building | 建筑信息，属于校区 |
| 5 | facility | 设施信息，位于建筑内 |
| 6 | teacher | 教师信息，所属院系 |
| 7 | course | 课程信息，开课院系 |
| 8 | course_teacher | 课程与教师多对多授课关系 |
| 9 | event | 校园活动信息 |
| 10 | query_record | 用户查询记录 |

### 约束设计要点

- **主键策略**：实体表采用单列自增整数，关系表采用复合主键
- **外键与参照完整性**：RESTRICT（7处）防止核心数据误删；CASCADE（5处）自动清理强依赖子记录；SET NULL（2处）保留活动记录同时标识关联失效
- **业务规则约束**：NOT NULL、UNIQUE、CHECK、DEFAULT、ENUM 等
- **索引**：为外键及高频查询字段建立索引，加速多表 JOIN 与聚合统计

## 仓库目录结构

```
/
├── README.md
├── database/
│   ├── schema.sql           # 建表DDL（含约束与索引）
│   ├── seed_data.sql        # 初始测试数据（106条）
│   ├── queries.sql          # 核心查询验证脚本
│   └── test_report.md       # 自我测试记录
├── docs/
│   ├── 需求分析/          
│   │   ├── 需求说明.pdf
│   │   └── 需求说明.md
│   ├── 关系模式设计/      
│   │   ├── 关系模式设计.pdf
│   │   └── 关系模式设计.md
│   ├── 数据库逻辑结构/      
│   │   ├── 数据库逻辑结构.pdf
│   │   └── 数据库逻辑结构.md
│   └── ER图/
│       └── ER图.png
└── src/                     # 前后端代码（开发中）
```

## 当前进度

- ✅ 第一阶段：需求分析与概念设计（已完成）
- ✅ 第二阶段：关系模式设计（已完成）
- ✅ 第三阶段：数据库逻辑结构定稿（已完成）
- ✅ 第四阶段：数据库实现 （已完成）
- ⏳ 第五阶段：系统核心功能开发
- ⏳ 第六阶段：最终完善

## 数据库部署

### 环境要求

- MySQL 8.0 或更高版本
- 字符集：utf8mb4

### 部署步骤

1. 克隆仓库到本地：
   ```bash
   git clone https://github.com/hdh-fall/Fudan-Campus-Info-System.git
   cd Fudan-Campus-Info-System
   ```

2. 执行建表脚本（创建数据库及全部10张表，含约束和索引）：
   ```bash
   mysql -u root -p < database/schema.sql
   ```

3. 导入初始测试数据（106条记录，覆盖所有表）：
   ```bash
   mysql -u root -p < database/seed_data.sql
   ```

4. 运行验证查询（确认数据正确性和查询功能）：
   ```bash
   mysql -u root -p fudan_campus_info < database/queries.sql
   ```

### 快速验证

部署完成后，可通过以下查询快速确认数据导入情况：

```sql
USE fudan_campus_info;

-- 查看各表记录数（预期合计 106 条）
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
```

预期输出：

| table_name | cnt |
|------------|-----|
| department | 8 |
| user | 10 |
| campus | 4 |
| building | 12 |
| facility | 20 |
| teacher | 12 |
| course | 12 |
| course_teacher | 20 |
| event | 10 |
| query_record | 20 |

## 小组信息

- 洪东浩
- 马天行

## 运行方式

> 后续完成系统开发后补充前后端启动命令与环境配置说明。