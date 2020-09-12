package com.daihao.mall.search.controller;

import com.daihao.mall.search.service.MallSearchService;
import com.daihao.mall.search.vo.SearchParam;
import com.daihao.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Description TODO
 * @Date 2020-09-04 9:42
 * @Author HJZ
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;


    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model){
        SearchResult result =  mallSearchService.search(param);
        model.addAttribute("result",result);
        return  "list";
    }

}
