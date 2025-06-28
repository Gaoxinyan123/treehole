package com.project.demo.entity.base;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 所有的实体（User, Article, TreeHole 等）都可以 extends BaseEntity，这样就自动拥有 id 主键字段
 */
@Data
public class BaseEntity {

    @TableId
    private Integer id;
}

