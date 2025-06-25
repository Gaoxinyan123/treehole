package com.project.demo.manager;

import com.project.demo.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Component
public class ArticleManager {

    @Autowired
    private ArticleService articleService;

    public void addNewArticle() {
        // 1. 准备文章数据
        Map<String, Object> newArticleData = new HashMap<>();
        newArticleData.put("title", "一篇全新的文章");
        newArticleData.put("type", "tech");
        newArticleData.put("content", "这是文章的正文内容...");
        newArticleData.put("hits", 0);
        newArticleData.put("praiseLen", 0); // 注意：这里使用 Java 实体类中的驼峰式命名
        newArticleData.put("createTime", new Timestamp(System.currentTimeMillis()));
        // 您可以根据 Article.java 实体类添加更多字段

        // 2. 调用 service 的 insert 方法
        Integer newArticleId = articleService.insert(newArticleData);

        // 3. (可选) 验证是否成功
        if (newArticleId != null) {
            System.out.println("新文章插入成功！ID 是：" + newArticleId);
        } else {
            System.out.println("文章插入失败。");
        }
    }
}