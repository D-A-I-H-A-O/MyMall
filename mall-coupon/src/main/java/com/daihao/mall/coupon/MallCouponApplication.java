package com.daihao.mall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: MyMall
 * @description: .
 * @author: DAIHAO
 * @created: 2020/04/07 21:32
 */
@EnableDiscoveryClient
@MapperScan("com.daihao.mall.coupon.dao")
@SpringBootApplication
public class MallCouponApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallCouponApplication.class, args);

        @RestController
        class EchoController {
            @GetMapping(value = "/echo/{string}")
            public String echo(@PathVariable String string) {
                return string;
            }
        }
    }
}
