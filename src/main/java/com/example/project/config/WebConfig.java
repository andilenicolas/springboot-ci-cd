package com.example.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.project.interceptor.RateLimitInterceptor;
import com.example.project.interceptor.HttpLoggingInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer 
{
    private final RateLimitConfig rateLimitConfig;

    public WebConfig(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }
    
    @Bean
    public HttpLoggingInterceptor getHttpLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setIncludeQueryString(true);
        interceptor.setIncludePayload(true);
        interceptor.setMaxPayloadLength(10000);
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	registry.addInterceptor(getHttpLoggingInterceptor());
    	
        registry.addInterceptor(new RateLimitInterceptor(rateLimitConfig))
               .addPathPatterns("/api/**", "/actuator/**")  
               .excludePathPatterns("/api/public/**");
    }
}