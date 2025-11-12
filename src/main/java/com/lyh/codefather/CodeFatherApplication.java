package com.lyh.codefather;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.lyh.codefather.mapper")
public class CodeFatherApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeFatherApplication.class, args);
    }

}
