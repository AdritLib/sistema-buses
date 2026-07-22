package com.sistema_buses.config;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sistema_buses.controller.AutenticacionController;
import com.sistema_buses.dto.usuario.LoginResponse;

import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeignDecoder implements Decoder{
	private final Decoder delegado;
	
	@Override
	public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
		Collection<String> cookies = response.headers().get("Set-Cookie");
		if(cookies != null && !cookies.isEmpty()) {
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
                HttpServletResponse clientResponse = attributes.getResponse();
                String valorToken = null, 
                	   valorExpiresIn = null;
                
                if (clientResponse != null) {
                    for (String cookie : cookies) {
                        if (cookie.contains("token")) {
                            valorToken = cookie.split(";")[0].split("=")[1];
                        }else if(cookie.contains("expires-in")){
                        	valorExpiresIn = cookie.split(";")[0].split("=")[1];
                        	
                        }
                    }
                    AutenticacionController.crearCookiesNecesarias(clientResponse, new LoginResponse(valorToken, Long.parseLong(valorExpiresIn == null ? "0L" : valorExpiresIn)));
                }
            }
		}
		return delegado.decode(response, type);
	}
}
