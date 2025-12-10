package com.lyh.codefather;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRetry
@MapperScan("com.lyh.codefather.mapper")
@SpringBootApplication
@EnableCaching
//exclude = {RedisEmbeddingStoreAutoConfiguration.class}
public class CodeFatherApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeFatherApplication.class, args);
    }

}
