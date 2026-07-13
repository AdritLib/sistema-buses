package com.sistema_buses.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class AppFrontConfig {

    @Bean
    ProblemExceptionDecoder errorDecoder(ObjectMapper objectMapper) {
        return new ProblemExceptionDecoder(objectMapper);
    }
    
    @Bean
    RequestInterceptor requestInterceptor() {
		return new FeignInterceptor();
    	
    }
}