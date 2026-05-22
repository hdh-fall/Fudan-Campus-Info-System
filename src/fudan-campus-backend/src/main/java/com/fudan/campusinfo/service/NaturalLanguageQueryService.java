package com.fudan.campusinfo.service;

import com.fudan.campusinfo.entity.*;
import com.fudan.campusinfo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自然语言查询服务
 * 将用户的自然语言问题转换为数据库查询
 */
@Service
public class NaturalLanguageQueryService {

    @Autowired
    private CampusInfoService campusInfoService;

    @Autowired
    private QueryRecordRepository queryRecordRepository;
    
    @Autowired(required = false)
    private AINaturalLanguageService aiService;
    
    // 存储AI分析结果，供executeQuery使用
    private ThreadLocal<Map<String, Object>> aiAnalysisResult = new ThreadLocal<>();

    /**
     * 同义词映射表 - 用于意图泛化
     */
    private static final Map<String, List<String>> SYNONYM_MAP = new HashMap<>();
    
    static {
        // 食堂相关 - 覆盖各种吃饭场景
        SYNONYM_MAP.put("食堂", Arrays.asList(
            "吃饭", "餐厅", "饭堂", "用餐", "就餐", "哪里可以吃饭", "去哪吃", "吃什么",
            "去哪里吃饭", "哪有吃的", "哪里有食堂", "吃饭的地方", "食堂在哪里",
            "饿了", "找吃的", "美食", "早餐", "午餐", "晚餐", "宵夜",
            "有什么好吃的", "推荐食堂", "哪个食堂好"
        ));
        // 咖啡厅相关
        SYNONYM_MAP.put("咖啡", Arrays.asList(
            "cafe", "饮品", "饮料", "喝咖啡", "奶茶", "休息", "聊天", "咖啡店", "咖啡馆",
            "下午茶", "甜品", "果汁", "星巴克"
        ));
        // 图书馆/自习室相关
        SYNONYM_MAP.put("学习", Arrays.asList(
            "自习", "阅览室", "看书", "复习", "备考", "安静", "哪里可以学习", "找个地方学习",
            "学习的地方", "图书馆在哪里", "哪里可以自习", "借书", "还书",
            "写作业", "做研究", "查资料", "阅读"
        ));
        // 建筑相关 - 覆盖各种问路场景
        SYNONYM_MAP.put("楼", Arrays.asList(
            "建筑", "教学楼", "宿舍", "馆", "大楼", "在哪里", "位置", "哪栋楼", "哪个楼",
            "怎么走", "如何去", "地址", "地点", "有哪些建筑", "建筑物",
            "办公楼", "实验楼", "综合楼", "行政楼"
        ));
        // 课程相关
        SYNONYM_MAP.put("课程", Arrays.asList(
            "课", "上课", "选课", "学分", "哪门课", "什么课", "有哪些课", "开什么课",
            "必修课", "选修课", "专业课", "公选课", "通识课",
            "课程表", "教学计划", "培养方案"
        ));
        // 教师相关 - 增加更多自然表达
        SYNONYM_MAP.put("老师", Arrays.asList(
            "教师", "教授", "谁教", "授课", "任课", "哪位老师", "老师是谁",
            "谁上", "谁讲", "主讲", "任课老师", "授课老师", "教什么", "谁教的",
            "导师", "辅导员", "班主任", "助教",
            "联系方式", "办公室", "邮箱", "电话"
        ));
        // 活动相关
        SYNONYM_MAP.put("活动", Arrays.asList(
            "讲座", "论坛", "晚会", "比赛", "展览", "有什么活动", "近期活动",
            "最近活动", "周末活动", "校园活动", "社团活动", "文艺活动",
            "体育比赛", "学术报告", "招聘会", "宣讲会"
        ));
        // 校区相关 - 新增
        SYNONYM_MAP.put("校区", Arrays.asList(
            "哪个校区", "校区地址", "校区电话", "校区介绍",
            "邯郸校区", "枫林校区", "张江校区", "江湾校区",
            "校区在哪里", "怎么去校区", "校区交通"
        ));
    }

