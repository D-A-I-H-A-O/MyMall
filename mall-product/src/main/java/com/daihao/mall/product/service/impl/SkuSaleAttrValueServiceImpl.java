package com.daihao.mall.product.service.impl;

import com.daihao.mall.product.vo.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.daihao.mall.common.utils.PageUtils;
import com.daihao.mall.common.utils.Query;

import com.daihao.mall.product.dao.SkuSaleAttrValueDao;
import com.daihao.mall.product.entity.SkuSaleAttrValueEntity;
import com.daihao.mall.product.service.SkuSaleAttrValueService;

@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {

        SkuSaleAttrValueDao dao = this.baseMapper;
        List<SkuItemSaleAttrVo> vos = dao.getSaleAttrsBySpuId(spuId);
        return vos;

    }

}