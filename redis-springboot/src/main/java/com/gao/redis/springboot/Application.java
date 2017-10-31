package com.gao.redis.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;

/**
 *   名称: Application.java
 *   描述:
 *   类型: JAVA
 *   最近修改时间:2017/10/31 14:29
 *   @version [版本号, V1.0]
 *   @since 2017/10/31 14:29
 *   @author gaoshudian
 */

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}