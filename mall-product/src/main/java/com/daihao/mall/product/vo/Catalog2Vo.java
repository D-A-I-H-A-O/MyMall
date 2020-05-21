package com.daihao.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @program: MyMall
 * @description: 二级分类vo
 * @author: DAIHAO
 * @created: 2020/05/21 20:46
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catalog2Vo {
    //1父分#id
    private String catalog1Id;
    //三级子分类
    private List<Object> catalog3List;
    private String id;
    private String name;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static  class Catalog3Vo{
        private String catalog2Id;
        private String id;
        private String name;
    }
}
