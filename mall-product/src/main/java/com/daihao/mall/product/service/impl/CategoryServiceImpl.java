package com.daihao.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.daihao.mall.common.utils.PageUtils;
import com.daihao.mall.common.utils.Query;
import com.daihao.mall.product.dao.CategoryDao;
import com.daihao.mall.product.entity.CategoryEntity;
import com.daihao.mall.product.service.CategoryBrandRelationService;
import com.daihao.mall.product.service.CategoryService;
import com.daihao.mall.product.utils.RedisUtil;
import com.daihao.mall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    RedisUtil redisUtil;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询所有分类以及子分类，以树形结构组装起来
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查询所有的分类
        List<CategoryEntity> allCategory = baseMapper.selectList(null);

        //2、组装成父子的树形结构
        //2.1、找到所有一级分类
        List<CategoryEntity> level1Menus = allCategory.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildren(menu, allCategory));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    /**
     * 删除菜单
     *
     * @param asList
     */
    @Override
    public void removeMenusByIds(List<Long> asList) {

        //TODO 检查当前要删除菜单是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param menu        菜单
     * @param allCategory 所有分类
     * @return 所有菜单的子菜单
     */
    private List<CategoryEntity> getChildren(CategoryEntity menu, List<CategoryEntity> allCategory) {

        List<CategoryEntity> children = allCategory.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(menu.getCatId());
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity, allCategory));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;

    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        //
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }


    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 查询所有一级分类
     *
     * @return
     */

    @Override
    @Cacheable(value = "categorys", key = "#root.method.name")
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("查询所有一级分类");

        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));

        return categoryEntities;
    }

    /**
     * 根据传进分类筛选出对应级别
     *
     * @param list
     * @param parent_cid
     * @return
     */
    public List<CategoryEntity> getCategorys(List<CategoryEntity> list, Long parent_cid) {

        List<CategoryEntity> collect = list.stream().filter(l -> parent_cid.equals(l.getParentCid())).collect(Collectors.toList());

        return collect;
    }

    /**
     * 查出所有分类 返回首页json
     */
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        //加入缓存逻辑
        Jedis jedis = redisUtil.getJedis();
        String catalogJson = jedis.get("catalogJson");

        Map<String, List<Catalog2Vo>> json = null;


        //缓存存在 转换返回
        if (!StringUtils.isEmpty(catalogJson)) {
            json = JSONObject.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return json;
        }

        //缓存没有从数据查询
        json = getCatalogJsonFromDBWithRedisLock();
        //转成str 加入缓存
        String jsonString = JSON.toJSONString(json);
        jedis.set("catalogJson", jsonString);

        return json;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        Jedis jedis = redisUtil.getJedis();
        //加锁
        String token = UUID.randomUUID().toString();
        String lock = jedis.set("lock", token, "NX", "EX", 20);
        System.out.println(lock);

        Map<String, List<Catalog2Vo>> map = null;
        //加锁成功
        if ("ok".equals(lock)) {
            map = getCatalogJsonFromDB();
            //删除锁 lua脚本
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            jedis.eval(script, Collections.singletonList("lock"), Collections.singletonList(token));
            return map;
        } else {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //自旋
            return getCatalogJsonFromDBWithRedisLock();
        }

    }


    /**
     * 从数据库获取数据并封装返回
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDB() {
        //查询出所有分类
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //先查出所有一级分类
        List<CategoryEntity> level1Categorys = getCategorys(selectList, 0L);

        //封装数据 map k,v 结构
        Map<String, List<Catalog2Vo>> map = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> category2Entities = getCategorys(selectList, v.getCatId());
            List<Catalog2Vo> catelog2Vos = null;

            if (category2Entities != null) {
                catelog2Vos = category2Entities.stream().map(level2 -> {
                    //封装catalog2Vo
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());
                    //每一个二级分类，查到三级分类
                    List<CategoryEntity> category3Entities = getCategorys(selectList, level2.getCatId());
                    if (category3Entities != null) {
                        List<Object> catalog3List = category3Entities.stream().map(level3 -> {
                            //封装catalog3Vo
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        //封装catalog3Vo到catalog2Vo
                        catalog2Vo.setCatalog3List(catalog3List);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            //返回v=catalog2Vo
            return catelog2Vos;
        }));


        return map;
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }


}