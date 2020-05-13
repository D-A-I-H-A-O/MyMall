package com.daihao.mall.search.service;


import com.daihao.mall.common.es.SkuEsModel;

import java.util.List;


public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels);
}

