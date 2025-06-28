package com.project.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo.entity.Article;
import com.project.demo.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    // 加这一行，mock 掉 MyBatis 的 Mapper
    @MockBean
    private com.project.demo.dao.ArticleMapper articleMapper;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void testAddAndGetAndList() throws Exception {
        // 1) stub insert，不抛异常
        doNothing().when(articleService).insert(any(Map.class));

        // 2) 预先准备一个"存好的"对象
        Article a = new Article();
        a.setArticleId(1);
        a.setTitle("JUnit5 测试文章");
        a.setType("test");
        a.setCreateTime(new Timestamp(System.currentTimeMillis()));
        a.setHits(0);
        a.setPraise_len(0);

        // 3) stub get_obj
        when(articleService.selectBaseList(any()))
                .thenReturn(Collections.singletonList(a));

        // 4) stub get_list
        Map<String,Object> page = new HashMap<>();
        page.put("list", Collections.singletonList(a));
        page.put("count", 1);
        when(articleService.selectToPage(any(), any()))
                .thenReturn(page);

        String json = objectMapper.writeValueAsString(a);

        // === 1) add 请求 ===
        mockMvc.perform(post("/article/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1));

        // === 2) get_obj 请求 ===
        mockMvc.perform(get("/article/get_obj")
                .param("articleId", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.obj.articleId").value(1))
                .andExpect(jsonPath("$.result.obj.title").value("JUnit5 测试文章"));

        // === 3) get_list 请求 ===
        mockMvc.perform(get("/article/get_list")
                .param("type", "test")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.list[0].articleId").value(1))
                .andExpect(jsonPath("$.result.count").value(1));
    }

    @Test
    void testSet() throws Exception {
        doNothing().when(articleService).update(any(), any(), any());

        Article a = new Article();
        a.setArticleId(1);
        a.setTitle("Updated Title");

        String json = objectMapper.writeValueAsString(a);

        mockMvc.perform(post("/article/set")
                .param("articleId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1));
    }

    @Test
    void testDel() throws Exception {
        doNothing().when(articleService).delete(any(), any());

        mockMvc.perform(get("/article/del")
                .param("articleId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1));
    }

    @Test
    void testListGroup() throws Exception {
        Map<String, Object> group = new HashMap<>();
        group.put("test", 1);
        when(articleService.selectToList(any(), any())).thenReturn(group);

        mockMvc.perform(get("/article/list_group")
                .param("type", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.test").value(1));
    }

    @Test
    void testBarGroup() throws Exception {
        Map<String, Object> group = new HashMap<>();
        group.put("test", 1);
        when(articleService.selectBarGroup(any(), any())).thenReturn(group);

        mockMvc.perform(get("/article/bar_group")
                .param("type", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.test").value(1));
    }

    @Test
    void testCount() throws Exception {
        when(articleService.selectSqlToInteger(any())).thenReturn(5);

        mockMvc.perform(get("/article/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(5));
    }

    @Test
    void testSum() throws Exception {
        when(articleService.selectSqlToInteger(any())).thenReturn(100);

        mockMvc.perform(get("/article/sum")
                .param("field", "hits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(100));
    }

    @Test
    void testAvg() throws Exception {
        when(articleService.selectSqlToInteger(any())).thenReturn(20);

        mockMvc.perform(get("/article/avg")
                .param("field", "hits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(20));
    }
}
