# CSV批量导入使用说明

## 概述

系统支持通过CSV文件批量导入数据，包括建筑、设施、课程、教师、课程-教师关联和活动等信息。

## 使用方法

### 方式一：通过管理后台界面导入

1. 登录系统，进入"管理后台"页面
2. 选择对应的管理标签页（如"建筑管理"）
3. 点击"CSV导入"按钮
4. 选择符合格式要求的CSV文件
5. 系统会自动上传并导入数据
6. 导入成功后会显示导入的记录数量

### 方式二：通过API接口导入

使用curl或其他HTTP客户端工具调用API接口：

```bash
curl -X POST http://localhost:8080/api/import/buildings \
  -F "file=@buildings.csv"
```

## CSV文件格式要求

### 通用要求

- 文件编码：**UTF-8**
- 第一行必须是表头（列名）
- 使用逗号（`,`）作为分隔符
- 空值可以留空或用空字符串表示

### 1. 建筑（buildings）

**文件名示例**: `buildings_example.csv`

**列定义**:
| 列名 | 说明 | 是否必填 | 示例 |
|------|------|----------|------|
| name | 建筑名称 | 是 | 教学楼A |
| type | 建筑类型 | 否 | 教学楼 |
| campus_id | 所属校区ID | 是 | 1 |
| floors | 楼层数 | 否 | 5 |
| description | 描述 | 否 | 主教学楼 |

**示例内容**:
```csv
name,type,campus_id,floors,description
教学楼A,教学楼,1,5,主教学楼
实验楼B,实验楼,1,8,科学实验楼
图书馆C,图书馆,2,6,中央图书馆
```

### 2. 设施（facilities）

**文件名示例**: `facilities_example.csv`

**列定义**:
| 列名 | 说明 | 是否必填 | 示例 |
|------|------|----------|------|
| name | 设施名称 | 是 | 第一食堂 |
| type | 设施类型 | 是 | 食堂 |
| building_id | 所属建筑ID | 是 | 1 |
| location_desc | 位置描述 | 否 | 一楼 |
| capacity | 容量 | 否 | 500 |
| contact | 联系电话 | 否 | 021-65640001 |
| description | 描述 | 否 | 学生主食堂 |

**示例内容**:
```csv
name,type,building_id,location_desc,capacity,contact,description
第一食堂,食堂,1,一楼,500,021-65640001,学生主食堂
第二食堂,食堂,2,二楼,300,021-65640002,教工食堂
咖啡厅A,咖啡厅,1,一楼,50,021-65640003,校园咖啡厅
```

### 3. 课程（courses）

**文件名示例**: `courses_example.csv`

**列定义**:
| 列名 | 说明 | 是否必填 | 示例 |
|------|------|----------|------|
| name | 课程名称 | 是 | 高等数学 |
| department_id | 开课院系ID | 否 | 1 |
| credits | 学分 | 否 | 4.0 |
| semester_offered | 开课学期 | 否 | 2024-2025-1 |
| description | 描述 | 否 | 基础数学课程 |

**示例内容**:
```csv
name,department_id,credits,semester_offered,description
高等数学,1,4.0,2024-2025-1,基础数学课程
大学英语,2,3.0,2024-2025-1,英语基础课程
数据结构,3,3.5,2024-2025-2,计算机核心课程
```

### 4. 教师（teachers）

**文件名示例**: `teachers_example.csv`

**列定义**:
| 列名 | 说明 | 是否必填 | 示例 |
|------|------|----------|------|
| name | 教师姓名 | 是 | 张三 |
| department_id | 所属院系ID | 否 | 1 |
| title | 职称 | 否 | 教授 |
| email | 邮箱 | 否 | zhangsan@fudan.edu.cn |
| phone | 电话 | 否 | 021-65640010 |

**示例内容**:
```csv
name,department_id,title,email,phone
张三,1,教授,zhangsan@fudan.edu.cn,021-65640010
李四,2,副教授,lisi@fudan.edu.cn,021-65640011
王五,3,讲师,wangwu@fudan.edu.cn,021-65640012
```

### 5. 课程-教师关联（course_teachers）

**文件名示例**: `course_teachers_example.csv`

**列定义**:
| 列名 | 说明 | 是否必填 | 示例 |
|------|------|----------|------|
| course_id | 课程ID | 是 | 1 |
| teacher_id | 教师ID | 是 | 1 |
| semester | 学期 | 是 | 2024-2025-1 |
| role | 角色 | 否 | 主讲 |
| remarks | 备注 | 否 | 计算机科学导论课程 |

