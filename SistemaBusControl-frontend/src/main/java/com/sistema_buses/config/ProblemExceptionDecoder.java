package com.sistema_buses.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.ProblemDetail;

import com.sistema_buses.exception.ProblemaDetallesException;

import feign.Response;
import feign.codec.ErrorDecoder;
import tools.jackson.databind.ObjectMapper;

public class ProblemExceptionDecoder implements ErrorDecoder{
	private final ObjectMapper mapper;

    public ProblemExceptionDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }
	
	@Override
    public Exception decode(String methodKey, Response response) {
        try (InputStream bodyIs = response.body().asInputStream()) {
            ProblemDetail problemDetail = mapper.readValue(bodyIs, ProblemDetail.class);
            return new ProblemaDetallesException(problemDetail);
        } catch (IOException e) {
            return new ProblemExceptionDecoder.Default().decode(methodKey, response);
        }
    }
}
