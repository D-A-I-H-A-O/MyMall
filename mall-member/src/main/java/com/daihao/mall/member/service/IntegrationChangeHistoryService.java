package com.daihao.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.daihao.mall.common.utils.PageUtils;
import com.daihao.mall.member.entity.IntegrationChangeHistoryEntity;

import java.util.Map;

/**
 * 积分变化历史记录
 *
 * @author daihao
 * @email 651433368@qq.com
 * @date 2020-04-07 21:42:45
 */
public interface IntegrationChangeHistoryService extends IService<IntegrationChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

