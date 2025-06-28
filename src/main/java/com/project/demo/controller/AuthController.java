package com.project.demo.controller;

import com.project.demo.entity.Auth;
import com.project.demo.service.AuthService;

import com.project.demo.controller.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定制授权(Auth)表控制层
 */
@RestController
@RequestMapping("auth")
public class AuthController extends BaseController<Auth, AuthService> {
    /**
     * 服务对象
     */
    @Autowired
    public AuthController(AuthService service) {
        setService(service);
    }
    /**
     * 获取权限列表
     * 支持按用户组查询权限配置
     */
    @RequestMapping("/get_list")
    public Map<String, Object> getList(HttpServletRequest request) {
        // 获取查询参数
        String userGroup = request.getParameter("user_group");
        String position = request.getParameter("position");
        String size = request.getParameter("size");

        // 构建查询条件
        Auth queryAuth = new Auth();
        if (userGroup != null && !userGroup.isEmpty()) {
            queryAuth.setUserGroup(userGroup);
        }
        if (position != null && !position.isEmpty()) {
            queryAuth.setPosition(position);
        }

        // 查询权限列表
        List<Auth> authList = service.selectAuthList(queryAuth);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("list", authList);

        // 如果size参数为"0"，返回所有数据，否则返回分页数据
        if ("0".equals(size)) {
            result.put("result", data);
        } else {
            // 使用父类的分页查询方法
            return super.getList(request);
        }

        return result;
    }
}
