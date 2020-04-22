package com.daihao.mall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @program: MyMall
 * @description: 跨域配置类
 * @author: DAIHAO
 * @created: 2020/04/17 22:54
 */
@Configuration
public class MallCorsConfiguration {


    @Bean
    public CorsWebFilter corsWebFilter() {
        //响应式编程
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfiguration = new CorsConfiguration();

        //配置跨域
        //任意请求头
        corsConfiguration.addAllowedHeader("*");
        //任意请求方法
        corsConfiguration.addAllowedMethod("*");
        //任意请求来源
        corsConfiguration.addAllowedOrigin("*");
        //是否允许携带cookie
        corsConfiguration.setAllowCredentials(true);

        source.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(source);
    }

}
