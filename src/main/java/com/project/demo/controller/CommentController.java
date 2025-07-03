package com.project.demo.controller;

import com.project.demo.entity.Comment;
import com.project.demo.service.CommentService;
import com.project.demo.dao.CommentMapper;

import com.project.demo.controller.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论：(Comment)表控制层
 *
 */
@RestController
@RequestMapping("comment")
public class CommentController extends BaseController<Comment, CommentService> {
    /**
     * 服务对象
     */
    @Autowired
    public CommentController(CommentService service) {
        setService(service);
    }

    @Autowired
    private CommentMapper commentMapper;

    @PostMapping("/del")
    public Map<String, Object> deleteById(@RequestParam("comment_id") Long commentId) {
        int removed = commentMapper.deleteCommentById(commentId);
        if (removed > 0) {
            return success(1);
        } else {
            return error(404, "评论不存在或已被删除");
        }
    }
}


