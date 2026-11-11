package com.recruitment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 文档配置（第06步课件第十一节）
 * 给 Swagger 加 Bearer 配置后，页面右上角会出现 Authorize，
 * 可以直接粘贴登录返回的 Token。这段只负责让 Swagger 帮请求加 Header，
 * 真正判断 Token 是否有效的仍是 JwtInterceptor。
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("招聘网站API文档")
                        .description("Web架构技术教学项目 - 招聘网站后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("Web架构技术教学组"))
                )
                // ↓ 第06步新增：Swagger 页面使用 Bearer Token
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Token",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("粘贴登录返回的 Token，不要手写 Bearer 前缀")
                        )
                );
    }
}
