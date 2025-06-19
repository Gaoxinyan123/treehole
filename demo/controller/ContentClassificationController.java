package com.project.demo.controller;

import com.project.demo.entity.ContentClassification;
import com.project.demo.service.ContentClassificationService;
import com.project.demo.controller.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;


/**
 * 内容分类：(ContentClassification)表控制层
 *
 */
@RestController
@RequestMapping("/content_classification")
public class ContentClassificationController extends BaseController<ContentClassification, ContentClassificationService> {

    /**
     * 内容分类对象
     */
    @Autowired
    public ContentClassificationController(ContentClassificationService service) {
        setService(service);
    }



    @PostMapping("/add")
    @Transactional
    public Map<String, Object> add(HttpServletRequest request) throws IOException {
        Map<String,Object> paramMap = service.readBody(request.getReader());
        this.addMap(paramMap);
        return success(1);
    }


}
