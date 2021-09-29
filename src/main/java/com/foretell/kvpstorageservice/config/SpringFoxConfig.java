package com.foretell.kvpstorageservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SpringFoxConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.foretell.kvpstorageservice.controller"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Key value storage service API",
                "This API helps you to work with key value storage service.",
                "1.0",
                null,
                new Contact("Daniil Karanov", "https://github.com/4etell", "daniilkaranov@yandex.ru"),
                null,
                null,
                Collections.emptyList());
    }
}