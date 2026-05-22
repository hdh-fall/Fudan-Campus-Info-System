# 复旦校园百事通问答系统

## 项目简介

本项目是复旦大学数据库系统课程的实践项目，设计并实现了一个以数据库为核心的校园信息问答系统。系统允许用户通过关键词查询校园建筑、设施、课程、教师和活动等信息，并记录查询历史，为后续智能问答提供数据基础。

**小组成员**：洪东浩、马天行

**技术栈**：
- **前端**：Vue 3 + Element Plus + Axios
- **后端**：Spring Boot 4.0.6 + Spring Data JPA
- **数据库**：MySQL 8.0

---

## 系统功能

### 1. 🤖 AI智能问答（新功能）
- **自然语言查询**：支持用日常语言提问，如“复旦有哪些食堂？”、“数据库课程是谁开的？”
- **AI语义理解**：基于通义千问大模型，理解用户意图并生成SQL查询
- **智能列名映射**：自动将英文字段名转换为中文显示
- **同名课程区分**：同一课程不同教师或学期会分别显示
- **学科语义扩展**：查询“数学课程”可返回高等代数、线性代数等相关课程
- **自动降级机制**：AI失败时自动切换到规则匹配，保证系统可用性

### 2. 校区建筑查询
- 按校区查看建筑列表
- 查看建筑详细信息（类型、楼层、开放时间等）
- 查看建筑内的设施信息

### 3. 校园设施查询
- 按类型浏览设施（食堂、咖啡厅、图书馆、自习室、实验室等）
- 关键词搜索设施
- 查看设施详细信息（位置、开放时间、容量等）

### 4. 课程与教师查询
- 浏览所有课程信息
- 按院系查看课程
- 关键词搜索课程
- 查看所有教师信息
- 查询教师授课情况
- **同名课程区分**：同一课程不同开课学期分别显示

### 5. 校园活动查询
- 查看近期活动
- 搜索活动名称或主办方
- 查看活动详细信息（时间、地点、主办方等）

### 6. 个人中心
- 查看个人信息
- 查看查询历史记录
- 查看热门查询类别统计
- 切换用户（演示用）

### 7. 管理后台（仅管理员可见）
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

系统包含11张数据表（含分析增强统计表），符合第三范式（3NF）：

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
11. **query_statistics_cache** - 查询统计缓存表（分析增强模块）

### 关键设计特点

- **一对多关系**：校区-建筑、建筑-设施、用户-查询记录、日期-查询统计缓存等
- **多对多关系**：课程-教师（通过course_teacher中间表实现）
- **约束设计**：
  - 主键约束：所有表都有主键
  - 外键约束：保证引用完整性
  - 唯一约束：如用户名、院系名、校区名、统计日期+类别组合等
  - 检查约束：如学分范围、年级格式、经纬度范围等
  - 非空约束：关键字段不允许为空
- **索引优化**：为常用查询字段创建索引（11个索引，含分析增强索引）
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
├── database/                    # 数据库相关文件
│   ├── schema.sql              # 建表脚本
│   ├── seed_data.sql           # 初始数据
│   ├── analytics_enhancement.sql  # 分析增强脚本（视图、存储过程、触发器、事件）
│   └── queries.sql             # 示例查询
├── docs/                        # 文档
│   ├── ER图/                   # ER图
│   ├── 关系模式设计/            # 关系模式说明
│   ├── 数据库逻辑结构/          # 数据库逻辑结构
│   └── 需求分析/               # 需求分析文档
├── src/
│   ├── fudan-campus-backend/   # 后端项目（Spring Boot）
│   │   ├── src/main/java/com/fudan/campusinfo/
│   │   │   ├── entity/         # 实体类
│   │   │   ├── repository/     # 数据访问层
│   │   │   ├── service/        # 业务逻辑层
│   │   │   ├── controller/     # 控制器层
│   │   │   └── config/         # 配置类
│   │   └── src/main/resources/
│   │       └── application.properties  # 配置文件
│   └── fudan-campus-frontend/  # 前端项目（Vue 3）
│       ├── src/
│       │   ├── api/            # API调用模块
│       │   ├── views/          # 页面组件
│       │   └── App.vue         # 主应用组件
│       └── package.json        # 前端依赖
└── README.md                   # 项目说明
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

**免费额度**：新用户可获得100万token的免费额度，足够日常使用。

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

详见：
- [AI_INTEGRATION_GUIDE.md](AI_INTEGRATION_GUIDE.md) - AI集成详细指南
- [QUICK_START_AI.md](QUICK_START_AI.md) - AI功能快速启动

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

## 示例查询

系统设计了多种SQL查询，详见 [database/queries.sql](database/queries.sql)：

1. **多表连接查询**：邯郸校区所有建筑及其设施数量
2. **聚合查询**：各院系课程数量统计
3. **多表连接查询**：王建国老师的所有授课课程
4. **业务统计查询**：近30天热门查询类别
5. **条件查询**：未来一周即将举办的活动
6. **约束验证**：测试数据约束是否生效

---

## 自我测试

### 1. 数据正确性测试

**测试1：插入合法数据**
```sql
-- 应该成功
INSERT INTO campus (name, address) VALUES ('测试校区', '测试地址');
```

**测试2：插入违反约束的数据**
```sql
-- 应该失败：学分超出范围
INSERT INTO course (name, department_id, credits) VALUES ('测试课程', 1, 25.0);

-- 应该失败：年级格式错误
INSERT INTO user (username, name, grade) VALUES ('testuser', '测试', '大二');

-- 应该失败：引用不存在的外键
INSERT INTO building (name, campus_id) VALUES ('幽灵楼', 999);
```

### 2. 基本功能测试

- **新增**：在管理后台添加建筑、设施、课程等
- **查询**：在各页面搜索和浏览数据
- **修改**：点击“编辑”按钮修改数据名称
- **删除**：在管理后台删除数据

详见 [TESTING.md](TESTING.md) - 完整的自我测试文档

### 3. 查询测试

- 多表连接查询是否正确返回结果
- 聚合查询统计数据是否准确
- 搜索功能是否能找到相关数据

### 4. 边界情况测试

- 空数据情况下的显示
- 重复输入的处理
- 非法外键引用的处理
- 删除关联数据时的表现（CASCADE/RESTRICT）

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
13. **🤖 AI智能问答**：
    - 基于通义千问大模型的自然语言理解
    - 智能SQL生成与执行
    - 语义扩展与关键词匹配
    - 自动降级机制保证系统可靠性
    - 中文列名映射与时间格式化

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

**最后更新**：2026年5月21日 - 新增AI智能问答功能
