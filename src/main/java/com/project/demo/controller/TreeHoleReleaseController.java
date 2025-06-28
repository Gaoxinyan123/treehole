package com.project.demo.controller;

import com.project.demo.entity.TreeHoleRelease;
import com.project.demo.service.TreeHoleReleaseService;
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
 * 树洞发布：(TreeHoleRelease)表控制层
 *
 */
@RestController
@RequestMapping("/tree_hole_release")
public class TreeHoleReleaseController extends BaseController<TreeHoleRelease, TreeHoleReleaseService> {

    /**
     * 树洞发布对象
     */
    @Autowired
    public TreeHoleReleaseController(TreeHoleReleaseService service) {
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
