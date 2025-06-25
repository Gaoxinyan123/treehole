package com.project.demo.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;


/**
 * 树洞发布：(TreeHoleRelease)表实体类
 *
 */
@TableName("`tree_hole_release`")
@Data
@EqualsAndHashCode(callSuper = false)
public class TreeHoleRelease implements Serializable {

    // TreeHoleRelease编号
    @TableId(value = "tree_hole_release_id", type = IdType.AUTO)
    private Integer tree_hole_release_id;

    // 发布用户
    @TableField(value = "`publish_users`")
    private Integer publish_users;
    // 内容标题
    @TableField(value = "`content_title`")
    private String content_title;
    // 内容分类
    @TableField(value = "`content_classification`")
    private String content_classification;
    // 图片内容
    @TableField(value = "`image_content`")
    private String image_content;
    // 视频内容
    @TableField(value = "`video_content`")
    private String video_content;
    // 文字内容
    @TableField(value = "`text_content`")
    private String text_content;

    // 点击数
    @TableField(value = "hits")
    private Integer hits;

    // 点赞数
    @TableField(value = "praise_len")
    private Integer praise_len;








    // 更新时间
    @TableField(value = "update_time")
    private Timestamp update_time;

    // 创建时间
    @TableField(value = "create_time")
    private Timestamp create_time;







}
