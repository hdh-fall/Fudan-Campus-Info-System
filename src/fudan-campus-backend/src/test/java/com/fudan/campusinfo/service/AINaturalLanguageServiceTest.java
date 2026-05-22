package com.fudan.campusinfo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI自然语言服务测试
 */
@SpringBootTest
public class AINaturalLanguageServiceTest {

    @Autowired(required = false)
    private AINaturalLanguageService aiService;

    @Test
    public void testAnalyzeQuestion_Canteen() {
        if (aiService == null) {
            System.out.println("⚠️ AI服务未启用，跳过测试");
            return;
        }

        Map<String, Object> result = aiService.analyzeQuestion("复旦有哪些食堂？");
        
        System.out.println("测试结果: " + result);
        
        assertNotNull(result);
        assertTrue(result.containsKey("query_type"));
    }

    @Test
    public void testAnalyzeQuestion_Building() {
        if (aiService == null) {
            System.out.println("⚠️ AI服务未启用，跳过测试");
            return;
        }

        Map<String, Object> result = aiService.analyzeQuestion("邯郸校区有哪些教学楼？");
        
        System.out.println("测试结果: " + result);
        
        assertNotNull(result);
    }

    @Test
    public void testAnalyzeQuestion_Teacher() {
        if (aiService == null) {
            System.out.println("⚠️ AI服务未启用，跳过测试");
            return;
        }

        Map<String, Object> result = aiService.analyzeQuestion("数据库课程是谁开的？");
        
        System.out.println("测试结果: " + result);
        
        assertNotNull(result);
    }

    @Test
    public void testAnalyzeQuestion_Event() {
        if (aiService == null) {
            System.out.println("⚠️ AI服务未启用，跳过测试");
            return;
        }

        Map<String, Object> result = aiService.analyzeQuestion("最近有哪些校园讲座？");
        
        System.out.println("测试结果: " + result);
        
        assertNotNull(result);
    }
}
