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
 * 内容分类：(ContentClassification)表实体类
 *
 */
@TableName("`content_classification`")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContentClassification implements Serializable {

    // ContentClassification编号
    @TableId(value = "content_classification_id", type = IdType.AUTO)
    private Integer content_classification_id;

    // 内容分类
    @TableField(value = "`content_classification`")
    private String content_classification;










    // 更新时间
    @TableField(value = "update_time")
    private Timestamp update_time;

    // 创建时间
    @TableField(value = "create_time")
    private Timestamp create_time;







}