    /**
     * 处理自然语言查询
     * @param question 用户问题
     * @param userId 用户ID
     * @return 查询结果
     */
    public Map<String, Object> processNaturalLanguageQuery(String question, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        
        // 解析问题类型（使用改进的意图识别）
        QueryType queryType = parseQueryTypeWithIntent(question);
        result.put("queryType", queryType.getDescription());
        
        // 执行对应的查询
        List<Map<String, Object>> data = executeQuery(question, queryType);
        result.put("data", data);
        result.put("count", data.size());
        
        // 保存查询记录
        saveQueryRecord(userId, question, queryType, data);
        
        return result;
    }

    /**
     * 基于意图识别的问题类型解析（改进版 + AI增强）
     * 支持同义词和自然表达变体
     */
    private QueryType parseQueryTypeWithIntent(String question) {
        // 清空之前的AI分析结果
        aiAnalysisResult.remove();
        
        // 尝试使用AI分析（如果可用）
        if (aiService != null) {
            try {
                Map<String, Object> aiResult = aiService.analyzeQuestion(question);
                
                // 如果AI启用且成功分析
                if (Boolean.TRUE.equals(aiResult.get("ai_enabled"))) {
                    String sql = (String) aiResult.get("sql");
                    if (sql != null && !sql.isEmpty()) {
                        System.out.println("✅ AI生成SQL: " + sql);
                        System.out.println("💡 说明: " + aiResult.get("explanation"));
                        
                        // 保存AI分析结果供后续使用
                        aiAnalysisResult.set(aiResult);
                        
                        // 返回特殊类型，表示使用AI SQL
                        return QueryType.AI_SQL;
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ AI分析失败，降级到规则匹配: " + e.getMessage());
            }
        }
        
        // 降级到原有的规则匹配
        System.out.println("📋 使用规则匹配");
        return parseQueryTypeWithRules(question);
    }
    
    /**
     * 将AI返回的字符串类型转换为QueryType枚举
     */
    private QueryType convertToQueryType(String aiType) {
        try {
            return QueryType.valueOf(aiType);
        } catch (IllegalArgumentException e) {
            System.err.println("未知的查询类型: " + aiType + "，使用默认类型");
            return QueryType.GENERAL_SEARCH;
        }
    }
    
    /**
     * 基于规则的意图识别（原有逻辑）
     */
    private QueryType parseQueryTypeWithRules(String question) {
        String lowerQuestion = question.toLowerCase().trim();
            
        // 计算每个类别的匹配得分
        Map<QueryType, Integer> scores = new HashMap<>();
        
        // 检测是否有校区名称
        boolean hasCampusName = lowerQuestion.contains("邯郸") || lowerQuestion.contains("枫林") || 
                                lowerQuestion.contains("张江") || lowerQuestion.contains("江湾");
            
        // 食堂相关 - 提高匹配精度
        if (matchesIntent(lowerQuestion, "食堂")) {
            int score = calculateMatchScore(lowerQuestion, "食堂");
            // “去哪里吃饭”、“哪有吃的”等强意图表达额外加分
            if (lowerQuestion.contains("去哪里吃饭") || lowerQuestion.contains("哪有吃的") || 
                lowerQuestion.contains("哪里有食堂") || lowerQuestion.contains("吃饭的地方")) {
                score += 8;
            }
            // 如果有校区名，优先返回食堂而非校区
            if (hasCampusName) {
                score += 15;  // 确保识别为食堂查询
            }
            scores.put(QueryType.FACILITY_CANTEEN, score);
        }
            
        // 咖啡厅相关
        if (matchesIntent(lowerQuestion, "咖啡")) {
            scores.put(QueryType.FACILITY_CAFE, calculateMatchScore(lowerQuestion, "咖啡"));
        }
            
        // 图书馆/自习室相关
        if (matchesIntent(lowerQuestion, "学习")) {
            int score = calculateMatchScore(lowerQuestion, "学习");
            // “哪里可以学习”、“找个地方学习”等强意图表达额外加分
            if (lowerQuestion.contains("哪里可以学习") || lowerQuestion.contains("找个地方学习") ||
                lowerQuestion.contains("学习的地方") || lowerQuestion.contains("哪里可以自习")) {
                score += 8;
            }
            scores.put(QueryType.FACILITY_LIBRARY, score);
        }
        
        // 建筑相关 - 检测“教学楼”、“有哪些建筑”等
        if (matchesIntent(lowerQuestion, "楼")) {
            int score = calculateMatchScore(lowerQuestion, "楼");
            // 如果是问路或位置，额外加分
            if (lowerQuestion.contains("怎么走") || lowerQuestion.contains("如何去") || 
                lowerQuestion.contains("在哪里") || lowerQuestion.contains("地址")) {
                score += 5;
            }
            // 如果提到“教学楼”、“有哪些”，给予高分
            if (lowerQuestion.contains("教学楼") || lowerQuestion.contains("有哪些") || 
                lowerQuestion.contains("有什么")) {
                score += 10;
            }
            // 如果有校区名，优先返回该校区建筑
            if (hasCampusName) {
                score += 15;
            }
            scores.put(QueryType.BUILDING, score);
        }
            
        // 校区相关 - 仅在没有明确设施/建筑意图时才匹配
        if (matchesIntent(lowerQuestion, "校区") && !scores.containsKey(QueryType.FACILITY_CANTEEN) && 
            !scores.containsKey(QueryType.BUILDING)) {
            int score = calculateMatchScore(lowerQuestion, "校区");
            // 如果明确提到校区名称，给予高分
            if (hasCampusName) {
                score += 10;
            }
            scores.put(QueryType.CAMPUS, score);
        }
            
        // 课程相关
        if (matchesIntent(lowerQuestion, "课程")) {
            int score = calculateMatchScore(lowerQuestion, "课程");
            scores.put(QueryType.COURSE, score);
        }
            
        // 教师相关 - 优先匹配“谁教”、“谁上”、“授课”等强意图词
        if (matchesIntent(lowerQuestion, "老师")) {
            int score = calculateMatchScore(lowerQuestion, "老师");
            // 如果是“谁教”、“谁上”这类强意图，给予额外加分
            if (lowerQuestion.contains("谁教") || lowerQuestion.contains("授课") || 
                lowerQuestion.contains("任课") || lowerQuestion.contains("谁上") ||
                lowerQuestion.contains("谁讲") || lowerQuestion.contains("主讲") ||
                lowerQuestion.contains("谁开的") || lowerQuestion.contains("开的")) {
                score += 15;  // 提高权重，确保识别为教师查询
            }
            scores.put(QueryType.TEACHER, score);
        }
            
        // 活动相关
        if (matchesIntent(lowerQuestion, "活动")) {
            int score = calculateMatchScore(lowerQuestion, "活动");
            // “最近”、“近期”、“有哪些”等表达额外加分
            if (lowerQuestion.contains("最近") || lowerQuestion.contains("近期") || 
                lowerQuestion.contains("有哪些") || lowerQuestion.contains("有什么")) {
                score += 8;
            }
            scores.put(QueryType.EVENT, score);
        }
            
        // 返回得分最高的类型
        if (!scores.isEmpty()) {
            return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(QueryType.GENERAL_SEARCH);
        }
            
        // 默认返回通用搜索
        return QueryType.GENERAL_SEARCH;
    }
    
    /**
     * 检查问题是否匹配某个意图（包括同义词）
     */
    private boolean matchesIntent(String question, String intentKey) {
        List<String> synonyms = SYNONYM_MAP.get(intentKey);
        if (synonyms == null) {
            return question.contains(intentKey);
        }
        
        // 检查是否包含任何同义词或关键词本身
        for (String synonym : synonyms) {
            if (question.contains(synonym)) {
                return true;
            }
        }
        return question.contains(intentKey);
    }
    
    /**
     * 计算匹配得分（用于多个意图冲突时选择最相关的）
     */
    private int calculateMatchScore(String question, String intentKey) {
        int score = 0;
        List<String> synonyms = SYNONYM_MAP.get(intentKey);
        
        if (synonyms != null) {
            for (String synonym : synonyms) {
                if (question.contains(synonym)) {
                    // 长关键词权重更高
                    score += synonym.length() > 2 ? 3 : 2;
                }
            }
        }
        
        // 原始关键词也计分
        if (question.contains(intentKey)) {
            score += 2;
        }
        
        return score;
    }

    /**
     * 解析问题类型（旧版本，保留作为备用）
     */
    private QueryType parseQueryType(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // 食堂相关
        if (lowerQuestion.contains("食堂") || lowerQuestion.contains("吃饭") || lowerQuestion.contains("餐厅")) {
            return QueryType.FACILITY_CANTEEN;
        }
        
        // 咖啡厅相关
        if (lowerQuestion.contains("咖啡") || lowerQuestion.contains("cafe") || lowerQuestion.contains("饮品")) {
            return QueryType.FACILITY_CAFE;
        }
        
        // 图书馆/自习室相关
        if (lowerQuestion.contains("图书馆") || lowerQuestion.contains("自习") || lowerQuestion.contains("阅览室")) {
            return QueryType.FACILITY_LIBRARY;
        }
        
        // 建筑相关
        if (lowerQuestion.contains("楼") || lowerQuestion.contains("建筑") || lowerQuestion.contains("教学楼")) {
            return QueryType.BUILDING;
        }
        
        // 校区相关
        if (lowerQuestion.contains("校区")) {
            return QueryType.CAMPUS;
        }
        
        // 课程相关
        if (lowerQuestion.contains("课程") || lowerQuestion.contains("课")) {
            return QueryType.COURSE;
        }
        
        // 教师相关
        if (lowerQuestion.contains("老师") || lowerQuestion.contains("教师") || lowerQuestion.contains("教授")) {
            return QueryType.TEACHER;
        }
        
        // 活动相关
        if (lowerQuestion.contains("活动") || lowerQuestion.contains("讲座") || lowerQuestion.contains("论坛") || lowerQuestion.contains("晚会")) {
            return QueryType.EVENT;
        }
        
        // 默认返回通用搜索
        return QueryType.GENERAL_SEARCH;
    }

    /**
     * 执行查询（改进版 - 使用AI分析结果）
     */
    private List<Map<String, Object>> executeQuery(String question, QueryType queryType) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        switch (queryType) {
            case FACILITY_CANTEEN:
                // 使用改进的意图查询，支持校区过滤和关键词匹配
                results = queryFacilitiesByIntent(question, "食堂");
                break;
            case FACILITY_CAFE:
                results = queryFacilitiesByIntent(question, "咖啡厅");
                break;
            case FACILITY_LIBRARY:
                // 查询图书馆和自习室
                List<Map<String, Object>> libraries = queryFacilitiesByIntent(question, "阅览室");
                List<Map<String, Object>> studyRooms = queryFacilitiesByIntent(question, "自习室");
                results.addAll(libraries);
                results.addAll(studyRooms);
                break;
            case BUILDING:
                results = queryBuildings(question);
                break;
            case CAMPUS:
                results = queryCampuses();
                break;
            case COURSE:
                results = queryCourses(question);
                break;
            case TEACHER:
                results = queryTeachers(question);
                break;
            case EVENT:
                results = queryEvents(question);
                break;
            case GENERAL_SEARCH:
            default:
                results = generalSearch(question);
                break;
            case AI_SQL:
                // 使用AI生成的SQL直接查询
                results = executeAISQL();
                break;
        }
        
        // 清空AI分析结果，避免影响下次查询
        aiAnalysisResult.remove();
        
        return results;
    }

    /**
     * 查询指定类型的设施（改进版 - 更精确的匹配）
     */
    private List<Map<String, Object>> queryFacilitiesByType(String type) {
        List<Facility> facilities = campusInfoService.getFacilitiesByType(type);
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Facility facility : facilities) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", facility.getName());
            item.put("type", facility.getType());
            item.put("location", facility.getLocationDesc());
            item.put("building", facility.getBuilding() != null ? facility.getBuilding().getName() : "未知");
            item.put("openTime", facility.getOpenTime());
            item.put("capacity", facility.getCapacity());
            item.put("description", facility.getDescription());
            results.add(item);
        }
        
