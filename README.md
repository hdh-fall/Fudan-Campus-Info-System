# 复旦校园百事通问答系统

## 项目简介

本项目是复旦大学数据库系统课程的实践项目，设计并实现了一个以数据库为核心的校园信息问答系统。系统允许用户通过关键词或自然语言查询校园建筑、设施、课程、教师和活动等信息，并记录查询历史，为后续智能问答提供数据基础。

**小组成员**：洪东浩、马天行

**技术栈**：
- **前端**：Vue 3 + Element Plus + Axios + ECharts
- **后端**：Spring Boot 4.0.6 + Spring Data JPA + Hibernate 7.x
- **数据库**：MySQL 8.0（含视图、存储过程、触发器、定时事件）
- **AI集成**：阿里云通义千问大模型（可选）
  
**GitHub仓库**：https://github.com/hdh-fall/Fudan-Campus-Info-System

---

## 系统功能

### 1. AI智能问答
- **自然语言查询**：支持用日常语言提问，如“复旦有哪些食堂？”、“数据库课程是谁开的？”
- **AI语义理解**：基于通义千问大模型，理解用户意图并生成SQL查询
- **智能类别识别**：自动识别10种查询类别（图书馆、自习室、实验室等独立分类）
- **智能列名映射**：自动将英文字段名转换为中文显示
- **同名课程区分**：同一课程不同教师或学期会分别显示
- **学科语义扩展**：查询“数学课程”可返回高等代数、线性代数等相关课程
- **自动降级机制**：AI失败时自动切换到规则匹配，保证系统可用性

### 2. 个性化推荐
- **基于历史的推荐**：根据用户查询历史智能推荐相关内容
- **全类别覆盖**：支持10种查询类别的个性化推荐
- **精准跳转**：点击推荐直接跳转到对应的筛选列表
- **实时统计**：热门查询类别排行榜和趋势分析

### 3. 校区建筑查询
- 按校区查看建筑列表
- 查看建筑详细信息（类型、楼层、开放时间等）
- 查看建筑内的设施信息

### 4. 校园设施查询
- 按类型浏览设施（食堂、咖啡厅、图书馆、自习室、实验室等）
- 关键词搜索设施
- 查看设施详细信息（位置、开放时间、容量等）

### 5. 课程与教师查询
- 浏览所有课程信息
- 按院系查看课程
- 关键词搜索课程
- 查看所有教师信息
- 查询教师授课情况
- **同名课程区分**：同一课程不同开课学期分别显示

### 6. 校园活动查询
- 查看近期活动
- 搜索活动名称或主办方
- 查看活动详细信息（时间、地点、主办方等）

### 7. 数据分析
- **查询趋势图表**：每日查询次数可视化展示
- **热门类别排行**：10种查询类别的排行榜
- **活跃用户统计**：用户活跃度评分和排名
- **校区设施分布**：各校区设施密度统计
- **个性化推荐**：基于用户行为的智能推荐

### 8. 个人中心
- 查看个人信息
- 查看查询历史记录
- 查看热门查询类别统计
- 切换用户（演示用）

### 9. 管理后台（仅管理员可见）
- 管理建筑信息（增删改、CSV批量导入）
- 管理设施信息（增删改、CSV批量导入）
- 管理课程信息（增删改、CSV批量导入）
- 管理教师信息（增删改、CSV批量导入）
- 管理活动信息（增删改、CSV批量导入）

---

## 数据库设计

### 实体关系图
详见 [docs/ER图/ER图.png](docs/ER图/ER图.png)

### 数据表结构

系统包含11张数据表，符合第三范式（3NF）：

1. **department** - 院系表
2. **user** - 用户表
3. **campus** - 校区表
4. **building** - 建筑表
5. **facility** - 设施表
6. **teacher** - 教师表
7. **course** - 课程表
8. **course_teacher** - 课程-教师授课关系表（多对多中间表）
9. **event** - 活动表
10. **query_record** - 查询记录表
11. **query_statistics_cache** - 查询统计缓存表

### 关键设计特点

