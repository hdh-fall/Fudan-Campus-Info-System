package com.fudan.campusinfo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * AI自然语言处理服务
 * 使用通义千问API进行意图识别和关键词提取
 */
@Service
public class AINaturalLanguageService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.qwen.api-key:}")
    private String apiKey;
    
    @Value("${ai.qwen.enabled:false}")
    private boolean aiEnabled;

    public AINaturalLanguageService() {
        this.webClient = WebClient.builder()
            .baseUrl("https://dashscope.aliyuncs.com/api/v1")
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 分析用户问题，返回结构化的查询意图
     * 
     * @param question 用户问题
     * @return 包含queryType、keywords、filters的Map
     */
    public Map<String, Object> analyzeQuestion(String question) {
        Map<String, Object> result = new HashMap<>();
        
        // 如果AI未启用或没有API Key，返回空结果（使用原有规则匹配）
        if (!aiEnabled || apiKey == null || apiKey.isEmpty()) {
            result.put("ai_enabled", false);
            result.put("query_type", "RULE_BASED");
            return result;
        }

        try {
            // 构建Prompt - 让AI生成SQL
            String prompt = buildSQLPrompt(question);
            
            // 调用通义千问API
            String response = callQwenAPI(prompt);
            
            // 解析响应
            Map<String, Object> aiResult = parseSQLResponse(response);
            
            result.put("ai_enabled", true);
            result.putAll(aiResult);
            
        } catch (Exception e) {
            System.err.println("AI分析失败，降级到规则匹配: " + e.getMessage());
            result.put("ai_enabled", false);
            result.put("query_type", "RULE_BASED");
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 构建SQL生成Prompt
     */
    private String buildSQLPrompt(String question) {
        return String.format("""
            你是一个校园信息查询系统的SQL生成助手。根据用户的问题，生成对应的MySQL查询语句。
                
            数据库表结构：
            - campus(campus_id, name, address, contact_phone, description)
            - building(building_id, name, type, campus_id, floors, open_time, close_time, description)
            - facility(facility_id, name, type, building_id, location_desc, capacity, open_time, description)
            - department(department_id, name, description)
            - teacher(teacher_id, name, department_id, title, email, phone)
            - course(course_id, name, department_id, credits, semester_offered, description)
            - course_teacher(course_id, teacher_id, semester, role, remarks) -- 复合主键
            - event(event_id, name, event_time, campus_id, building_id, location_desc, organizer, category, description)
                
            校区名称映射：
            - 邯郸校区: campus_id = 1
            - 枫林校区: campus_id = 2  
            - 张江校区: campus_id = 3
            - 江湾校区: campus_id = 4
                
            设施类型：食堂、咖啡厅、图书馆、自习室、机房、研讨室、报告厅、实验室
            建筑类型：教学楼、实验楼、办公楼、宿舍、图书馆、体育馆
            
            **重要：课程分类知识**
            - 数学类课程：高等代数、数学分析、线性代数、概率论、数理统计、微积分、离散数学、数值分析等
            - 计算机类课程：程序设计、数据结构、算法、操作系统、计算机网络、数据库、人工智能等
            - 物理类课程：大学物理、理论力学、量子力学、电动力学、热力学等
            - 化学类课程：无机化学、有机化学、分析化学、物理化学等
            - 英语类课程：大学英语、英语写作、英语听力、英语口语等
            
            **查询策略：**
            - 当用户问“数学相关课程”时，应该通过院系（department）或课程描述（description）来查找
            - 如果课程名称不直接包含关键词，可以尝试匹配院系名称或使用OR条件
            - 例如：WHERE co.name LIKE '%%数学%%' OR d.name LIKE '%%数学%%' OR co.description LIKE '%%数学%%'
                
            要求：
            1. 只返回JSON格式，不要解释
            2. SQL必须是有效的MySQL语句
            3. 使用LEFT JOIN保证数据完整性
            4. 时间查询使用event_time >= NOW()
            5. 模糊搜索使用LIKE '%%keyword%%'
            6. **重要：所有SELECT的列都必须使用AS别名，别名要清晰易懂（用英文）**
            7. 例如：f.name AS name, b.name AS building_name, c.name AS campus_name
            8. **课程查询时，必须包含教师信息和学期信息，以便区分同名课程**
            9. **学科相关课程查询：使用多个OR条件匹配相关关键词，包括课程名、院系名、描述**
            10. **例如“数学课程”应匹配：数学、代数、分析、几何、概率、统计等关键词**
            11. **必须返回query_category字段，表示查询的业务类别**
                
            query_category可选值：
            - FACILITY_CANTEEN: 食堂/餐厅/吃饭相关
            - FACILITY_CAFE: 咖啡厅/饮品/咖啡相关
            - FACILITY_LIBRARY: 图书馆/阅览室相关
            - FACILITY_STUDY_ROOM: 自习室/学习空间相关
            - FACILITY_LAB: 实验室/机房相关
            - BUILDING: 建筑/教学楼/宿舍/位置相关
            - CAMPUS: 校区介绍/校区地址相关
            - COURSE: 课程/选课/学分相关
            - TEACHER: 教师/授课/联系方式相关
            - EVENT: 活动/讲座/比赛/展览相关
            - GENERAL_SEARCH: 其他通用查询
                
            示例1：
            用户问题：“复旦有哪些食堂？”
            返回：{
              "sql": "SELECT f.name AS name, f.type AS type, b.name AS building_name, c.name AS campus_name FROM facility f LEFT JOIN building b ON f.building_id = b.building_id LEFT JOIN campus c ON b.campus_id = c.campus_id WHERE f.type = '食堂'",
              "explanation": "查询所有食堂及其位置信息",
              "query_category": "FACILITY_CANTEEN"
            }
                            
            示例2：
            用户问题：“邯郸校区有哪些教学楼？”
            返回：{
              "sql": "SELECT b.name AS name, b.type AS type, c.name AS campus_name FROM building b LEFT JOIN campus c ON b.campus_id = c.campus_id WHERE c.name LIKE '%%邯郸%%' AND b.type = '教学楼'",
              "explanation": "查询邯郸校区的教学楼",
              "query_category": "BUILDING"
            }
                            
            示例3：
            用户问题：“数据库课程是谁开的？”
            返回：{
              "sql": "SELECT co.name AS course_name, t.name AS teacher_name, t.title AS title, t.email AS email, ct.semester AS semester, ct.role AS role FROM course co LEFT JOIN course_teacher ct ON co.course_id = ct.course_id LEFT JOIN teacher t ON ct.teacher_id = t.teacher_id WHERE co.name LIKE '%%数据库%%'",
              "explanation": "查询数据库课程的授课教师（同名课程会显示不同教师和学期）",
              "query_category": "COURSE"
            }
                            
            示例4：
            用户问题：“最近有哪些校园讲座？”
            返回：{
              "sql": "SELECT e.name AS event_name, e.event_time AS event_time, e.location_desc AS location, e.organizer AS organizer, e.category AS category FROM event e WHERE e.event_time >= NOW() AND e.category LIKE '%%讲座%%' ORDER BY e.event_time ASC LIMIT 20",
              "explanation": "查询未来的讲座活动",
              "query_category": "EVENT"
            }
                
            示例5：
            用户问题：“有哪些数学课程？”
            返回：{
              "sql": "SELECT co.name AS course_name, d.name AS department_name, co.credits AS credits, t.name AS teacher_name, ct.semester AS semester FROM course co LEFT JOIN department d ON co.department_id = d.department_id LEFT JOIN course_teacher ct ON co.course_id = ct.course_id LEFT JOIN teacher t ON ct.teacher_id = t.teacher_id WHERE co.name LIKE '%%数学%%' OR co.name LIKE '%%代数%%' OR co.name LIKE '%%分析%%' OR d.name LIKE '%%数学%%'",
              "explanation": "查询数学相关课程（包括高等代数、数学分析等，同名课程会按教师和学期分别显示）",
              "query_category": "COURSE"
            }
                
            现在请分析以下问题：
            用户问题："%s"
            返回JSON：
            """, question);
    }

    /**
     * 调用通义千问API
     */
    private String callQwenAPI(String prompt) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> input = new HashMap<>();
            input.put("messages", new Object[]{
                Map.of("role", "user", "content", prompt)
            });
            requestBody.put("input", input);
            requestBody.put("model", "qwen-turbo");
            
            // 发送请求
            String response = webClient.post()
                .uri("/services/aigc/text-generation/generation")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 阻塞等待结果
            
            return response;
            
        } catch (Exception e) {
            throw new RuntimeException("调用AI API失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析AI返回的SQL响应
     */
    private Map<String, Object> parseSQLResponse(String response) throws Exception {
        JsonNode rootNode = objectMapper.readTree(response);
        
        // 提取生成的文本
        String generatedText = rootNode.path("output").path("text").asText();
        System.out.println("🤖 AI原始响应: " + generatedText);
        
        // 提取JSON部分（可能包含在文本中）
        String jsonStr = extractJson(generatedText);
        System.out.println("📋 提取的JSON: " + jsonStr);
        
        // 解析JSON
        JsonNode resultNode = objectMapper.readTree(jsonStr);
        
        Map<String, Object> result = new HashMap<>();
        String sql = resultNode.path("sql").asText("");
        String explanation = resultNode.path("explanation").asText("");
        String queryCategory = resultNode.path("query_category").asText("GENERAL_SEARCH");
        
        result.put("sql", sql);
        result.put("explanation", explanation);
        result.put("query_category", queryCategory);
        
        System.out.println("✅ SQL: " + sql);
        System.out.println("✅ 说明: " + explanation);
        System.out.println("✅ 类别: " + queryCategory);
        
        return result;
    }

    /**
     * 从文本中提取JSON
     */
    private String extractJson(String text) {
        // 尝试找到JSON对象
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        
        if (start != -1 && end != -1) {
            return text.substring(start, end + 1);
        }
        
        return text;
    }
}
