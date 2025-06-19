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
 * 内容举报：(ContentReporting)表实体类
 *
 */
@TableName("`content_reporting`")
@Data
@EqualsAndHashCode(callSuper = false)
public class ContentReporting implements Serializable {

    // ContentReporting编号
    @TableId(value = "content_reporting_id", type = IdType.AUTO)
    private Integer content_reporting_id;

    // 发布用户
    @TableField(value = "`publish_users`")
    private Integer publish_users;
    // 举报用户
    @TableField(value = "`report_users`")
    private Integer report_users;
    // 内容标题
    @TableField(value = "`content_title`")
    private String content_title;
    // 举报类型
    @TableField(value = "`report_type`")
    private String report_type;
    // 举报详情
    @TableField(value = "`reporting_details`")
    private String reporting_details;



    // 审核状态
    @TableField(value = "examine_state")
    private String examine_state;



    // 审核回复
    @TableField(value = "examine_reply")
    private String examine_reply;




    // 更新时间
    @TableField(value = "update_time")
    private Timestamp update_time;

    // 创建时间
    @TableField(value = "create_time")
    private Timestamp create_time;







}
