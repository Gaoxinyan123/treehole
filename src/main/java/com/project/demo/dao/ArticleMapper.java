package com.project.demo.dao;

import com.project.demo.dao.base.BaseMapper;
import com.project.demo.entity.Article;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文章：用于内容管理系统的文章Mapper接口
 *
 * 自动拥有了：
 * insert(E)、deleteById(Serializable)、updateById(E)、selectById(Serializable) 等常用 CRUD 方法
 *
 * selectPage(...)、selectList(...) 等分页和列表查询方法
 * 无需再在 XML 或注解里重复写这些通用 SQL。
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article>
{
    /**
     * 查询文章：用于内容管理系统的文章
     *	根据主键 articleId 查询单篇文章
     * @param articleId 文章：用于内容管理系统的文章ID
     * @return 文章：用于内容管理系统的文章
     */
    Article selectArticleById(Integer articleId);

    /**
     * 查询文章：用于内容管理系统的文章列表
     *根据传入的 Article 条件（如字段非空表示要做筛选）查询多条记录
     * @param article 文章：用于内容管理系统的文章
     * @return 文章：用于内容管理系统的文章集合
     */
    List<Article> selectArticleList(Article article);

    /**
     * 新增文章：用于内容管理系统的文章
     *	插入一条新的文章记录
     * @param article 文章：用于内容管理系统的文章
     * @return 结果
     */
    int insertArticle(Article article);

    /**
     * 修改文章：用于内容管理系统的文章
     *	根据实体主键更新对应的文章记录
     * @param article 文章：用于内容管理系统的文章
     * @return 结果
     */
    int updateArticle(Article article);

    /**
     * 删除文章：用于内容管理系统的文章
     *	根据 articleId 删除单条文章
     * @param articleId 文章：用于内容管理系统的文章ID
     * @return 结果
     */
    int deleteArticleById(Integer articleId);

    /**
     * 批量删除文章：用于内容管理系统的文章
     *	根据一组 articleIds 批量删除多条文章
     * @param articleIds 需要删除的数据ID
     * @return 结果
     */
    int deleteArticleByIds(Integer[] articleIds);
}
