package com.daihao.mall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @program: MyMall
 * @description: 会员服务启动类
 * @author: DAIHAO
 * @created: 2020/04/07 22:38
 */
@EnableFeignClients("com.daihao.mall.member.feign")
@SpringBootApplication
@MapperScan("com.daihao.mall.member.dao")
@EnableDiscoveryClient
public class MallMemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallMemberApplication.class, args);
    }
}
