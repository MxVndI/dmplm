package com.diplom.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final MetricsInterceptor metricsInterceptor;
    private final ABInterceptor abInterceptor;
    private final TemplateOverrideInterceptor templateOverrideInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(abInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/**", "/internal/**", "/admin/**",
                        "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/login", "/register");

        registry.addInterceptor(templateOverrideInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/**", "/internal/**", "/admin/**",
                        "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/login", "/register");

        registry.addInterceptor(metricsInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/**", "/internal/**",
                        "/css/**", "/js/**", "/images/**", "/webjars/**");
    }
}
