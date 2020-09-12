package com.daihao.mall.search.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @program: MyMall
 * @description: 首页控制器
 * @author: DAIHAO
 * @created: 2020/05/21 20:05
 */
@Controller
public class IndexController {


    /**
     * 简单请求
     * @return
     */
    @GetMapping("/list.html")
    public String hello() {

        return "list.html";
    }


}