- **一对多关系**：校区-建筑、建筑-设施、用户-查询记录、日期-查询统计缓存等
- **多对多关系**：课程-教师（通过course_teacher中间表实现）
- **约束设计**：
  - 主键约束：所有表都有主键
  - 外键约束：保证引用完整性
  - 唯一约束：如用户名、院系名、校区名、统计日期+类别组合等
  - 检查约束：如学分范围、年级格式、经纬度范围等
  - 非空约束：关键字段不允许为空
- **索引优化**：为常用查询字段创建索引（11个索引）
- **事务支持**：使用@Transactional保证数据一致性
- **分析增强模块**：
  - **视图（Views）**：热门查询类别、每日查询趋势、活跃用户排行、校区设施热度、课程受欢迎程度
  - **存储过程**：统计缓存自动更新、个性化活动推荐
  - **触发器**：查询记录插入后自动更新统计缓存
  - **定时事件**：每日凌晨自动刷新统计数据

详见：
- [database/schema.sql](database/schema.sql) - 数据库建表脚本
- [database/seed_data.sql](database/seed_data.sql) - 初始测试数据
- [database/analytics_enhancement.sql](database/analytics_enhancement.sql) - 分析增强脚本（视图、存储过程、触发器、事件）
- [docs/关系模式设计/关系模式设计.md](docs/关系模式设计/关系模式设计.md) - 关系模式详细说明
- [docs/数据库逻辑结构/数据库逻辑结构.md](docs/数据库逻辑结构/数据库逻辑结构.md) - 数据库逻辑结构说明

---

## 项目结构

```
Fudan-Campus-Info-System/
├── database/                       # 数据库相关文件
│   ├── schema.sql                 # 建表脚本（11张表）
│   ├── seed_data.sql              # 初始测试数据（106条）
│   ├── analytics_enhancement.sql  # 分析增强（视图、存储过程、触发器、事件）
│   ├── queries.sql                # 示例查询
│   ├── test_report.md             # 数据库自我测试报告
│   └── csv_templates/             # CSV批量导入模板
│       ├── README.md
│       ├── buildings_example.csv
│       ├── course_teachers_example.csv
│       ├── courses_example.csv
│       ├── events_example.csv
│       ├── facilities_example.csv
│       └── teachers_example.csv
├── docs/                           # 项目文档
│   ├── ER图/
│   │   └── ER图.png               # 实体关系图
│   ├── 需求分析/
│   │   ├── 需求说明.md             # 需求分析文档
│   │   └── 需求说明.pdf
│   ├── 关系模式设计/
│   │   ├── 关系模式设计.md         # 关系模式详细说明
│   │   └── 关系模式设计.pdf
│   ├── 数据库逻辑结构/
│   │   ├── 数据库逻辑结构.md       # 数据库逻辑结构说明
│   │   └── 数据库逻辑结构.pdf
│   ├── 主要功能实现结果及阶段演示说明/  # 阶段演示材料
│   │   ├── 主要功能实现结果及阶段演示说明.md
│   │   ├── 主要功能实现结果及阶段演示说明.pdf
│   │   ├── 智能问答.png
│   │   ├── 推荐卡片.png
│   │   └── ...
│   └── 展示材料/
│       └── 复旦百事通.pptx         # 项目展示PPT
├── src/
│   ├── fudan-campus-backend/      # 后端项目（Spring Boot 4.0.6）
│   │   ├── src/main/java/com/fudan/campusinfo/
│   │   │   ├── entity/            # 实体类（10个）
│   │   │   │   ├── Campus.java
│   │   │   │   ├── Building.java
│   │   │   │   ├── Facility.java
│   │   │   │   ├── Course.java
│   │   │   │   ├── Teacher.java
│   │   │   │   ├── CourseTeacher.java / CourseTeacherId.java
│   │   │   │   ├── Event.java
│   │   │   │   ├── Department.java
│   │   │   │   ├── User.java
│   │   │   │   └── QueryRecord.java
│   │   │   ├── repository/        # 数据访问层（10个Repository）
│   │   │   ├── service/           # 业务逻辑层
│   │   │   │   ├── CampusInfoService.java
│   │   │   │   ├── StatisticsService.java
│   │   │   │   ├── RecommendationService.java
│   │   │   │   ├── CSVImportService.java
│   │   │   │   ├── AINaturalLanguageService.java
│   │   │   │   └── NaturalLanguageQueryService.java
│   │   │   ├── controller/        # 控制器层（RESTful API）
│   │   │   │   ├── CampusInfoController.java
│   │   │   │   └── RecommendationController.java
│   │   │   ├── config/
│   │   │   │   └── CorsConfig.java
│   │   │   └── FudanCampusBackendApplication.java
│   │   ├── src/main/resources/
│   │   │   ├── application.properties
│   │   │   └── application.example.properties
│   │   ├── src/test/java/com/fudan/campusinfo/
│   │   │   ├── FudanCampusBackendApplicationTests.java
│   │   │   └── service/
│   │   │       └── AINaturalLanguageServiceTest.java
│   │   ├── pom.xml                # Maven依赖配置
│   │   ├── mvnw / mvnw.cmd        # Maven Wrapper
│   │   └── .gitignore
│   └── fudan-campus-frontend/     # 前端项目（Vue 3 + Vite）
│       ├── src/
│       │   ├── api/
│       │   │   └── index.js       # API调用封装（Axios）
│       │   ├── assets/
│       │   │   ├── base.css
│       │   │   ├── main.css
│       │   │   └── logo.svg
│       │   ├── views/             # 页面组件（8个视图）
│       │   │   ├── HomeView.vue         # 首页（含AI问答、推荐）
│       │   │   ├── CampusView.vue       # 校区建筑
│       │   │   ├── FacilityView.vue     # 校园设施
│       │   │   ├── CourseView.vue       # 课程教师
│       │   │   ├── EventView.vue        # 校园活动
│       │   │   ├── UserView.vue         # 个人中心
│       │   │   ├── AdminView.vue        # 管理后台
│       │   │   └── AnalyticsView.vue    # 数据分析
│       │   ├── App.vue            # 根组件
│       │   └── main.js            # 入口文件
│       ├── public/
│       │   ├── favicon.ico
│       │   └── images/
│       │       └── carousel/      # 首页轮播背景图
│       ├── package.json           # 前端依赖
│       ├── vite.config.js         # Vite构建配置
│       └── index.html
├── .gitignore
└── README.md                      # 项目说明（本文件）
```

