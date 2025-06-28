package com.project.demo.service;

import com.project.demo.entity.Auth;
import com.project.demo.service.base.BaseService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 定制授权(Auth)表服务接口
 *
 */
@Service
public class AuthService extends BaseService<Auth> {
    /**
     * 查询定制授权列表
     *
     * @param auth 定制授权查询条件
     * @return 定制授权集合
     */
    public List<Auth> selectAuthList(Auth auth) {
        // 构建查询SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM auth WHERE 1=1");

        if (auth.getUserGroup() != null && !auth.getUserGroup().isEmpty()) {
            sql.append(" AND user_group = '").append(auth.getUserGroup()).append("'");
        }

        if (auth.getPosition() != null && !auth.getPosition().isEmpty()) {
            sql.append(" AND position = '").append(auth.getPosition()).append("'");
        }

        sql.append(" ORDER BY auth_id ASC");

        // 使用父类的selectBaseList方法
        return selectBaseList(sql.toString());
    }
}


