package com.project.demo.service;

import com.project.demo.entity.Article;
import com.project.demo.service.base.BaseService;
import com.project.demo.dao.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文章：用于内容管理系统的文章(Article)表服务接口
 */
@Service
public class ArticleService extends BaseService<Article> {

    @Autowired
    private ArticleMapper articleMapper;

    public Article getById(Integer id) {
        return articleMapper.selectArticleById(id);
    }

}


