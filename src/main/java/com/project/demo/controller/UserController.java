package com.project.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.project.demo.entity.AccessToken;
import com.project.demo.entity.RegisteredUsers;
import com.project.demo.entity.User;
import com.project.demo.entity.UserGroup;
import com.project.demo.service.RegisteredUsersService;
import com.project.demo.service.UserGroupService;
import com.project.demo.service.UserService;

import com.project.demo.controller.base.BaseController;
import com.project.demo.util.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * 用户账户：用于保存用户登录信息(User)表控制层
 */
@Slf4j
@RestController
@RequestMapping("user")
public class UserController extends BaseController<User, UserService> {
    /**
     * 服务对象
     */
    @Autowired
    public UserController(UserService service) {
        setService(service);
    }

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private RegisteredUsersService registeredUsersService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 注册
     * @param user
     * @return
     */
    @PostMapping("register")
    @Transactional
    public Map<String, Object> signUp(@RequestBody User user) {
        // 查询用户
        Map<String, String> query = new HashMap<>();
        Map<String,Object> map = JSON.parseObject(JSON.toJSONString(user));
        query.put("username",user.getUsername());
        List list = service.selectBaseList(service.select(query, new HashMap<>()));
        if (list.size()>0){
            return error(30000, "用户已存在");
        }

        // 加密密码
        map.put("password",service.encryption(String.valueOf(map.get("password"))));

        // 插入用户表
        service.insert(map);

        // 获取插入后的用户ID
        Integer userId = null;
        if (map.get("user_id") != null) {
            userId = Integer.valueOf(map.get("user_id").toString());
        } else {
            // 如果没有返回user_id，需要重新查询
            query.clear();
            query.put("username", user.getUsername());
            List<User> userList = service.selectBaseList(service.select(query, new HashMap<>()));
            if (userList.size() > 0) {
                userId = userList.get(0).getUserId();
            }
        }

        // 如果是注册用户，同时插入registered_users表
        if ("注册用户".equals(user.getUserGroup()) && userId != null) {
            RegisteredUsers registeredUser = new RegisteredUsers();
            registeredUser.setUserId(userId);

            // 尝试从不同的字段获取手机号
            String mobilePhoneNumber = null;
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                mobilePhoneNumber = user.getPhone();
            } else if (map.get("mobile_phone_number") != null) {
                mobilePhoneNumber = map.get("mobile_phone_number").toString();
            }

            if (mobilePhoneNumber != null && !mobilePhoneNumber.isEmpty()) {
                registeredUser.setMobile_phone_number(mobilePhoneNumber);
            }

            // 插入registered_users表
            Map<String, Object> regMap = JSON.parseObject(JSON.toJSONString(registeredUser));
            registeredUsersService.insert(regMap);
        }

