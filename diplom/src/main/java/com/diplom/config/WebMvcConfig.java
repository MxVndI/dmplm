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
        // 1. A/B interceptor (preHandle): resolves test/variant and sets request attributes.
        registry.addInterceptor(abInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/**", "/internal/**", "/admin/**",
                        "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/login", "/register");

        // 2. Template override interceptor (postHandle): swaps the view with a custom HTML
        //    template if one is configured for this test/variant/path combination.
        registry.addInterceptor(templateOverrideInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/**", "/internal/**", "/admin/**",
                        "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/login", "/register");

        // 3. Metrics interceptor (postHandle): injects tracking metadata into the model.
        registry.addInterceptor(metricsInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/**", "/internal/**",
                        "/css/**", "/js/**", "/images/**", "/webjars/**");
    }
}