        return results;
    }

    /**
     * 根据意图和关键词查询设施（新增 - 支持更精确的语义匹配）
     */
    private List<Map<String, Object>> queryFacilitiesByIntent(String question, String facilityType) {
        // 首先按类型查询
        List<Facility> facilities = campusInfoService.getFacilitiesByType(facilityType);
        
        // 尝试使用AI提取的校区过滤
        Map<String, Object> aiResult = aiAnalysisResult.get();
        String campusFilter = null;
        if (aiResult != null && aiResult.get("campus_filter") != null) {
            campusFilter = (String) aiResult.get("campus_filter");
            System.out.println("🏫 使用AI提取的校区过滤: " + campusFilter);
        }
        
        // 如果AI没有提供，尝试从问题中提取
        if (campusFilter == null) {
            campusFilter = extractCampusName(question);
        }
        
        // 应用校区过滤
        if (campusFilter != null && !facilities.isEmpty()) {
            // 找到对应校区的建筑ID列表
            List<Campus> campuses = campusInfoService.getAllCampuses();
            Integer campusId = null;
            for (Campus campus : campuses) {
                if (campus.getName().contains(campusFilter)) {
                    campusId = campus.getCampusId();
                    break;
                }
            }
            
            if (campusId != null) {
                // 使用 final 变量供 lambda 表达式使用
                final Integer finalCampusId = campusId;
                // 过滤出属于该校区建筑的设施
                facilities = facilities.stream()
                    .filter(f -> f.getBuilding() != null && f.getBuilding().getCampus() != null 
                        && f.getBuilding().getCampus().getCampusId().equals(finalCampusId))
                    .toList();
                System.out.println("✅ 校区过滤后剩余 " + facilities.size() + " 条记录");
            }
        }
        
        // 如果问题中包含具体关键词，进行名称匹配
        String keyword = extractKeyword(question, SYNONYM_MAP.getOrDefault("食堂", Arrays.asList()).toArray(new String[0]));
        if (keyword != null && !keyword.isEmpty() && !facilities.isEmpty()) {
            // 使用 final 变量供 lambda 表达式使用
            final String finalKeyword = keyword;
            facilities = facilities.stream()
                .filter(f -> f.getName().contains(finalKeyword) || 
                           (f.getLocationDesc() != null && f.getLocationDesc().contains(finalKeyword)))
                .toList();
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        for (Facility facility : facilities) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", facility.getName());
            item.put("type", facility.getType());
            item.put("location", facility.getLocationDesc());
            item.put("building", facility.getBuilding() != null ? facility.getBuilding().getName() : "未知");
            item.put("campus", facility.getBuilding() != null && facility.getBuilding().getCampus() != null 
                ? facility.getBuilding().getCampus().getName() : "未知");
            item.put("openTime", facility.getOpenTime());
            item.put("capacity", facility.getCapacity());
            item.put("description", facility.getDescription());
            results.add(item);
        }
        
        return results;
    }

    /**
     * 查询建筑（改进版 - 支持校区精准过滤）
     */
    private List<Map<String, Object>> queryBuildings(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // 尝试使用AI提取的校区过滤
        Map<String, Object> aiResult = aiAnalysisResult.get();
        String campusFilter = null;
        if (aiResult != null && aiResult.get("campus_filter") != null) {
            campusFilter = (String) aiResult.get("campus_filter");
            System.out.println("🏫 使用AI提取的校区过滤: " + campusFilter);
        }
        
        // 如果AI没有提供，尝试从问题中提取
        if (campusFilter == null) {
            campusFilter = extractCampusName(question);
        }
        
        List<Building> buildings;
        if (campusFilter != null) {
            // 先找到校区ID
            List<Campus> campuses = campusInfoService.getAllCampuses();
            Integer campusId = null;
            String foundCampusName = null;
            for (Campus campus : campuses) {
                if (campus.getName().contains(campusFilter)) {
                    campusId = campus.getCampusId();
                    foundCampusName = campus.getName();
                    break;
                }
            }
            
            if (campusId != null) {
                buildings = campusInfoService.getBuildingsByCampusId(campusId);
                System.out.println("✅ 找到 " + buildings.size() + " 栋建筑");
            } else {
                buildings = campusInfoService.searchBuildings(campusFilter);
            }
        } else {
            // 提取建筑关键词
            String keyword = extractKeyword(question, new String[]{"楼", "建筑", "教学楼", "宿舍", "馆", "有哪些", "有什么"});
            buildings = campusInfoService.searchBuildings(keyword != null ? keyword : "");
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        for (Building building : buildings) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", building.getName());
            item.put("type", building.getType());
            item.put("campus", building.getCampus() != null ? building.getCampus().getName() : "未知");
            item.put("floors", building.getFloors());
            item.put("openTime", building.getOpenTime());
            item.put("closeTime", building.getCloseTime());
            item.put("description", building.getDescription());
            results.add(item);
        }
        
        return results;
    }

    /**
     * 查询校区
     */
    private List<Map<String, Object>> queryCampuses() {
        List<Campus> campuses = campusInfoService.getAllCampuses();
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Campus campus : campuses) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", campus.getName());
            item.put("address", campus.getAddress());
            item.put("phone", campus.getContactPhone());
            item.put("description", campus.getDescription());
            results.add(item);
        }
        
        return results;
    }

    /**
     * 查询课程（改进版 - 支持“谁教XXX课”的语义）
     */
    private List<Map<String, Object>> queryCourses(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // 提取课程名称或教师名称
        String keyword = extractKeyword(question, new String[]{"课程", "课"});
        
        List<Course> courses;
        if (keyword != null && !keyword.isEmpty()) {
            courses = campusInfoService.searchCourses(keyword);
        } else {
            courses = campusInfoService.getAllCourses();
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        for (Course course : courses) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", course.getName());
            item.put("department", course.getDepartment() != null ? course.getDepartment().getName() : "未知");
            item.put("credits", course.getCredits());
            item.put("semester", course.getSemesterOffered());
            item.put("description", course.getDescription());
            
            // 获取该课程的授课教师列表
            List<CourseTeacher> courseTeachers = campusInfoService.getTeachersByCourseId(course.getCourseId());
            if (!courseTeachers.isEmpty()) {
                List<String> teacherNames = new ArrayList<>();
                for (CourseTeacher ct : courseTeachers) {
                    Teacher teacher = ct.getTeacher();
                    String teacherInfo = teacher.getName();
                    if (teacher.getTitle() != null) {
                        teacherInfo += "(" + teacher.getTitle() + ")";
                    }
                    teacherNames.add(teacherInfo);
                }
                item.put("teachers", String.join(", ", teacherNames));
            } else {
                item.put("teachers", "暂无教师信息");
            }
            
            results.add(item);
        }
        
        return results;
    }

    /**
     * 查询教师（改进版 - 支持“谁教这门课”、“谁上XXX课”等语义）
     */
    private List<Map<String, Object>> queryTeachers(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // 尝试使用AI提取的关键词
        Map<String, Object> aiResult = aiAnalysisResult.get();
        if (aiResult != null) {
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) aiResult.get("keywords");
            
            if (keywords != null && !keywords.isEmpty()) {
                String courseKeyword = keywords.get(0); // 取第一个关键词作为课程名
                System.out.println("🎯 使用AI提取的课程关键词: " + courseKeyword);
                
                // 搜索课程
                List<Course> courses = campusInfoService.searchCourses(courseKeyword);
                if (!courses.isEmpty()) {
                    // 找到课程后，返回该课程的授课教师信息
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Course course : courses) {
                        // 获取该课程的授课教师列表
                        List<CourseTeacher> courseTeachers = campusInfoService.getTeachersByCourseId(course.getCourseId());
                            
                        if (!courseTeachers.isEmpty()) {
                            for (CourseTeacher ct : courseTeachers) {
                                Teacher teacher = ct.getTeacher();
                                Map<String, Object> item = new HashMap<>();
                                item.put("name", teacher.getName());
                                item.put("department", teacher.getDepartment() != null ? teacher.getDepartment().getName() : "未知");
                                item.put("title", teacher.getTitle());
                                item.put("email", teacher.getEmail());
                                item.put("phone", teacher.getPhone());
                                item.put("course", course.getName());
                                item.put("semester", ct.getSemester());
                                item.put("role", ct.getRole() != null ? ct.getRole() : "主讲");
                                results.add(item);
                            }
                        } else {
                            Map<String, Object> item = new HashMap<>();
                            item.put("course", course.getName());
                            item.put("note", "该课程暂无授课教师信息");
                            results.add(item);
                        }
                    }
                    return results;
                }
            }
        }
            
        // 检查是否是“谁教XXX”或“谁上XXX”的句式（规则匹配备用）
        boolean isWhoTeaches = lowerQuestion.contains("谁教") || lowerQuestion.contains("授课") || 
                              lowerQuestion.contains("任课") || lowerQuestion.contains("谁上") || 
                              lowerQuestion.contains("谁讲") || lowerQuestion.contains("主讲") ||
                              lowerQuestion.contains("谁开的") || lowerQuestion.contains("开的");
            
        if (isWhoTeaches) {
            // 提取课程名称
            String courseName = extractKeyword(question, Arrays.asList(
                "谁教", "授课", "任课", "谁上", "谁讲", "主讲", 
                "老师", "教师", "教授", "教的", "讲的", "上的", "开的"
            ));
            if (courseName != null && !courseName.isEmpty()) {
                // 搜索课程
                List<Course> courses = campusInfoService.searchCourses(courseName);
                if (!courses.isEmpty()) {
                    // 找到课程后，返回该课程的授课教师信息
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Course course : courses) {
                        // 获取该课程的授课教师列表
                        List<CourseTeacher> courseTeachers = campusInfoService.getTeachersByCourseId(course.getCourseId());
                            
                        if (!courseTeachers.isEmpty()) {
                            for (CourseTeacher ct : courseTeachers) {
                                Teacher teacher = ct.getTeacher();
                                Map<String, Object> item = new HashMap<>();
                                item.put("name", teacher.getName());
                                item.put("department", teacher.getDepartment() != null ? teacher.getDepartment().getName() : "未知");
                                item.put("title", teacher.getTitle());
                                item.put("email", teacher.getEmail());
                                item.put("phone", teacher.getPhone());
                                item.put("course", course.getName());
                                item.put("semester", ct.getSemester());
                                item.put("role", ct.getRole() != null ? ct.getRole() : "主讲");
                                results.add(item);
                            }
                        } else {
                            Map<String, Object> item = new HashMap<>();
                            item.put("course", course.getName());
                            item.put("note", "该课程暂无授课教师信息");
                            results.add(item);
                        }
                    }
                    return results;
                }
            }
        }
            
        // 提取教师关键词
        String keyword = extractKeyword(question, new String[]{"老师", "教师", "教授", "谁教", "授课", "任课", "谁上", "谁讲", "主讲"});
            
        List<Teacher> teachers;
        if (keyword != null && !keyword.isEmpty()) {
            teachers = campusInfoService.searchTeachers(keyword);
        } else {
            teachers = campusInfoService.getAllTeachers();
        }
            
        List<Map<String, Object>> results = new ArrayList<>();
        for (Teacher teacher : teachers) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", teacher.getName());
            item.put("department", teacher.getDepartment() != null ? teacher.getDepartment().getName() : "未知");
            item.put("title", teacher.getTitle());
            item.put("email", teacher.getEmail());
            item.put("phone", teacher.getPhone());
            results.add(item);
        }
            
        return results;
    }

    /**
     * 查询活动（改进版 - 支持“讲座”、“最近”等关键词）
     */
    private List<Map<String, Object>> queryEvents(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // 尝试使用AI提取的时间过滤
        Map<String, Object> aiResult = aiAnalysisResult.get();
        boolean upcoming = false;
        
        if (aiResult != null && aiResult.get("time_filter") != null) {
            String timeFilter = (String) aiResult.get("time_filter");
            System.out.println("⏰ 使用AI提取的时间过滤: " + timeFilter);
            upcoming = true; // AI识别为时间相关查询
        } else {
            // 降级到规则匹配
            upcoming = lowerQuestion.contains("近期") || lowerQuestion.contains("最近") || 
                      lowerQuestion.contains("下周") || lowerQuestion.contains("有哪些") ||
                      lowerQuestion.contains("有什么") || lowerQuestion.contains("校园");
        }
        
        List<Event> events;
        if (upcoming) {
            // 获取未来30天内的活动
            events = campusInfoService.getUpcomingEvents(30);
            System.out.println("✅ 找到 " + events.size() + " 个近期活动");
        } else {
            String keyword = extractKeyword(question, new String[]{"活动", "讲座", "论坛", "晚会", "比赛", "展览"});
            events = campusInfoService.searchEvents(keyword != null ? keyword : "");
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        for (Event event : events) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", event.getName());
            item.put("time", event.getEventTime());
            item.put("location", event.getLocationDesc());
            item.put("organizer", event.getOrganizer());
            item.put("category", event.getCategory());
            item.put("description", event.getDescription());
            results.add(item);
        }
        
        return results;
    }

    /**
     * 通用搜索（改进版 - 优先返回精确匹配）
     */
    private List<Map<String, Object>> generalSearch(String keyword) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // 提取有效关键词（去除疑问词等）
        String cleanKeyword = extractKeyword(keyword, new String[]{
            "什么", "哪些", "哪里", "怎么", "如何", "有没有", "的", "吗", "呢"
        });
        
        if (cleanKeyword == null || cleanKeyword.isEmpty()) {
            return results;
        }
        
        // 搜索设施（优先级最高，因为通常是具体地点）
        List<Facility> facilities = campusInfoService.searchFacilities(cleanKeyword);
        for (Facility f : facilities) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "设施");
            item.put("name", f.getName());
            item.put("detail", f.getType() + " - " + (f.getBuilding() != null ? f.getBuilding().getName() : ""));
            item.put("relevance", "high"); // 高相关性
            results.add(item);
        }
        
        // 搜索建筑
        List<Building> buildings = campusInfoService.searchBuildings(cleanKeyword);
        for (Building b : buildings) {
            // 避免与设施重复
            boolean duplicate = results.stream()
                .anyMatch(r -> r.get("name").equals(b.getName()));
            if (!duplicate) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "建筑");
                item.put("name", b.getName());
                item.put("detail", b.getCampus() != null ? b.getCampus().getName() : "");
                item.put("relevance", "medium"); // 中相关性
                results.add(item);
            }
        }
        
        // 搜索课程
        List<Course> courses = campusInfoService.searchCourses(cleanKeyword);
        for (Course c : courses) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "课程");
            item.put("name", c.getName());
            item.put("detail", c.getDepartment() != null ? c.getDepartment().getName() : "");
            item.put("relevance", "medium");
            results.add(item);
        }
        
        // 搜索活动（仅当关键词明确时）
        if (cleanKeyword.length() > 1) {
            List<Event> events = campusInfoService.searchEvents(cleanKeyword);
            for (Event e : events) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "活动");
                item.put("name", e.getName());
                item.put("detail", e.getCategory() + " - " + e.getEventTime());
                item.put("relevance", "low"); // 低相关性
                results.add(item);
            }
        }
        
        return results;
    }

    /**
     * 执行AI生成的SQL查询
     */
    private List<Map<String, Object>> executeAISQL() {
        Map<String, Object> aiResult = aiAnalysisResult.get();
        if (aiResult == null) {
            return new ArrayList<>();
        }
        
        String sql = (String) aiResult.get("sql");
        if (sql == null || sql.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            System.out.println("🚀 执行AI生成的SQL...");
            return campusInfoService.executeNativeSQL(sql);
        } catch (Exception e) {
            System.err.println("❌ AI SQL执行失败: " + e.getMessage());
            // 降级到空结果
            return new ArrayList<>();
        }
    }

    /**
     * 提取校区名称
     */
    private String extractCampusName(String question) {
        String[] campuses = {"邯郸", "枫林", "张江", "江湾"};
        for (String campus : campuses) {
            if (question.contains(campus)) {
                return campus;
            }
        }
        return null;
    }

    /**
     * 提取关键词（支持 List 和数组两种参数类型）
     */
    private String extractKeyword(String question, List<String> removeWords) {
        String result = question;
        for (String word : removeWords) {
            result = result.replace(word, "");
        }
        result = result.replaceAll("[的什么哪些有]", "").trim();
        return result.isEmpty() ? null : result;
    }
    
    /**
     * 提取关键词（数组版本 - 用于兼容旧代码）
     */
    private String extractKeyword(String question, String[] removeWords) {
        String result = question;
        for (String word : removeWords) {
            result = result.replace(word, "");
        }
        result = result.replaceAll("[的什么哪些有]", "").trim();
        return result.isEmpty() ? null : result;
    }

    /**
     * 保存查询记录
     */
    private void saveQueryRecord(Integer userId, String question, QueryType queryType, List<Map<String, Object>> data) {
        try {
            QueryRecord record = new QueryRecord();
            
            // 获取用户对象
            User user = new User();
            user.setUserId(userId);
            record.setUser(user);
            
            record.setQuestion(question);
            record.setQueryTime(LocalDateTime.now());
            record.setCategory(queryType.name());
            record.setResultSummary(data.size() + "条结果");
            record.setUsedNl2sql(true);
            
            queryRecordRepository.save(record);
        } catch (Exception e) {
            // 记录失败不影响查询结果
            System.err.println("保存查询记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询类型枚举
     */
    enum QueryType {
        FACILITY_CANTEEN("食堂查询"),
        FACILITY_CAFE("咖啡厅查询"),
        FACILITY_LIBRARY("图书馆/自习室查询"),
        BUILDING("建筑查询"),
        CAMPUS("校区查询"),
        COURSE("课程查询"),
        TEACHER("教师查询"),
        EVENT("活动查询"),
        AI_SQL("AI SQL查询"),
        GENERAL_SEARCH("通用搜索");

        private final String description;

        QueryType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
