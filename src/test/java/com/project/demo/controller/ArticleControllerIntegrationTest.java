package com.project.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo.service.ArticleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ArticleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ObjectMapper objectMapper;

    private Integer savedId;

    @BeforeEach
    void setup() {
        Map<String, Object> a = new HashMap<>();
        a.put("title", "集成测试文章");
        a.put("type", "test");
        a.put("hits", 100);
        a.put("praise_len", 0);
        a.put("createTime", new Timestamp(System.currentTimeMillis()));
        this.savedId = articleService.insert(a);
    }
    // 有效数据模板
    private Map<String, Object> validArticle() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "标题");
        body.put("type", "news");
        body.put("hits", 0);
        body.put("praise_len", 0);
        body.put("create_time", "2025-06-25 12:00:00");
        return body;
    }
    @Test
    void T00_setArticle_AllValid() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("title", "更新后的标题");
        updateBody.put("type", "news");
        updateBody.put("hits", 10);
        updateBody.put("praise_len", 5);
        updateBody.put("create_time", "2025-06-25 12:00:00");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", String.valueOf(this.savedId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1));

        // 可选：再查一次，断言内容已更新
        mockMvc.perform(get("/article/get_obj").param("article_id", String.valueOf(this.savedId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.obj.title").value("更新后的标题"))
                .andExpect(jsonPath("$.result.obj.type").value("news"))
                .andExpect(jsonPath("$.result.obj.hits").value(10))
                .andExpect(jsonPath("$.result.obj.praise_len").value(5));
    }
    // T01: article_id = -1
    @Test
    void T01_setArticle_ArticleIdNegative() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("title", "新标题");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", "-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("article_id 必须为正整数")));
    }

    // T02: article_id = 0
    @Test
    void T02_setArticle_ArticleIdZero() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("title", "新标题");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("article_id 必须为正整数")));
    }

    // T03: article_id = 100（假设不存在）
    @Test
    void T03_setArticle_ArticleIdNotExist() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("title", "新标题");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("找不到 ID")));
    }

    // T04: article_id = 空串
    @Test
    void T04_setArticle_ArticleIdEmpty() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("title", "新标题");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("article_id 不能为空")));
    }

    // T05: article_id = abc
    @Test
    void T05_setArticle_ArticleIdNotNumber() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("title", "新标题");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", "abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("article_id 必须为数字")));
    }

    // T06: title 为空串
    @Test
    void T06_setArticle_TitleEmpty() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("title", "");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", String.valueOf(this.savedId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("title 不能为空")));
    }

    // T07: title 超长
    @Test
    void T07_setArticle_TitleTooLong() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("title", new String(new char[501]).replace('\0', 'A'));
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", String.valueOf(this.savedId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("title 长度超限")));
    }

    // T08: type 不存在
    @Test
    void T08_setArticle_TypeNotExist() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("type", "foo");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", String.valueOf(this.savedId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("type 非法")));
    }

    // T09: type 为空
    @Test
    void T09_setArticle_TypeEmpty() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("type", "");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", String.valueOf(this.savedId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("type 不能为空")));
    }

    // T10: hits < 0
    @Test
    void T10_setArticle_HitsNegative() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("hits", -5);
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", String.valueOf(this.savedId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("hits 必须 ≥ 0")));
    }

    // T11: hits 非数字
    @Test
    void T11_setArticle_HitsNotNumber() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("hits", "xyz");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", String.valueOf(this.savedId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("hits 必须为数字")));
    }

    // T12: praise_len < 0
    @Test
    void T12_setArticle_PraiseLenNegative() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("praise_len", -3);
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", String.valueOf(this.savedId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("praise_len 必须 ≥ 0")));
    }

    // T13: praise_len 非数字
    @Test
    void T13_setArticle_PraiseLenNotNumber() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("praise_len", "abc");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", String.valueOf(this.savedId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("praise_len 必须为数字")));
    }

    // T14: create_time 为空
    @Test
    void T14_setArticle_CreateTimeEmpty() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("create_time", "");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", String.valueOf(this.savedId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("create_time 不能为空")));
    }

    // T15: create_time 格式错
    @Test
    void T15_setArticle_CreateTimeFormatError() throws Exception {
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("create_time", "2025/06/25");
        String json = objectMapper.writeValueAsString(updateBody);

        mockMvc.perform(post("/article/set")
                .param("article_id", String.valueOf(this.savedId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("日期格式非法")));
    }
}
