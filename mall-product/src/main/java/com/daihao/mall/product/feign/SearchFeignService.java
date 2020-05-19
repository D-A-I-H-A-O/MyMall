package com.daihao.mall.product.feign;

import com.daihao.mall.common.es.SkuEsModel;
import com.daihao.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("mall-search")
public interface SearchFeignService {
    //上架商品 I
    @RequestMapping("/search/search")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
