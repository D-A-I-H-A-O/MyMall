package com.daihao.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.daihao.mall.common.es.SkuEsModel;
import com.daihao.mall.search.config.EsConfig;
import com.daihao.mall.search.constant.EsConstant;
import com.daihao.mall.search.service.ProductSaveService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: MyMall
 * @description: es保存service实现类
 * @author: DAIHAO
 * @created: 2020/05/13 22:52
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @SneakyThrows
    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) {

        //给es中建立索引
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel model : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            String s = JSON.toJSONString(model);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        //BulkRequest bulkRequest, RequestOptions options
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, EsConfig.COMMON_OPTIONS);

        //TODO 如果批量错误
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.error("商品上架错误:{}", collect);

        return b;

    }
}
