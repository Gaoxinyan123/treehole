package com.project.demo.controller.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.project.demo.service.base.BaseService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
@Slf4j
public class BaseController<E, S extends BaseService<E>> {

    @Setter
    protected S service;


    @PostMapping("/add")
    @Transactional
    public Map<String, Object> add(HttpServletRequest request) throws IOException {
        service.insert(service.readBody(request.getReader()));
        return success(1);
    }

    @Transactional
    public Map<String, Object> addMap(Map<String,Object> map){
        service.insert(map);
        return success(1);
    }

    @PostMapping("/set")
    @Transactional
    public Map<String, Object> set(HttpServletRequest request) throws IOException {
        service.update(service.readQuery(request), service.readConfig(request), service.readBody(request.getReader()));
        return success(1);
    }


    @RequestMapping(value = "/del")
    @Transactional
    public Map<String, Object> del(HttpServletRequest request) {
        service.delete(service.readQuery(request), service.readConfig(request));
        return success(1);
    }

    @RequestMapping("/get_obj")
    public Map<String, Object> obj(HttpServletRequest request) {
        List resultList = service.selectBaseList(service.select(service.readQuery(request), service.readConfig(request)));
        if (resultList.size() > 0) {
            return success(resultList.get(0));
        } else {
            return error(404, "未找到数据");
        }
    }


    @RequestMapping("/get_list")
    public Map<String, Object> getList(HttpServletRequest request) {
        Map<String, Object> map = service.selectToPage(service.readQuery(request), service.readConfig(request));
        return success(map);
    }

    @RequestMapping("/list_group")
    public Map<String, Object> listGroup(HttpServletRequest request) {
        Map<String,Object> map = service.selectToList(service.readQuery(request), service.readConfig(request));
        Map<String,Object> result = new HashMap<>();
        result.put("result",map);
        return result;
    }

    @RequestMapping("/bar_group")
    public Map<String, Object> barGroup(HttpServletRequest request) {
        Map<String, Object> map = service.selectBarGroup(service.readQuery(request), service.readConfig(request));
        return success(map);
    }

    @RequestMapping(value = {"/count_group", "/count"})
    public Map<String, Object> count(HttpServletRequest request) {
        Integer value= service.selectSqlToInteger(service.groupCount(service.readQuery(request), service.readConfig(request)));
        return success(value);
    }

    @RequestMapping(value = {"/sum_group", "/sum"})
    public Map<String, Object> sum(HttpServletRequest request) {
        Integer value = service.selectSqlToInteger(service.sum(service.readQuery(request), service.readConfig(request)));
        return success(value);
    }

    @RequestMapping(value = {"/avg_group", "/avg"})
    public Map<String, Object> avg(HttpServletRequest request) {
        Integer value = service.selectSqlToInteger(service.avg(service.readQuery(request), service.readConfig(request)));
        return success(value);
    }


    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        log.info("进入方法");
        if (file.isEmpty()) {
            return error(30000, "没有选择文件");
        }
        try {
            //判断有没路径，没有则创建
            String filePath = System.getProperty("user.dir") + "/src/main/resources/static/";
            File targetDir = new File(filePath);
            if (!targetDir.exists() && !targetDir.isDirectory()) {
                if (targetDir.mkdirs()) {
                    log.info("创建目录成功");
                } else {
                    log.error("创建目录失败");
                }
            }
//            String path = ResourceUtils.getURL("classpath:").getPath() + "static/upload/";
//            String filePath = path.replace('/', '\\').substring(1, path.length());
            String fileName = file.getOriginalFilename();
            File dest = new File(filePath + fileName);
            log.info("文件路径:{}", dest.getPath());
            log.info("文件名:{}", dest.getName());
            file.transferTo(dest);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("url", "/api/upload/" + fileName);
            return success(jsonObject);
        } catch (IOException e) {
            log.info("上传失败：{}", e.getMessage());
        }
        return error(30000, "上传失败");
    }

    public Map<String, Object> success(Object o) {
        Map<String, Object> map = new HashMap<>();
        if (o == null) {
            map.put("result", null);
            return map;
        }
        if (o instanceof List) {
            if (((List) o).size() == 1) {
                o =  ((List) o).get(0);
                map.put("result", o);
            }else {
                String jsonString = JSONObject.toJSONString(o, SerializerFeature.WriteMapNullValue);
                JSONArray objects = service.covertArray(JSONObject.parseArray(jsonString));
                map.put("result", objects);
            }
        } else if (o instanceof Integer || o instanceof String) {
            map.put("result", o);
        } else {
            String jsonString = JSONObject.toJSONString(o, SerializerFeature.WriteMapNullValue);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            JSONObject j = service.covertObject(jsonObject);
            map.put("result", j);
        }
        return map;
    }

    public Map<String, Object> error(Integer code, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", new HashMap<String, Object>(4) {{
            put("code", code);
            put("message", message);
        }});
        return map;
    }
}
