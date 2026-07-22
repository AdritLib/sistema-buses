package com.sistema_buses.config;

import java.util.logging.Logger;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class FeignInterceptor implements RequestInterceptor{
	private final Logger logger = Logger.getLogger(FeignInterceptor.class.getName());
	
	@Override
	public void apply(RequestTemplate template) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;
        HttpServletRequest request = attributes.getRequest();
        
        String token = null;
        String expiresAtStr = null;

        if (request.getCookies() != null && !request.getRequestURI().equals("api/usuario/validar")) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                } else if ("expires-in".equals(cookie.getName())) {
                    expiresAtStr = cookie.getValue();
                }
            }
        }
        
        if (token != null) {
            StringBuilder cookieHeader = new StringBuilder();
            cookieHeader.append("token=").append(token);
            
            if (expiresAtStr != null) {
                cookieHeader.append("; expires-in=").append(expiresAtStr);
            }
            logger.info(token.substring(token.length() - 10, token.length()));
            
            template.header("Cookie", cookieHeader.toString());
        }
	}
}
