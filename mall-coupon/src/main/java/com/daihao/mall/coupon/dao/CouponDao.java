package com.daihao.mall.coupon.dao;

import com.daihao.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author daihao
 * @email 651433368@qq.com
 * @date 2020-04-07 21:23:59
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
