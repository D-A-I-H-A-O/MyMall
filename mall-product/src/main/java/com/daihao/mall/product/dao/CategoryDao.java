package com.daihao.mall.product.dao;

import com.daihao.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author DAIHAO
 * @email 651433368@qq.com
 * @date 2020-04-06 13:59:55
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