        return success(1);
    }

    /**
     * 找回密码
     * @param form
     * @return
     */
    @PostMapping("forget_password")
    public Map<String, Object> forgetPassword(@RequestBody User form,HttpServletRequest request) {
        JSONObject ret = new JSONObject();
        String username = form.getUsername();
        String code = form.getCode();
        String password = form.getPassword();
        // 判断条件
        if(code == null || code.length() == 0){
            return error(30000, "验证码不能为空");
        }
        if(username == null || username.length() == 0){
            return error(30000, "用户名不能为空");
        }
        if(password == null || password.length() == 0){
            return error(30000, "密码不能为空");
        }

        // 查询用户
        Map<String, String> query = new HashMap<>();
        query.put("username",username);
        List list = service.selectBaseList(service.select(query, service.readConfig(request)));
        if (list.size() > 0) {
            User o = (User) list.get(0);
            JSONObject query2 = new JSONObject();
            JSONObject form2 = new JSONObject();
            // 修改用户密码
            query2.put("user_id",o.getUserId());
            form2.put("password",service.encryption(password));
            service.update(query, service.readConfig(request), form2);
            return success(1);
        }
        return error(70000,"用户不存在");
    }

    /**
     * 登录
     * @param data
     * @param httpServletRequest
     * @return
     */
    @PostMapping("login")
    public Map<String, Object> login(@RequestBody Map<String, String> data, HttpServletRequest httpServletRequest) {
        log.info("[执行登录接口]");

        String username = data.get("username");
        String email = data.get("email");
        String phone = data.get("phone");
        String password = data.get("password");

        try {
            password = RsaUtils.decryptByPrivateKey(password);
        }catch (Exception e){
            return error(30000,"解密失败");
        }

        List resultList = null;
        Map<String, String> map = new HashMap<>();
        if(username != null && "".equals(username) == false){
            map.put("username", username);
            resultList = service.selectBaseList(service.select(map, new HashMap<>()));
        }
        else if(email != null && "".equals(email) == false){
            map.put("email", email);
            resultList = service.selectBaseList(service.select(map, new HashMap<>()));
        }
        else if(phone != null && "".equals(phone) == false){
            map.put("phone", phone);
            resultList = service.selectBaseList(service.select(map, new HashMap<>()));
        }else{
            return error(30000, "账号或密码不能为空");
        }
        if (resultList == null || password == null) {
            return error(30000, "账号或密码不能为空");
        }
        //判断是否有这个用户
        if (resultList.size()<=0){
            return error(30000,"用户不存在");
        }

        User byUsername = (User) resultList.get(0);


        Map<String, String> groupMap = new HashMap<>();
        groupMap.put("name",byUsername.getUserGroup());
        List groupList = userGroupService.selectBaseList(userGroupService.select(groupMap, new HashMap<>()));
        if (groupList.size()<1){
            return error(30000,"用户组不存在");
        }

        UserGroup userGroup = (UserGroup) groupList.get(0);

        //查询用户审核状态
        if (!StringUtils.isEmpty(userGroup.getSourceTable())){
            String res = service.selectExamineState(userGroup.getSourceTable(),byUsername.getUserId());
            if (res==null){
                return error(30000,"用户不存在");
            }
            if (!res.equals("已通过")){
                return error(30000,"该用户审核未通过");
            }
        }

        //查询用户状态
        if (byUsername.getState()!=1){
            return error(30000,"用户非可用状态，不能登录");
        }

        String face = data.get("is_face");
        String md5password = service.encryption(password);
        if (!StringUtils.isEmpty(face) && face.equals("face")){
            if (byUsername.getPassword().equals(md5password)) {
                // 返回用户信息
                JSONObject user = JSONObject.parseObject(JSONObject.toJSONString(byUsername));
                JSONObject ret = new JSONObject();
                ret.put("obj",user);
                return success(ret);
            } else {
                return error(30000, "账号或密码不正确");
            }
        }else {
            if (byUsername.getPassword().equals(md5password)) {
                // 存储Token到数据库
                AccessToken accessToken = new AccessToken();
                accessToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
                accessToken.setUser_id(byUsername.getUserId());

                Duration duration = Duration.ofSeconds(7200L);
                redisTemplate.opsForValue().set(accessToken.getToken(), accessToken,duration);

                // 返回用户信息
                JSONObject user = JSONObject.parseObject(JSONObject.toJSONString(byUsername));
                user.put("token", accessToken.getToken());
                JSONObject ret = new JSONObject();
                ret.put("obj",user);
                return success(ret);
            } else {
                return error(30000, "账号或密码不正确");
            }
        }
    }


    /**
     * 修改密码
     * @param data
     * @param request
     * @return
     */
    @PostMapping("change_password")
    public Map<String, Object> change_password(@RequestBody Map<String, String> data, HttpServletRequest request){
        // 根据Token获取UserId
        String token = request.getHeader("x-auth-token");
        Integer userId = tokenGetUserId(token);
        // 根据UserId和旧密码获取用户
        Map<String, String> query = new HashMap<>();
        String o_password = data.get("o_password");
        query.put("user_id" ,String.valueOf(userId));
        query.put("password" ,service.encryption(o_password));
        int count = service.selectBaseCount(service.count(query, service.readConfig(request)));
        if(count > 0){
            // 修改密码
            Map<String,Object> form = new HashMap<>();
            form.put("password",service.encryption(data.get("password")));
            service.update(query,service.readConfig(request),form);
            return success(1);
        }
        return error(10000,"密码修改失败！");
    }

    /**
     * open_id登录
     *
     * @param data
     * @param httpServletRequest
     * @return
     */
    @PostMapping("open_id_login")
    public Map<String, Object> openIdLogin(@RequestBody Map<String, String> data, HttpServletRequest httpServletRequest) {
        log.info("[执行登录接口]");

        String openId = data.get("open_id");
        Map<String, String> map = new HashMap<>();
        map.put("open_id", openId);

        List resultList = service.selectBaseList(service.select(map, new HashMap<>()));
        //判断是否有这个用户
        if (resultList == null || resultList.size() <= 0) {
            String username = data.get("username");
            String email = data.get("email");
            String phone = data.get("phone");
            String password = data.get("password");

            map = new HashMap<>();
            if (username != null && "".equals(username) == false) {
                map.put("username", username);
                resultList = service.selectBaseList(service.select(map, new HashMap<>()));
            } else if (email != null && "".equals(email) == false) {
                map.put("email", email);
                resultList = service.selectBaseList(service.select(map, new HashMap<>()));
            } else if (phone != null && "".equals(phone) == false) {
                map.put("phone", phone);
                resultList = service.selectBaseList(service.select(map, new HashMap<>()));
            } else {
                return error(30000, "账号或密码不能为空");
            }
            if (resultList == null || password == null) {
                return error(30000, "账号或密码不能为空");
            }
            //判断是否有这个用户
            if (resultList.size() <= 0) {
                return error(30000, "用户不存在");
            }

            User byUsername = (User) resultList.get(0);


            Map<String, String> groupMap = new HashMap<>();
            groupMap.put("name", byUsername.getUserGroup());
            List groupList = userGroupService.selectBaseList(userGroupService.select(groupMap, new HashMap<>()));
            if (groupList.size() < 1) {
                return error(30000, "用户组不存在");
            }

            UserGroup userGroup = (UserGroup) groupList.get(0);

            //查询用户审核状态
            if (!StringUtils.isEmpty(userGroup.getSourceTable())) {
                String res = service.selectExamineState(userGroup.getSourceTable(), byUsername.getUserId());
                if (res == null) {
                    return error(30000, "用户不存在");
                }
                if (!res.equals("已通过")) {
                    return error(30000, "该用户审核未通过");
                }
            }

            //查询用户状态
            if (byUsername.getState() != 1) {
                return error(30000, "用户非可用状态，不能登录");
            }

            String md5password = service.encryption(password);
            if (byUsername.getPassword().equals(md5password)) {
                Map<String, String> query = new HashMap<>();
                query.put("user_id", String.valueOf(byUsername.getUserId()));
                Map<String, Object> form = new HashMap<>();
                form.put("open_id", openId);
                service.update(query, new HashMap<>(), form);
                // 存储Token到数据库
                AccessToken accessToken = new AccessToken();
                accessToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
                accessToken.setUser_id(byUsername.getUserId());

                Duration duration = Duration.ofSeconds(7200L);
                redisTemplate.opsForValue().set(accessToken.getToken(), accessToken, duration);

                // 返回用户信息
                JSONObject user = JSONObject.parseObject(JSONObject.toJSONString(byUsername));
                user.put("token", accessToken.getToken());
                JSONObject ret = new JSONObject();
                ret.put("obj", user);
                return success(ret);
            } else {
                return error(30000, "账号或密码不正确");
            }
        }

        User byOpenId = (User) resultList.get(0);

        Map<String, String> groupMap = new HashMap<>();
        groupMap.put("name", byOpenId.getUserGroup());
        List groupList = userGroupService.selectBaseList(userGroupService.select(groupMap, new HashMap<>()));
        if (groupList.size() < 1) {
            return error(30000, "用户组不存在");
        }

        UserGroup userGroup = (UserGroup) groupList.get(0);

        //查询用户审核状态
        if (!StringUtils.isEmpty(userGroup.getSourceTable())) {
            String res = service.selectExamineState(userGroup.getSourceTable(), byOpenId.getUserId());
            if (res == null) {
                return error(30000, "用户不存在");
            }
            if (!res.equals("已通过")) {
                return error(30000, "该用户审核未通过");
            }
        }

        //查询用户状态
        if (byOpenId.getState() != 1) {
            return error(30000, "用户非可用状态，不能登录");
        }

        // 存储Token到数据库
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
        accessToken.setUser_id(byOpenId.getUserId());

        Duration duration = Duration.ofSeconds(7200L);
        redisTemplate.opsForValue().set(accessToken.getToken(), accessToken, duration);

        // 返回用户信息
        JSONObject user = JSONObject.parseObject(JSONObject.toJSONString(byOpenId));
        user.put("token", accessToken.getToken());
        JSONObject ret = new JSONObject();
        ret.put("obj", user);
        return success(ret);
    }

    /**
     * 登录态
     * @param request
     * @return
     */
    @GetMapping("state")
    public Map<String, Object> state(HttpServletRequest request) {
        JSONObject ret = new JSONObject();
        // 获取状态
        String token = request.getHeader("x-auth-token");

        // 根据登录态获取用户ID
        Integer userId = tokenGetUserId(token);

        log.info("[返回userId] {}",userId);
        if(userId == null || userId == 0){
            return error(10000,"用户未登录!");
        }

        // 根据用户ID获取用户
        Map<String,String> query = new HashMap<>();
        query.put("user_id" ,String.valueOf(userId));

        // 根据用户ID获取
        List resultList = service.selectBaseList(service.select(query,service.readConfig(request)));
        if (resultList.size() > 0) {
            JSONObject user = JSONObject.parseObject(JSONObject.toJSONString(resultList.get(0)));
            user.put("token",token);
            ret.put("obj",user);
            return success(ret);
        } else {
            return error(10000,"用户未登录!");
        }
    }

    /**
     * 登录态
     * @param request
     * @return
     */
    @GetMapping("quit")
    public Map<String, Object> quit(HttpServletRequest request) {
        String token = request.getHeader("x-auth-token");
        try{
            redisTemplate.delete(token);
        }catch (Exception e){
            e.printStackTrace();
        }
        return success("退出登录成功！");
    }

    /**
     * 获取登录用户ID
     * @param token
     * @return
     */
    public Integer tokenGetUserId(String token) {
        log.info("[获取的token] {}",token);
        // 根据登录态获取用户ID
        if(token == null || "".equals(token)){
            return 0;
        }
        AccessToken byToken = (AccessToken) redisTemplate.opsForValue().get(token);
        if(byToken == null){
            return 0;
        }
        return byToken.getUser_id();
    }

    /**
     * 重写add
     * @return
     */
    @PostMapping("/add")
    @Transactional
    public Map<String, Object> add(HttpServletRequest request) throws IOException {
        Map<String,Object> map = service.readBody(request.getReader());
        map.put("password",service.encryption(String.valueOf(map.get("password"))));
        service.insert(map);
        return success(1);
    }

}
