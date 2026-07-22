package com.sistema_buses.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import feign.codec.Decoder;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ApplicacionConfig {
	public static final Long TOKEN_EXPIRATION_TIME = 35L;
	
	@Bean
    ProblemExceptionDecoder errorDecoder(ObjectMapper objectMapper) {
        return new ProblemExceptionDecoder(objectMapper);
    }
    
    @Bean
    RequestInterceptor requestInterceptor() {
		return new FeignInterceptor();    	
    }
    
	@Bean
    FeignHttpMessageConverters messageConverters(
    		ObjectProvider<ClientHttpMessageConvertersCustomizer> clientMessageConverter,
    		ObjectProvider<HttpMessageConverterCustomizer> messageConverters
    		) {
    	return new FeignHttpMessageConverters(clientMessageConverter, messageConverters);
    }
    
    @Bean
    Decoder feignDecoder(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
    	Decoder decoder = new ResponseEntityDecoder(new SpringDecoder(messageConverters));
    	return new FeignDecoder(decoder);
    }
}