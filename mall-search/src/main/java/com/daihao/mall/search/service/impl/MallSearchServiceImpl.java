package com.daihao.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.daihao.mall.common.es.SkuEsModel;
import com.daihao.mall.search.config.EsConfig;
import com.daihao.mall.search.constant.EsConstant;
import com.daihao.mall.search.service.MallSearchService;
import com.daihao.mall.search.vo.SearchParam;
import com.daihao.mall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Date 2020-09-04 10:07
 * @Author HJZ
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResult search(SearchParam param) {
        //动态构建查询DSL语句
        SearchResult result = null;

        //准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            //执行检索请求
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, EsConfig.COMMON_OPTIONS);

            //分析响应数据封装成我们所需要的格式
            result = buildSearchResult(searchResponse, param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 构建结果数据
     *
     * @return
     */
    private SearchResult buildSearchResult00(SearchResponse searchResponse, SearchParam param) {
        SearchResult result = new SearchResult();

        //返回的所有查询到的商品
        SearchHits hits = searchResponse.getHits();
        List<SkuEsModel> essModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                //判断是否按关键字检索，若是就显示高亮，否则不显示
                if (StringUtils.isNotBlank(param.getKeyword())) {
                    String skuTitle = hit.getHighlightFields().get("skuTitle").getFragments()[0].string();
                    skuEsModel.setSkuTitle(skuTitle);
                }
                essModels.add(skuEsModel);
            }
        }
        result.setProducts(essModels);

        //当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = searchResponse.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            if (((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().size() > 0) {
                String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
                List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(doc -> {
                    String keyAsString = ((Terms.Bucket) doc).getKeyAsString();
                    return keyAsString;
                }).collect(Collectors.toList());
                attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
                attrVo.setAttrName(attrName);
                attrVo.setAttrValue(attrValues);
            }
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
        //当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = searchResponse.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            if (((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().size() > 0) {
                String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
                brandVo.setBrandName(brandImg);
            }

            if (((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().size() > 0) {
                String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
                brandVo.setBrandName(brandName);
            }

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
        //当前所有商品涉及到的所有分类信息
        ParsedLongTerms catalog_agg = searchResponse.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId(Long.parseLong(bucket.getKeyAsString()));
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            if (catalog_name_agg.getBuckets().size() > 0) {
                String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
                catalogVo.setCatalogName(catalog_name);
            }

            catalogVos.add(catalogVo);
        }
        //分页信息-页码
        result.setPageNum(param.getPageNum());
        //分页信息-总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //分页信息-总页码
        int totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : ((int) total / EsConstant.PRODUCT_PAGESIZE) + 1;
        result.setTotalPages(totalPages);

        return result;
    }

    /**
     * 构建结果数据
     * 模糊匹配，过滤（按照属性、分类、品牌，价格区间，库存），完成排序、分页、高亮,聚合分析功能
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response,SearchParam param) {

        SearchResult result = new SearchResult();

        //1、返回的所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();

        //遍历所有商品信息
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);

                //判断是否按关键字检索，若是就显示高亮，否则不显示
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    //拿到高亮信息显示标题
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String skuTitleValue = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(skuTitleValue);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        //2、当前商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        //获取属性信息的聚合
        ParsedNested attrsAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {

            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();

            //1、得到属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            //2、得到属性的名字
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            //3、得到属性的所有值
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(item -> item.getKeyAsString()).collect(
                    Collectors.toList());
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }

        result.setAttrs(attrVos);

        //3、当前商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        //获取到品牌的聚合
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            //1、得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);

            //2、得到品牌的名字
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);

            //3、得到品牌的图片
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //4、当前商品涉及到的所有分类信息
        //获取到分类的聚合
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            //得到分类名
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }

        result.setCatalogs(catalogVos);
        //===============以上可以从聚合信息中获取====================//
        //5、分页信息-页码
        result.setPageNum(param.getPageNum());
        //5、1分页信息、总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);

        //5、2分页信息-总页码-计算
        int totalPages = (int)total % EsConstant.PRODUCT_PAGESIZE == 0 ?
                (int)total / EsConstant.PRODUCT_PAGESIZE : ((int)total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
//        result.setPageNavs(pageNavs);
//
//        //6、构建面包屑导航
//        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
//            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
//                //1、分析每一个attrs传过来的参数值
//                SearchResult.NavVo navVo = new SearchResult.NavVo();
//                String[] s = attr.split("_");
//                navVo.setNavValue(s[1]);
//                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
//                if (r.getCode() == 0) {
//                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
//                    });
//                    navVo.setNavName(data.getAttrName());
//                } else {
//                    navVo.setNavName(s[0]);
//                }
//
//                //2、取消了这个面包屑以后，我们要跳转到哪个地方，将请求的地址url里面的当前置空
//                //拿到所有的查询条件，去掉当前
//                String encode = null;
//                try {
//                    encode = URLEncoder.encode(attr,"UTF-8");
//                    encode.replace("+","%20");  //浏览器对空格的编码和Java不一样，差异化处理
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//                //String replace = param.getQueryString().replace("&attrs=" + attr, "");
//                //navVo.setLink("http://search.gulimall.com/list.html?" + replace);
//
//                return navVo;
//            }).collect(Collectors.toList());
//
//            result.setNavs(collect);
//        }

        return result;
    }

    /**
     * 准备检索请求
     * 模糊匹配 、过滤、（按照属性、分类、品牌、价格区间、库存）、排序、分页、高亮、聚合分析
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        /**
         * 模糊匹配 、过滤、（按照属性、分类、品牌、价格区间、库存）
         */
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(param.getKeyword()))
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        if (param.getCatalog3Id() != null)
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        if (param.getBrandId() != null && param.getBrandId().size() > 0)
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        if (param.getHasStock() != null)
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        if (StringUtils.isNotBlank(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_"))
                    rangeQuery.lte(s[1]);
                if (param.getSkuPrice().endsWith("_"))
                    rangeQuery.gte(s[0]);
            }
            boolQuery.filter(rangeQuery);
        }
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            //attrs=1_5寸:8寸&attrs2_16G:8G
            for (String attr : param.getAttrs()) {
                String[] s = attr.split("_");
                String attrId = s[0];//检索的属性id
                String[] attrValues = s[1].split(":");
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //每一个都得生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        //所有条件进行封装
        sourceBuilder.query(boolQuery);
        /**
         * 排序、分页、高亮
         */
        if (StringUtils.isNotBlank(param.getSort())) {
            String sort = param.getSkuPrice();
            String[] s = sort.split("_");
            SortOrder sortOrder = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], sortOrder);
        }

        System.out.println("PageNum" + param.getPageNum());

        if (param.getPageNum() == null) {
            //sourceBuilder.from((50 - 1) * EsConstant.PRODUCT_PAGESIZE);
        } else {
            sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        }
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        if (StringUtils.isNotBlank(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }
        /**
         * 聚合分析
         */
        //品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        //品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        //分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(50);
        sourceBuilder.aggregation(catalog_agg);
        //分类子聚合
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));

        //属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(1));
        attr_agg.subAggregation(attr_id_agg);

        //聚合attr
        sourceBuilder.aggregation(attr_agg);

        System.out.println("检索请求" + sourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;

    }



}