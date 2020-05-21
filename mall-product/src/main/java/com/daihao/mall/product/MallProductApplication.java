package com.daihao.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @program: MyMall
 * @description: 启动类2
 * @author: DAIHAO
 * @created: 2020/04/06 14:24
 *
 * 、模板引整
 * 1) , thymeleaf-starters关闭缓存
 * 2)、静态资源都放在static文件夫下就可以按照路径直接访问
 * 3)、页面放在templatesT,直接访问
 * SpringBoot,访问项目的时候,默认会找index
 * 1)、页面修改不重启服务器实时更新
 * 1)、引시
 */

@EnableFeignClients(basePackages = "com.daihao.mall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.daihao.mall.product.dao")
@SpringBootApplication
public class MallProductApplication  {
    public static void main(String[] args) {
        SpringApplication.run(MallProductApplication.class, args);
    }

}
