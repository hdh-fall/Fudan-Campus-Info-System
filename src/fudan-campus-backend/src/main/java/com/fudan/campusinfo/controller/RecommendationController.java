package com.fudan.campusinfo.controller;

import com.fudan.campusinfo.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 推荐控制器
 * 提供个性化内容推荐功能
 */
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 获取推荐内容
     * @param userId 用户ID（可选，为空时返回热门内容）
     * @return 推荐列表（最多3条）
     */
    @GetMapping
    public List<Map<String, Object>> getRecommendations(@RequestParam(required = false) Integer userId) {
        return recommendationService.getRecommendations(userId);
    }
}
