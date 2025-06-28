package com.project.demo.interceptor;


import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 做统一的“登录态／Token 验证” 和 跨域设置，以及 统一日志。
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private String tokenName = "x-auth-token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(this.tokenName);

        setHeader(request, response);
        log.info("[请求接口] - {} , [请求类型] - {}",request.getRequestURL().toString(),request.getMethod());
        if (request.getRequestURL().toString().contains("/api/user/login")){
            return true;
        }
        else if (request.getRequestURL().toString().contains("/api/user/state")){
            return true;
        }
        else if (request.getRequestURL().toString().contains("/api/user/register")){
            return true;
        }
//        if (token == null || "".equals(token)){
//            if ("POST".equals(request.getMethod())){
//                return false;
//            }else {
//                return true;
//            }
//        }else {
            return true;
//        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        //更新token
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

    private void failure(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        response.setStatus(401);
//        response.getWriter().write("");
        response.sendRedirect("https://www.baidu.com");
    }

    private void setHeader(HttpServletRequest request, HttpServletResponse response) {
        // 允许所有来源（测试时 Origin 可能为 null，就回退到 *）
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin",
                origin != null && !origin.isEmpty() ? origin : "*");

        // 永远允许这些方法
        response.setHeader("Access-Control-Allow-Methods",
                "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");

        // 支持发送 Cookie
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // 只有在客户端真的发送了这个预检头时，才回写
        String reqHeaders = request.getHeader("Access-Control-Request-Headers");
        if (reqHeaders != null && !reqHeaders.isEmpty()) {
            response.setHeader("Access-Control-Allow-Headers", reqHeaders);
        }

        // 缓存 preflight 结果
        response.setHeader("Access-Control-Max-Age", "1800");

        // 对于返回 JSON，总是声明好编码
        response.setHeader("Content-Type", "application/json;charset=UTF-8");

        // 最后一定设置 200，避免 OPTIONS 走到别的逻辑
        response.setStatus(HttpStatus.OK.value());
    }

}
