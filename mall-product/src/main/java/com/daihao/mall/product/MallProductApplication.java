package com.daihao.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: MyMall
 * @description: 启动类2
 * @author: DAIHAO
 * @created: 2020/04/06 14:24
 */
@MapperScan("com.daihao.mall.product.dao")
@SpringBootApplication
public class MallProductApplication  {
    public static void main(String[] args) {
        SpringApplication.run(MallProductApplication.class, args);
    }

}