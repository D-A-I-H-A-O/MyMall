package com.daihao.mall.search.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @program: MyMall
 * @description: ES配置类
 * @author: DAIHAO
 * @created: 2020/05/09 22:53
 */
@Configuration
public class EsConfig {


    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//暂时不适用
        //        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }


    @Bean
    public RestHighLevelClient esRestClient() {
        RestClientBuilder builder = null;
        HttpHost host = new HttpHost("192.168.175.130", 9200, "http");
        builder = RestClient.builder(host);
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }

    private static final String HOST = "localhost";
    // Elasticsearch查询服务器使用9200端口，我们可以通过RESTful API直接查询数据库。
    private static final int PORT_ONE = 9200;
    // REST服务器使用9201端口，外部客户端可以使用它来连接和执行操作。
    private static final int PORT_TWO = 9201;
    // 通信方式
    private static final String SCHEME = "http";
    // 高级客户端实例
    private static RestHighLevelClient restHighLevelClient;
    // Jackson 的转换类
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 实现了单例模式
     * 不会为ES创建多个连接，从而节省大量内存，并保证线程安全
     *
     * @return RestHighLevelClient
     */
    private static synchronized RestHighLevelClient makeConnection() {

        if (restHighLevelClient == null) {
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(HOST, PORT_ONE, SCHEME),
                            new HttpHost(HOST, PORT_TWO, SCHEME)));
        }

        return restHighLevelClient;
    }

    private static synchronized void closeConnection() throws IOException {
        restHighLevelClient.close();
        restHighLevelClient = null;
    }
}
