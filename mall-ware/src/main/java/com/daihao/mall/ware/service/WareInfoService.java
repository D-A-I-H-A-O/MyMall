package com.daihao.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.daihao.mall.common.utils.PageUtils;
import com.daihao.mall.ware.entity.WareInfoEntity;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author daihao
 * @email 651433368@qq.com
 * @date 2020-04-07 21:47:26
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

