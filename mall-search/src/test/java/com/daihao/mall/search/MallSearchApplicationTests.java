package com.daihao.mall.search;

import com.alibaba.fastjson.JSON;
import com.daihao.mall.search.config.EsConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MallSearchApplicationTests {

    @Autowired
    RestHighLevelClient client;

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    /**
     * 测试存储数据到es
     */
    @Test
    public void index() throws IOException {
        //指定索引
        IndexRequest indexRequest = new IndexRequest("users");
        //指定ID 如果不设置自动生成
        indexRequest.id("1");
        //1、使用kv结构
        //index.source("userName","daihao","age","18");

        //2、使用json
        User user = new User();
        user.setName("DAIHAO");
        user.setAge(18);
        user.setGender("男");
        String json = JSON.toJSONString(user);
        indexRequest.source(json, XContentType.JSON);

        //执行同步存储 RequestOptions.DEFAULT:请求设置项
        IndexResponse index = client.index(indexRequest, RequestOptions.DEFAULT);

        //提取有用得响应数据
        System.out.println(index);
        //IndexResponse[index=users,type=_doc,id=1,version=1,result=created,seqNo=0,primaryTerm=1,shards={"total":2,"successful":1,"failed":0}]


        //java.lang.IllegalArgumentException: The number of object passed must be even but was [1]
        //错误 没有设置传递的内容格式 XContentType.JSON

    }

    @Data
    class User {
        private String name;
        private String gender;
        private Integer age;
    }


    /**
     * 测试检索数据从es
     */
    @Test
    public void search() throws IOException {
        GetRequest getRequest = new GetRequest(
                "users",
                "1");
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        GetRequest none = getRequest.storedFields("_none_");
        System.out.println(none);
    }

    /**
     * 复杂查询
     */
    @Test
    public void searchData()  throws IOException {

        //创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        //指定DSL，检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

        //按照年龄的值分布聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg")
                .field("age").size(10);
        sourceBuilder.aggregation(ageAgg);

        //计算平均薪资
        AvgAggregationBuilder balanceVag = AggregationBuilders.avg("balanceVag")
                .field("balance");
        sourceBuilder.aggregation(balanceVag);

        //System.out.println(sourceBuilder.toString());
        searchRequest.source(sourceBuilder);

        //执行
        SearchResponse searchResponse = client.search(searchRequest, EsConfig.COMMON_OPTIONS);

        //获取命中数据
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String str = hit.getSourceAsString();
            Account account = JSON.parseObject(str, Account.class);
            System.out.println("account:" + account);
            //account_number=970, balance=19648, firstname=Forbes, lastname=Wallace, age=28, gender=M,
            //address=990 Mill Road, employer=Pheast, email=forbeswallace@pheast.com, city=Lopezo, state=AK)
        }

        //获取这次检索的到的分析信息
        Aggregations aggregations = searchResponse.getAggregations();
        for (Aggregation aggregation : aggregations.asList()) {
            System.out.println("当前聚合：" + aggregation.getName());
            //当前聚合：ageAgg
            //当前聚合：balanceVag
        }

        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String str = bucket.getKeyAsString();
            System.out.println("年龄:" + str + "===>" + bucket.getDocCount());
//            年龄:38===>2
//            年龄:28===>1
//            年龄:32===>1
        }

        Avg balanceVag1 = aggregations.get("balanceVag");
        System.out.println("平均薪资：" + balanceVag1.getValueAsString());
        //平均薪资：25208.0

    }

    @ToString
    @Data
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }


}
