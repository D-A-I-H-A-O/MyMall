package com.daihao.mall.common.to;

import lombok.Data;

/**
 * @program: MyMall
 * @description: 查询sku是否有库存返回vo
 * @author: DAIHAO
 * @created: 2020/05/13 22:08
 */
@Data
public class SkuHasStockVo {

    private Long skuId;
    private Boolean hasStock;
}
