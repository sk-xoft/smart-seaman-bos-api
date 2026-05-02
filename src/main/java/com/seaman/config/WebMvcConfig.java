package com.seaman.config;

import com.seaman.interceptor.APIInterceptor;
import com.seaman.interceptor.AuthInterceptor;
import com.seaman.interceptor.CrudInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final APIInterceptor apiInterceptor;

    private final AuthInterceptor authInterceptor;
    private final CrudInterceptor crudInterceptor;

    private final String[] NOT_VALIDATE_AUTH = {
            "/actuator/**",
            "/swagger-ui.html/**",
            "/swagger-ui/**",
            "/smart-seaman-swagger/**",
            "/v1/login",
            // "/v1/register",
            // Master data
            "/v1/master",
            "/v1/admin",
            "/v1/send-noti/news"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiInterceptor)
                .excludePathPatterns(
                        "/swagger-ui.html/**",
                        "/swagger-ui/**",
                        "/smart-seaman-swagger/**"
                ).addPathPatterns("/v1/**")
                .order(1);

        registry.addInterceptor(authInterceptor)
                .excludePathPatterns(NOT_VALIDATE_AUTH).addPathPatterns("/v1/**")
                .order(2);

//        registry.addInterceptor(crudInterceptor)
//                .excludePathPatterns("/actuator/**",
//                        "/swagger-ui.html/**",
//                        "/swagger-ui/**",
//                        "/smart-seaman-swagger/**",
//                        "/v1/login",
//                        "/v1/master",
//                        "/v1/admin","/v1/**/list-menu",
//                        "/v1/**/list-admin","/v1/**/list-admin/**",
//                        "/v1/**/list-banner").addPathPatterns("/v1/**/add","/v1/**/update","/v1/**/delete","/v1/**/list**")
//                .order(3);

    }

}
