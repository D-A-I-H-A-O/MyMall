package com.daihao.mall.product;


import com.daihao.mall.product.utils.RedisUtil;
import com.daihao.mall.product.entity.BrandEntity;
import com.daihao.mall.product.service.BrandService;
import com.daihao.mall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;


/**
 * 1、引入oss-starter
 * 2、配置key，endpoint相关信息即可
 * 3、使用OSSClient 进行相关操作
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class MallProductApplicationTests {

    @Autowired
    BrandService brandService;


    @Autowired
    CategoryService categoryService;

    @Autowired
    RedisUtil redisUtil;


    @Test
    public void contextLoads() {

        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("华为");


        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功....");

        brandService.updateById(brandEntity);

    }

    @Test
    public void testRedis() {

        Jedis jedis = redisUtil.getJedis();
        //jedis.set("test","test");
        String s = jedis.get("test");
        System.out.println(s);
    }

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void testRedisson() {
        RLock lock = redissonClient.getLock("anyLock");
        //最常见的
        //使用方法
        lock.lock();


        System.out.println(lock);
    }

}
