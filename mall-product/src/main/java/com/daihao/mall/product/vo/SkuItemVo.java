/**
 * Copyright 2019 bejson.com
 */
package com.daihao.mall.product.vo;

import com.daihao.mall.product.entity.SkuImagesEntity;
import com.daihao.mall.product.entity.SkuInfoEntity;
import com.daihao.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    //sku基本信息
    SkuInfoEntity info;

    boolean hasStock = true;

    //sku图片信息
    List<SkuImagesEntity> images;

    //获取spu的所有销售属性
    List<SkuItemSaleAttrVo> saleAttr;

    //spu的介绍
    SpuInfoDescEntity desc;

    //spu的规定参数
    List<SpuItemAttrGroupVo> attrGroups;

}
