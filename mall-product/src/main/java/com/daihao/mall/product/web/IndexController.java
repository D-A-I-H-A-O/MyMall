package com.daihao.mall.product.web;

import com.daihao.mall.product.entity.CategoryEntity;
import com.daihao.mall.product.service.CategoryService;
import com.daihao.mall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @program: MyMall
 * @description: 首页控制器
 * @author: DAIHAO
 * @created: 2020/05/21 20:05
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryservice;

    @GetMapping({"/", "index.html"})
    public String indexPage(Model model) {
        List<CategoryEntity> categoryEntities = categoryservice.getLevel1Categorys();
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        Map<String, List<Catalog2Vo>> map = categoryservice.getCatalogJson();

        return map;
    }

    /**
     * 简单请求
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {

        return "hello";
    }


}
