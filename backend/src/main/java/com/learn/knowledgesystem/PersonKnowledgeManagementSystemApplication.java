package com.learn.knowledgesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication     //创建项目启动类
public class PersonKnowledgeManagementSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonKnowledgeManagementSystemApplication.class, args);
        System.out.println("=======================================");
        System.out.println("知识管理系统后端启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("=======================================");
    }

}
