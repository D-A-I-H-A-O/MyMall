package com.daihao.mall.order.dao;

import com.daihao.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author daihao
 * @email 651433368@qq.com
 * @date 2020-04-07 21:45:33
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
