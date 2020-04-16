package com.daihao.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.daihao.mall.common.utils.PageUtils;
import com.daihao.mall.member.entity.MemberStatisticsInfoEntity;

import java.util.Map;

/**
 * 会员统计信息
 *
 * @author daihao
 * @email 651433368@qq.com
 * @date 2020-04-07 21:42:45
 */
public interface MemberStatisticsInfoService extends IService<MemberStatisticsInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

