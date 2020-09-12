package com.daihao.mall.search.vo;

import com.daihao.mall.common.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @Description 搜索返回结果对象
 * @Date 2020-09-04 10:10
 * @Author HJZ
 */
@Data
public class SearchResult {

    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;
    /**
     * 当前页码
     */
    private Integer pageNum;
    /**
     * 总记录数
     */
    private Long total;
    /**
     * 总页码
     */
    private Integer totalPages;

    /**
     * 当前查询到的结果，所涉及到的所有品牌
     */
    private List<BrandVo> brands;
    /**
     * 当前查询到的结果，所涉及到的所有属性
     */
    private List<AttrVo> attrs;
    /**
     * 当前查询到的结果，所涉及到的所有分类
     */
    private List<CatalogVo> catalogs;

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
        private List<String> attrValue;
    }

}
