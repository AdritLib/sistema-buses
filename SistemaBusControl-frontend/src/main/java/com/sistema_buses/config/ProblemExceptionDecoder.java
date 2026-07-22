package com.sistema_buses.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.ProblemDetail;

import com.sistema_buses.exception.ProblemaDetallesException;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class ProblemExceptionDecoder implements ErrorDecoder{
	private final ErrorDecoder defaultDecoder = new Default();
	private final ObjectMapper mapper;
	
	@Override
    public Exception decode(String methodKey, Response response) {
		if(response.body() == null) return defaultDecoder.decode(methodKey, response);
        try (InputStream bodyIs = response.body().asInputStream()) {
        	ProblemDetail problemDetail = mapper.readValue(bodyIs, ProblemDetail.class);
        	return new ProblemaDetallesException(problemDetail);
        } catch (IOException e) {
            return defaultDecoder.decode(methodKey, response);
        }
    }
}
