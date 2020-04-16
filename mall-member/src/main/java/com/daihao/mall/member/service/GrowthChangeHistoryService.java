package com.daihao.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.daihao.mall.common.utils.PageUtils;
import com.daihao.mall.member.entity.GrowthChangeHistoryEntity;

import java.util.Map;

/**
 * 成长值变化历史记录
 *
 * @author daihao
 * @email 651433368@qq.com
 * @date 2020-04-07 21:42:45
 */
public interface GrowthChangeHistoryService extends IService<GrowthChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

