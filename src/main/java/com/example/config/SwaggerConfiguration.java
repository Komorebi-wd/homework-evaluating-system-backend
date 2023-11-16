package com.example.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Configuration
public class SwaggerConfiguration {
    @Bean
    public OpenAPI springDocOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("学生作业管理系统 - 在线API接口文档")   //设置API文档网站标题
                .description("这是一个学生作业互评系统的后端API文档，欢迎前端人员查阅！") //网站介绍
                .version("0.1"));   //当前API版本
    }
}
