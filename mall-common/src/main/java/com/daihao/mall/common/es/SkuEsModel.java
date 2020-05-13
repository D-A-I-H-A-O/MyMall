package com.daihao.mall.common.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: MyMall
 * @description: 商品传输ES数据模型
 * @author: DAIHAO
 * @created: 2020/05/13 21:14
 */
@Data
public class SkuEsModel {

    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    private Boolean hasStock;
    private Long hotScore;
    private Long brandId;
    private Long catalogId;
    private String brandName;
    private String brandImg;
    private String catalogName;

    private List<SkuEsModel.Attrs> attrs;

    @Data
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValues;
    }

}
