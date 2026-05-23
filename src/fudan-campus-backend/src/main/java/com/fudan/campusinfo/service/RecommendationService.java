package com.fudan.campusinfo.service;

import com.fudan.campusinfo.repository.QueryRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务
 * 基于用户查询历史提供个性化推荐
 */
@Service
public class RecommendationService {

    @Autowired
    private QueryRecordRepository queryRecordRepository;

    /**
     * 获取推荐内容
     * @param userId 用户ID（可选）
     * @return 推荐列表
     */
    public List<Map<String, Object>> getRecommendations(Integer userId) {
        if (userId != null) {
            // 尝试获取个性化推荐
            List<Map<String, Object>> personalized = getPersonalizedRecommendations(userId);
            if (!personalized.isEmpty()) {
                return personalized;
            }
        }
        
        // 返回热门/默认推荐
        return getDefaultRecommendations();
    }

    /**
     * 获取个性化推荐（基于用户查询历史）
     */
    private List<Map<String, Object>> getPersonalizedRecommendations(Integer userId) {
        try {
            // 查询该用户最近的查询记录
            List<Object[]> recentQueries = queryRecordRepository.findRecentQueriesByUserId(userId, 10);
                
            if (recentQueries.isEmpty()) {
                System.out.println("📭 用户没有查询记录，返回默认推荐");
                return getDefaultRecommendations(); // 返回默认推荐而不是空列表
            }
                
            // 统计各类别的查询频率，使用LinkedHashMap保持顺序
            Map<String, Long> categoryCount = new LinkedHashMap<>();
            for (Object[] row : recentQueries) {
                String category = (String) row[0];
                // 过滤无效的category
                if (category != null && !category.isEmpty() && !category.equals("GENERAL_SEARCH")) {
                    String mappedCategory = mapChineseCategory(category);
                    // 只统计有效的类别
                    if (!mappedCategory.equals("GENERAL_SEARCH")) {
                        categoryCount.merge(mappedCategory, 1L, Long::sum);
                    }
                }
            }
                
            if (categoryCount.isEmpty()) {
                System.out.println("⚠️ 所有查询记录都是无效类别，返回默认推荐");
                return getDefaultRecommendations();
            }
                
            // 按频率排序，取前3个类别（确保不重复）
            List<String> topCategories = categoryCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
            System.out.println("✅ 个性化推荐类别: " + topCategories);
                
            // 生成推荐内容（已经是去重后的）
            List<Map<String, Object>> recommendations = new ArrayList<>();
            for (String category : topCategories) {
                Map<String, Object> item = createRecommendationFromCategory(category);
                if (item != null) {
                    recommendations.add(item);
                }
            }
                
            return recommendations;
        } catch (Exception e) {
            System.err.println("❌ 获取个性化推荐失败: " + e.getMessage());
            e.printStackTrace();
            return getDefaultRecommendations(); // 出错时返回默认推荐
        }
    }

    /**
     * 获取默认推荐（热门内容）
     */
    private List<Map<String, Object>> getDefaultRecommendations() {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        // 食堂推荐
        recommendations.add(createRecommendation(
            "FACILITY_CANTEEN",
            "热门食堂推荐",
            "旦苑、南苑等校园美食等你来探索",
            "facility",
            "食堂"  // 匹配前端tab name
        ));
        
        // 活动推荐
        recommendations.add(createRecommendation(
            "EVENT",
            "近期校园活动",
            "精彩讲座、比赛不容错过",
            "event",
            ""
        ));
        
        // 图书馆推荐
        recommendations.add(createRecommendation(
            "FACILITY_LIBRARY",
            "图书馆自习室",
            "安静舒适的学习环境",
            "facility",
            "阅览室"  // 匹配前端tab name
        ));
        
        return recommendations;
    }

