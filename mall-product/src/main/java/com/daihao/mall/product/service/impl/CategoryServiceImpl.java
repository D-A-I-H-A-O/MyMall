package com.daihao.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.daihao.mall.common.utils.PageUtils;
import com.daihao.mall.common.utils.Query;
import com.daihao.mall.product.dao.CategoryDao;
import com.daihao.mall.product.entity.CategoryEntity;
import com.daihao.mall.product.service.CategoryBrandRelationService;
import com.daihao.mall.product.service.CategoryService;
import com.daihao.mall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;


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
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));

        return categoryEntities;
    }

    /**
     * 查出所有分类 返回首页json
     *
     * @return
     */
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        //所有一级分类
        List<CategoryEntity> level1Categorys = getLevel1Categorys();

        //封装数据 map k,v 结构
        Map<String, List<Catalog2Vo>> map = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> category2Entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
            List<Catalog2Vo> catelog2Vos = null;

            if (category2Entities != null) {
                catelog2Vos = category2Entities.stream().map(level2 -> {
                    //封装catalog2Vo
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());
                    //每一个二级分类，查到三级分类
                    List<CategoryEntity> category3Entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", level2.getCatId()));
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