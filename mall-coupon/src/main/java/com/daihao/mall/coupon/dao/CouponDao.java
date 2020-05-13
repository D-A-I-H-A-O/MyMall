package com.daihao.mall.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daihao.mall.coupon.entity.CouponEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:36:40
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
