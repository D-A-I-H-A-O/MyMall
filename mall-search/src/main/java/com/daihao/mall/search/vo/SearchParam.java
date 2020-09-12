package com.daihao.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description 封装页面所有可能传递过来的查询条件
 * @Date 2020-09-04 9:41
 * @Author HJZ
 */
@Data
public class SearchParam {

    /**
     * 页面传递的关键字
     */
    private String keyword;
    /**
     * 三类分级Id
     */
    private Long catalog3Id;
    /**
     * 排序条件
     */
    private String sort;
    /**
     * 是否有货 hasStock0/1 0无库存 1有库存
     */
    private Integer hasStock;
    /**
     * 价格区间  skuPrice1_100
     */
    private String skuPrice;
    /**
     * 品牌
     */
    private List<Long> brandId;
    /**
     * 属性
     */
    private List<String> attrs;
    /**
     * 页码
     */
    private Integer pageNum;

}
