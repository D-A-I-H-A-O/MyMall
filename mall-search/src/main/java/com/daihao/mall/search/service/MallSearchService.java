package com.daihao.mall.search.service;

import com.daihao.mall.search.vo.SearchParam;
import com.daihao.mall.search.vo.SearchResult;
import org.springframework.stereotype.Service;

/**
 * @Date 2020-09-04 9:43
 * @Author HJZ
 */
@Service
public interface MallSearchService {
    /**
     * @param param 搜索的条件
     * @return 搜索返回
     */
    SearchResult search(SearchParam param);
}

