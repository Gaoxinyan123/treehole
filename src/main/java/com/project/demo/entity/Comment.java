package com.project.demo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Timestamp;
/*定义了“表名—字段名—属性名”的映射关系。
Mapper + BaseMapper 提供了开箱即用的 CRUD 接口。
Service + Controller 组成经典的三层架构，用来封装业务逻辑并对外暴露 RESTful API。*/

/**
 * 评论：(Comment)表实体类
 *

 */
@TableName("comment")
@Data
@EqualsAndHashCode(callSuper = false)
public class Comment implements Serializable {

    private static final long serialVersionUID = 897384967462600611L;

    /**
     * 评论ID：
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Integer commentId;

    /**
     * 评论人ID：
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 回复评论ID：空为0
     */
    @TableField(value = "reply_to_id")
    private Integer replyToId;

    /**
     * 内容：
     */
    @TableField(value = "content")
    private String content;

    /**
     * 昵称：
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 头像地址：[0,255]
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 创建时间：
     */
    @TableField(value = "create_time")
    private Timestamp createTime;

    /**
     * 更新时间：
     */
    @TableField(value = "update_time")
    private Timestamp updateTime;

    /**
     * 来源表：
     */
    @TableField(value = "source_table")
    private String sourceTable;

    /**
     * 来源字段：
     */
    @TableField(value = "source_field")
    private String sourceField;

    /**
     * 来源ID：
     */
    @TableField(value = "source_id")
    private Integer sourceId;

}

