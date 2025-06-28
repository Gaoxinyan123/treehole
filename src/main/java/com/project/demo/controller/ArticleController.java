package com.project.demo.controller;

import com.project.demo.entity.Article;
import com.project.demo.service.ArticleService;

import com.project.demo.controller.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;

/**
 * 文章：用于内容管理系统的文章(Article)表控制层
 *
 */
@RestController
@RequestMapping("article")
public class ArticleController extends BaseController<Article, ArticleService> {
    /**
     * 服务对象
     */
    @Autowired
    public ArticleController(ArticleService service) {
        setService(service);
    }

    // 排序字段白名单
    private static final Set<String> ORDER_BY_FIELDS = new HashSet<>(
            Arrays.asList("article_id", "title", "type", "hits", "praiseLen", "createTime", "updateTime")
    );

    // 合法分组字段白名单
    private static final Set<String> GROUP_BY_FIELDS = new HashSet<>(
            Arrays.asList("article_id", "title", "type", "hits", "praiseLen", "createTime", "updateTime")
    );

    /**
     * 重写add方法，添加数据验证
     * @param request
     * @return
     * @throws IOException
     */
    @PostMapping("/add")
    @Transactional
    public Map<String, Object> add(HttpServletRequest request) throws IOException {
        Map<String, Object> body = service.readBody(request.getReader());

        // 数据验证
        String title = (String) body.get("title");
        String type = (String) body.get("type");
        String content = (String) body.get("content");

        // 检查title是否为空
        if (title == null || title.trim().isEmpty()) {
            return error(40001, "文章标题不能为空");
        }

        // 检查type是否为空
        if (type == null || type.trim().isEmpty()) {
            return error(40002, "文章类型不能为空");
        }

        // 检查content是否为空
        if (content == null || content.trim().isEmpty()) {
            return error(40003, "文章内容不能为空");
        }

        // 验证通过，调用父类的add方法
        service.insert(body);
        return success(1);
    }

    @PostMapping("/set")
    @Transactional
    public Map<String, Object> set(HttpServletRequest request) throws IOException {
        Map<String, String> query = service.readQuery(request);
        Map<String, Object> body = service.readBody(request.getReader());

        // 1. 检查id是否存在
        String idStr = query.get("article_id");
        if (idStr == null || idStr.trim().isEmpty()) {
            return error(40010, "缺少article_id参数");
        }
        Integer id = Integer.valueOf(idStr);
        Article article = service.getById(id);
        if (article == null) {
            return error(40011, "指定的文章不存在");
        }

        // 2. 检查要修改的内容是否全部为空
        boolean allEmpty = true;
        for (String key : new String[]{"title", "type", "content", "description", "hits", "praiseLen"}) {
            Object v = body.get(key);
            if (v != null && !(v instanceof String && ((String)v).trim().isEmpty())) {
                allEmpty = false;
                break;
            }
        }
        if (allEmpty) {
            return error(40012, "没有可修改的内容");
        }

        // 通过校验，执行修改
        service.update(query, service.readConfig(request), body);
        return success(1);
    }

    @RequestMapping(value = "/del")
    @Transactional
    public Map<String, Object> del(HttpServletRequest request) {
        Map<String, String> query = service.readQuery(request);
        String idStr = query.get("article_id");
        if (idStr == null || idStr.trim().isEmpty()) {
            return error(40010, "缺少article_id参数");
        }
        Integer id = Integer.valueOf(idStr);
        Article article = service.getById(id);
        if (article == null) {
            return error(40011, "指定的文章不存在");
        }
        service.delete(query, service.readConfig(request));
        return success(1);
    }

    @RequestMapping("/get_obj")
    public Map<String, Object> obj(HttpServletRequest request) {
        Map<String, String> query = service.readQuery(request);
        String idStr = query.get("article_id");
        if (idStr == null || idStr.trim().isEmpty()) {
            return error(40010, "缺少article_id参数");
        }
        Integer id = Integer.valueOf(idStr);
        Article article = service.getById(id);
        if (article == null) {
            return error(40011, "指定的文章不存在");
        }
        return success(article);
    }

    @RequestMapping("/get_list")
    public Map<String, Object> getList(HttpServletRequest request) {
        Map<String, String> query = service.readQuery(request);
        Map<String, String> config = service.readConfig(request);

        // 校验 page
        String pageStr = config.get("page");
        if (pageStr != null) {
            try {
                int page = Integer.parseInt(pageStr);
                if (page <= 0) return error(400, "parseInt error, field : page");
            } catch (NumberFormatException e) {
                return error(400, "parseInt error, field : page");
            }
        }
        // 校验 size
        String sizeStr = config.get("size");
        if (sizeStr != null) {
            try {
                int size = Integer.parseInt(sizeStr);
                if (size <= 0) return error(400, "parseInt error, field : size");
            } catch (NumberFormatException e) {
                return error(400, "parseInt error, field : size");
            }
        }
        // 校验 hits_min
        String hitsMinStr = query.get("hits_min");
        if (hitsMinStr != null) {
            try {
                int hitsMin = Integer.parseInt(hitsMinStr);
                if (hitsMin < 0) return error(400, "parseInt error, field : hits_min");
            } catch (NumberFormatException e) {
                return error(400, "parseInt error, field : hits_min");
            }
        }
        // 校验 hits_max
        String hitsMaxStr = query.get("hits_max");
        if (hitsMaxStr != null) {
            try {
                int hitsMax = Integer.parseInt(hitsMaxStr);
                if (hitsMax < 0) return error(400, "parseInt error, field : hits_max");
            } catch (NumberFormatException e) {
                return error(400, "parseInt error, field : hits_max");
            }
        }
        // 逻辑校验 hits_min > hits_max
        if (hitsMinStr != null && hitsMaxStr != null) {
            int hitsMin = Integer.parseInt(hitsMinStr);
            int hitsMax = Integer.parseInt(hitsMaxStr);
            if (hitsMin > hitsMax) return error(400, "最小值大于最大值");
        }

        // 在 getList 方法中校验 orderby
        String orderby = config.get("orderby");
        if (orderby != null && !orderby.trim().isEmpty()) {
            // 只取第一个字段，去掉 desc/asc
            String field = orderby.trim().split("\\s+")[0];
            // 兼容驼峰和下划线
            field = field.replace("praise_len", "praiseLen")
                    .replace("create_time", "createTime")
                    .replace("update_time", "updateTime");
            if (!ORDER_BY_FIELDS.contains(field)) {
                // 1. 忽略排序
                config.remove("orderby");
                // 2. 或者返回错误
                // return error(400, "无效排序字段");
            }
        }

        Map<String, Object> map = service.selectToPage(query, config);
        return success(map);
    }

    @RequestMapping("/count")
    public Map<String, Object> count(HttpServletRequest request) {
        Map<String, String> config = service.readConfig(request);
        String groupby = config.get("groupby");
        if (groupby != null && !groupby.trim().isEmpty()) {
            // 分组统计，返回多条
            List<Map<String, Object>> result = service.selectGroupCountList(service.readQuery(request), config);
            return success(result);
        } else {
            // 普通统计，返回单个
            Integer value = service.selectSqlToInteger(service.groupCount(service.readQuery(request), config));
            return success(value);
        }
    }

}