**示例内容**:
```csv
course_id,teacher_id,semester,role,remarks
1,1,2024-2025-1,主讲,计算机科学导论课程
1,2,2024-2025-1,助教,协助实验教学
2,3,2024-2025-1,主讲,高等数学A课程
```

**注意事项**:
- **复合主键**: (course_id, teacher_id, semester) 三者组合必须唯一
- **重复检测**: 同一教师在同一学期不能重复关联同一课程
- **外键依赖**: course_id 和 teacher_id 必须在数据库中已存在
- **角色类型**: 常见角色包括"主讲"、"助教"等，可自定义

### 6. 活动（events）

**文件名示例**: `events_example.csv`

**列定义**:
| 列名 | 说明 | 是否必填 | 示例 |
|------|------|----------|------|
| name | 活动名称 | 是 | 新生开学典礼 |
| event_time | 活动时间 | 是 | 2025-09-01 09:00:00 |
| location_desc | 地点描述 | 否 | 大礼堂 |
| campus_id | 所属校区ID | 否 | 1 |
| organizer | 主办方 | 否 | 学校办公室 |
| category | 类别 | 否 | 典礼 |
| description | 描述 | 否 | 2025级新生开学典礼 |

**示例内容**:
```csv
name,event_time,location_desc,campus_id,organizer,category,description
新生开学典礼,2025-09-01 09:00:00,大礼堂,1,学校办公室,典礼,2025级新生开学典礼
学术讲座-AI前沿,2025-05-25 14:00:00,报告厅,1,计算机学院,学术讲座,人工智能前沿技术讲座
```

## 注意事项

### 1. 外键依赖关系

导入数据时需要注意外键依赖关系，建议按以下顺序导入：

1. **校区** (campus) - 无依赖
2. **院系** (department) - 无依赖
3. **建筑** (building) - 依赖校区
4. **设施** (facility) - 依赖建筑
5. **教师** (teacher) - 依赖院系
6. **课程** (course) - 依赖院系
7. **课程-教师关系** (course_teacher) - 依赖课程和教师
8. **活动** (event) - 依赖校区和建筑

### 2. ID引用

- CSV中的ID字段（如`campus_id`、`department_id`等）必须在数据库中已存在
- 如果引用的ID不存在，导入会失败
- 可以先查询数据库获取有效的ID值

### 3. 日期时间格式

- 活动时间的格式必须为：`YYYY-MM-DD HH:MM:SS`
- 示例：`2025-09-01 09:00:00`

### 4. 事务处理

- CSV导入使用事务机制，如果导入过程中出现错误，所有数据都会回滚
- 确保CSV文件中所有数据都正确后再导入

### 5. 重复数据

- **课程-教师关联**: 系统会自动检测重复的(course_id, teacher_id, semester)组合，跳过已存在的记录
- **其他数据**: 系统不会自动检查重复数据，导入前请确认数据不重复，或先清空相关数据

## 常见问题

### Q1: 导入失败，提示"外键约束失败"

**原因**: CSV中引用的ID在数据库中不存在

**解决方法**: 
1. 检查`campus_id`、`department_id`、`building_id`等外键字段
2. 先查询数据库确认这些ID是否存在
3. 修改CSV中的ID为有效值后重新导入

### Q2: 导入后中文显示乱码

**原因**: CSV文件编码不是UTF-8

**解决方法**:
1. 用记事本打开CSV文件
2. 选择"另存为"
3. 在编码选项中选择"UTF-8"
4. 保存后重新导入

### Q3: 导入课程-教师关联时提示"该教师在此学期已关联此课程"

**原因**: CSV中存在重复的(course_id, teacher_id, semester)组合

**解决方法**:
1. 检查CSV文件中是否有相同的三元组组合
2. 删除重复的行
3. 或者修改semester字段为不同的学期

### Q4: 如何查看已导入的数据

**方法**:
1. 在管理后台对应标签页查看
2. 或直接查询数据库：
   ```sql
   SELECT * FROM building;
   SELECT * FROM facility;
   SELECT * FROM course;
   SELECT * FROM teacher;
   SELECT * FROM course_teacher;
   SELECT * FROM event;
   ```

### Q5: 导入大量数据时超时

**原因**: 数据量过大导致超时

**解决方法**:
1. 将CSV文件拆分成多个小文件
2. 分批导入
3. 或者联系管理员调整服务器超时设置

## 示例文件位置

系统提供了完整的CSV示例文件，位于：
```
database/csv_templates/
├── README.md
├── buildings_example.csv
├── facilities_example.csv
├── courses_example.csv
├── teachers_example.csv
├── course_teachers_example.csv
└── events_example.csv
```

可以直接使用这些示例文件作为模板，修改数据后导入。