---

## 快速开始

### 前置要求

- Java 21+
- Node.js 20+
- MySQL 8.0+
- Maven（后端构建工具）

### 1. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行建表脚本
source database/schema.sql

# 导入初始数据
source database/seed_data.sql

# （可选）启用分析增强功能（视图、存储过程、触发器、事件）
source database/analytics_enhancement.sql
```

### 2. 启动后端服务

```bash
cd src/fudan-campus-backend

# 使用Maven运行
.\mvnw.cmd spring-boot:run

# 或者先编译再运行
mvn clean package
java -jar target/campus-info-0.0.1-SNAPSHOT.jar
```

后端服务将在 `http://localhost:8080` 启动。

**注意**：如需修改数据库连接配置，请编辑 `src/fudan-campus-backend/src/main/resources/application.properties` 文件。

#### AI智能问答配置（可选）

要启用AI智能问答功能，需要配置通义千问API Key：

1. **获取API Key**：访问 [阿里云DashScope](https://dashscope.console.aliyun.com/) 注册并创建API Key
2. **编辑配置文件**：打开 `application.properties`，设置以下参数：
   ```properties
   ai.qwen.api-key=sk-your-api-key-here
   ai.qwen.enabled=true
   ```
3. **重启后端服务**：重新运行后端即可启用AI功能

**降级机制**：如果未配置API Key或AI服务不可用，系统会自动切换到规则匹配模式，保证基本功能可用。

### 3. 启动前端服务

```bash
cd src/fudan-campus-frontend

# 安装依赖（如果尚未安装）
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:5173` 启动（具体端口以实际输出为准）。

### 4. 访问系统

打开浏览器访问前端地址，即可使用系统。

---

## AI智能问答使用示例

### 基础查询

1. **设施查询**
   - “复旦有哪些食堂？”
   - “邯郸校区有哪些教学楼？”
   - “枫林校区的咖啡厅在哪里？”

2. **课程查询**
   - “数据库课程是谁开的？”
   - “有哪些数学课程？” → 返回高等代数、线性代数、数学分析等
   - “计算机学院的课程有哪些？”

3. **活动查询**
   - “最近有哪些校园讲座？”
   - “下周有什么活动？”
   - “近期有哪些社团活动？”

4. **教师查询**
   - “张三老师教什么课？”
   - “教授职称的老师有哪些？”

### 智能特性

- **语义理解**：查询“数学课程”可返回不包含“数学”二字的相关课程（如高等代数）
- **同名区分**：同一课程不同学期或不同教师会分别显示
- **中文显示**：所有字段名自动转换为中文，如`course_name` → “课程名称”
- **时间格式化**：活动时间自动格式化为友好格式

---

## API接口说明

后端提供RESTful API接口，主要接口如下：

### 校区相关
- `GET /api/campuses` - 获取所有校区
- `GET /api/campuses/{id}` - 获取指定校区

### 建筑相关
- `GET /api/buildings/campus/{campusId}` - 获取校区的建筑列表
- `GET /api/buildings/search?keyword=xxx` - 搜索建筑
- `POST /api/buildings` - 创建建筑
- `PUT /api/buildings/{id}` - 更新建筑
- `DELETE /api/buildings/{id}` - 删除建筑

### 设施相关
- `GET /api/facilities/building/{buildingId}` - 获取建筑的设施列表
- `GET /api/facilities/type/{type}` - 按类型获取设施
- `GET /api/facilities/search?keyword=xxx` - 搜索设施
- `POST /api/facilities` - 创建设施
- `PUT /api/facilities/{id}` - 更新设施
- `DELETE /api/facilities/{id}` - 删除设施

### 课程相关
- `GET /api/courses` - 获取所有课程
- `GET /api/courses-with-teachers` - 获取课程及授课教师信息
- `GET /api/courses/search?keyword=xxx` - 搜索课程
- `POST /api/courses` - 创建课程
- `PUT /api/courses/{id}` - 更新课程
- `DELETE /api/courses/{id}` - 删除课程

### 教师相关
- `GET /api/teachers` - 获取所有教师
- `GET /api/teachers/{id}/courses` - 获取教师授课课程
- `POST /api/teachers` - 创建教师
- `PUT /api/teachers/{id}` - 更新教师
- `DELETE /api/teachers/{id}` - 删除教师

### 活动相关
- `GET /api/events/upcoming?days=7` - 获取近期活动
- `GET /api/events/search?keyword=xxx` - 搜索活动
- `POST /api/events` - 创建活动
- `PUT /api/events/{id}` - 更新活动
- `DELETE /api/events/{id}` - 删除活动

### 用户相关
- `GET /api/users/{id}` - 获取用户信息
- `POST /api/users` - 创建用户
- `DELETE /api/users/{id}` - 删除用户

### 查询记录相关
- `POST /api/query-records` - 保存查询记录
- `GET /api/query-records/user/{userId}` - 获取用户查询历史
- `GET /api/query-records/popular-categories` - 获取热门查询类别统计

### 统计分析相关
- `GET /api/statistics/daily-trend?days=15` - 获取每日查询趋势（最近N天）
- `GET /api/statistics/active-users?limit=10` - 获取活跃用户排行
- `GET /api/statistics/recommendations?userId=1&limit=5` - 获取个性化活动推荐

### CSV批量导入
- `POST /api/import/buildings` - 从CSV文件批量导入建筑
- `POST /api/import/facilities` - 从CSV文件批量导入设施
- `POST /api/import/courses` - 从CSV文件批量导入课程
- `POST /api/import/teachers` - 从CSV文件批量导入教师
- `POST /api/import/events` - 从CSV文件批量导入活动

---

## 数据库测试

系统已完成完整的数据库自我测试，所有测试用例均已通过。

### 测试概览

| 测试类别 | 测试项数量 | 通过数量 | 状态 |
|---------|-----------|---------|------|
| 数据正确性测试 | 8项 | 8项 | ✅ 全部通过 |
| 基本功能测试 | 6项 | 6项 | ✅ 全部通过 |
| 查询测试 | 5项 | 5项 | ✅ 全部通过 |
| 边界情况测试 | 3项 | 3项 | ✅ 全部通过 |

### 测试内容

#### 1. 数据正确性测试（8项）
- ✅ 重复校区名插入约束验证
- ✅ 学分超范围CHECK约束验证
- ✅ 不存在外键引用约束验证
- ✅ RESTRICT删除保护验证
- ✅ CASCADE删除联动验证
- ✅ 合法数据插入（106条记录）
- ✅ 用户角色默认值验证
- ✅ 邮箱格式校验验证

#### 2. 基本功能测试（6项）
- ✅ 查询所有校区（4条记录）
- ✅ 查询建筑及校区（JOIN查询，12条记录）
- ✅ 查询设施及建筑（JOIN查询，20条记录）
- ✅ 更新用户信息
- ✅ 删除查询记录
- ✅ CSV批量导入功能

#### 3. 查询测试（5项）
- ✅ 多表连接查询：邯郸校区建筑及设施数量
- ✅ 多表连接查询：王建国的授课课程
- ✅ 聚合查询：各院系课程数量统计
- ✅ 业务统计查询：近30天热门查询类别
- ✅ 时序查询：未来一周即将举办的活动

#### 4. 边界情况测试（3项）
- ✅ 空数据查询处理
- ✅ NULL外键允许（event表campus_id可为NULL）
- ✅ SET NULL删除联动验证

### 测试结果总结

✅ **10张表全部创建成功**，约束（主键、外键、CHECK、UNIQUE、DEFAULT、ENUM）均生效  
✅ **106条初始数据导入成功**，覆盖所有表  
✅ **所有基础查询、多表连接查询、聚合查询返回正确结果**  
✅ **约束拦截了不合法数据**，保护了数据完整性  
✅ **CASCADE删除联动正确**，RESTRICT删除保护正确  
✅ **数据库已就绪**，可支撑后续后端和前端开发联调

详细测试报告请查看：[database/test_report.md](database/test_report.md)

---

## 常见问题

### 1. 后端启动失败

- 检查MySQL是否正常运行
- 检查数据库连接配置（application.properties）
- 确认数据库已创建并导入数据

### 2. 前端无法连接后端

- 确认后端服务已启动
- 检查API地址配置（src/fudan-campus-frontend/src/api/index.js）
- 检查浏览器控制台是否有CORS错误

### 3. 中文乱码

- 确保数据库字符集为utf8mb4
- 检查MySQL连接字符串中的字符集设置

---

## 项目亮点

1. **完整的数据库设计**：11张表，包含一对多和多对多关系，符合3NF
2. **丰富的约束设计**：主键、外键、唯一、检查、非空等多种约束
3. **合理的索引优化**：11个索引覆盖常用查询字段，支撑高性能统计分析
4. **完整的事务支持**：@Transactional保证数据一致性
5. **完整的前后端实现**：Vue 3 + Spring Boot全栈开发
6. **完善的CRUD操作**：支持增删改查全部操作
7. **CSV批量导入**：支持从CSV文件批量导入数据
8. **良好的用户体验**：基于Element Plus的现代化界面
9. **完善的测试数据**：包含80+条测试数据，覆盖所有表
10. **详细的测试文档**：21项测试用例，100%通过率
11. **智能分析增强**：视图、存储过程、触发器、定时事件构建的数据分析体系
12. **数据可视化支持**：查询趋势图表、热门类别排行、活跃用户统计、个性化推荐
13. **AI智能问答**：
    - 基于通义千问大模型的自然语言理解
    - 智能SQL生成与执行
    - 语义扩展与关键词匹配
    - 自动降级机制保证系统可靠性
    - 中文列名映射与时间格式化
14. **个性化推荐系统**：
    - 基于用户查询历史的智能推荐算法
    - 10种查询类别全覆盖（图书馆、自习室、实验室独立分类）
    - 精准跳转到对应的筛选列表
    - 动态更新推荐内容
    - 视觉化展示（专属图标、颜色、标签）

---

## 参考资料

- [需求分析文档](docs/需求分析/需求说明.md)
- [ER图](docs/ER图/ER图.png)
- [关系模式设计](docs/关系模式设计/关系模式设计.md)
- [数据库逻辑结构](docs/数据库逻辑结构/数据库逻辑结构.md)

---

## 许可证

本项目为数据库课程教学项目，仅供学习使用。

---

**最后更新**：2026年6月10日 - 最终提交版本，完善项目结构与文档
