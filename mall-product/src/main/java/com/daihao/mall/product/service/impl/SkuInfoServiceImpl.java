package com.daihao.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.daihao.mall.common.utils.PageUtils;
import com.daihao.mall.common.utils.Query;
import com.daihao.mall.product.dao.SkuInfoDao;
import com.daihao.mall.product.entity.SkuImagesEntity;
import com.daihao.mall.product.entity.SkuInfoEntity;
import com.daihao.mall.product.entity.SpuInfoDescEntity;
import com.daihao.mall.product.service.AttrGroupService;
import com.daihao.mall.product.service.SkuImagesService;
import com.daihao.mall.product.service.SkuInfoService;
import com.daihao.mall.product.service.SkuSaleAttrValueService;
import com.daihao.mall.product.service.SpuInfoDescService;
import com.daihao.mall.product.vo.SkuItemSaleAttrVo;
import com.daihao.mall.product.vo.SkuItemVo;
import com.daihao.mall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {

            queryWrapper.eq("catalog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");

        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);

                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {

            }

        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据spuId查询所有对应spuId的sku信息
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
        List<SkuInfoEntity> skuList = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuList;
    }

    @Override
    public SkuItemVo getSkuInfoBySkuId(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();

        //sku基本信息
        SkuInfoEntity info = getById(skuId);
        skuItemVo.setInfo(info);

        //sku图片信息
        List<SkuImagesEntity> skuImages = skuImagesService.getImagesBySkuId(skuId);
        skuItemVo.setImages(skuImages);
        Long spuId = info.getSpuId();

        //获取spu的所有销售属性
        List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
        skuItemVo.setSaleAttr(saleAttrVos);

        //spu的介绍
        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(spuId);
        skuItemVo.setDesc(spuInfoDescEntity);

        //获取规格参数组及组下的规格参数
        Long catalogId = info.getCatalogId();
        List<SpuItemAttrGroupVo> attrGroup = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
        skuItemVo.setAttrGroups(attrGroup);

        return skuItemVo;
    }

}