    /**
     * 根据类别创建推荐项
     * 支持英文枚举（FACILITY_CANTEEN）和中文（食堂、设施等）
     */
    private Map<String, Object> createRecommendationFromCategory(String category) {
        // 先进行类别映射，将中文转换为英文枚举
        String mappedCategory = mapChineseCategory(category);
        
        switch (mappedCategory) {
            case "FACILITY_CANTEEN":
                return createRecommendation(
                    mappedCategory,
                    "您可能感兴趣的食堂",
                    "探索更多校园美食",
                    "facility",
                    "食堂"  // 匹配前端tab name
                );
            case "FACILITY_CAFE":
                return createRecommendation(
                    mappedCategory,
                    "咖啡厅推荐",
                    "休闲放松的好去处",
                    "facility",
                    "咖啡厅"  // 匹配前端tab name
                );
            case "FACILITY_LIBRARY":
                return createRecommendation(
                    mappedCategory,
                    "图书馆推荐",
                    "安静的阅读和学习空间",
                    "facility",
                    "阅览室"  // 匹配前端tab name
                );
            case "FACILITY_STUDY_ROOM":
                return createRecommendation(
                    mappedCategory,
                    "自习室推荐",
                    "舒适的学习环境",
                    "facility",
                    "自习室"  // 匹配前端tab name
                );
            case "FACILITY_LAB":
                return createRecommendation(
                    mappedCategory,
                    "实验室推荐",
                    "探索科技创新空间",
                    "facility",
                    "实验室"  // 匹配前端tab name
                );
            case "BUILDING":
                return createRecommendation(
                    mappedCategory,
                    "校园建筑探索",
                    "了解复旦各校区建筑",
                    "campus",
                    ""
                );
            case "CAMPUS":
                return createRecommendation(
                    mappedCategory,
                    "校区信息查询",
                    "探索复旦多个校区的风采",
                    "campus",
                    ""
                );
            case "COURSE":
                return createRecommendation(
                    mappedCategory,
                    "课程推荐",
                    "发现优质课程资源",
                    "course",
                    ""
                );
            case "TEACHER":
                return createRecommendation(
                    mappedCategory,
                    "教师信息查询",
                    "了解授课教师信息",
                    "course",
                    ""
                );
            case "EVENT":
                return createRecommendation(
                    mappedCategory,
                    "校园活动",
                    "精彩活动不错过",
                    "event",
                    ""
                );
            case "AI_SQL":
                return createRecommendation(
                    mappedCategory,
                    "智能问答",
                    "试试用自然语言提问",
                    "home",
                    ""
                );
            case "GENERAL_SEARCH":
                return createRecommendation(
                    mappedCategory,
                    "校园搜索",
                    "探索校园精彩内容",
                    "home",
                    ""
                );
            default:
                // 未知类别，返回通用推荐
                return createRecommendation(
                    mappedCategory,
                    "为您推荐",
                    "探索校园精彩内容",
                    "home",
                    ""
                );
        }
    }
    
    /**
     * 将中文类别映射为英文枚举
     * 支持历史数据中的中文category
     */
    private String mapChineseCategory(String category) {
        if (category == null || category.isEmpty()) {
            return "GENERAL_SEARCH";
        }
        
        // 中文到英文枚举的映射
        switch (category) {
            case "食堂":
            case "餐厅":
                return "FACILITY_CANTEEN";
            case "咖啡厅":
            case "咖啡":
                return "FACILITY_CAFE";
            case "图书馆":
            case "阅览室":
                return "FACILITY_LIBRARY";
            case "自习室":
            case "自习":
                return "FACILITY_STUDY_ROOM";
            case "实验室":
            case "机房":
            case "实验":
                return "FACILITY_LAB";
            case "设施":  // 通用设施，根据问题内容判断，这里默认返回食堂
                return "FACILITY_CANTEEN";
            case "建筑":
            case "教学楼":
            case "宿舍":
                return "BUILDING";
            case "校区":
                return "CAMPUS";
            case "课程":
                return "COURSE";
            case "教师":
            case "老师":
            case "教授":
                return "TEACHER";
            case "活动":
            case "讲座":
            case "比赛":
            case "展览":
                return "EVENT";
            default:
                // 如果已经是英文枚举，直接返回
                if (category.matches("[A-Z_]+")) {
                    return category;
                }
                return "GENERAL_SEARCH";
        }
    }

    /**
     * 创建推荐对象
     */
    private Map<String, Object> createRecommendation(String category, String title, 
                                                      String description, String targetPage, 
                                                      String searchKeyword) {
        Map<String, Object> recommendation = new HashMap<>();
        recommendation.put("category", category);
        recommendation.put("title", title);
        recommendation.put("description", description);
        recommendation.put("targetPage", targetPage);
        recommendation.put("searchKeyword", searchKeyword);
        // 对于设施类型，添加filterType用于前端筛选
        if ("facility".equals(targetPage) && searchKeyword != null && !searchKeyword.isEmpty()) {
            recommendation.put("filterType", searchKeyword);
        } else {
            recommendation.put("filterType", null);
        }
        return recommendation;
    }
}
