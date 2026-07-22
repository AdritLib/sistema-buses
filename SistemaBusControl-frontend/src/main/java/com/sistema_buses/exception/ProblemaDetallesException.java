package com.sistema_buses.exception;

import org.springframework.http.ProblemDetail;

import lombok.Getter;

@SuppressWarnings("serial")
@Getter
public class ProblemaDetallesException extends RuntimeException {
    private ProblemDetail problemDetail;

	public ProblemaDetallesException(ProblemDetail problem) {
		super(problem.getDetail());
		this.problemDetail = problem;
	}
}