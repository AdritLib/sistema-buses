package com.sistema_buses.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
	    String errorMessage = ex.getMostSpecificCause().getMessage();
	    String detalle = "Ha ocurrido un error al procesar la solicitud.";
	    if (errorMessage != null && errorMessage.contains("Duplicate entry")) {
	    	detalle = "El registro ya existe en el sistema. Verifique los datos ingresados.";
	    }
	    return problemDetail(ex, HttpStatus.CONFLICT, "Conflicto con los datos proporcionados.", detalle);
	}
	
	@ExceptionHandler(ExpiredJwtException.class)
	public ProblemDetail handleExpiredJwtException(ExpiredJwtException ex) {
		return problemDetail(ex, HttpStatus.UNAUTHORIZED, "Token de acceso expirado.", "Token ya expiro, necesitas volver a iniciar sesion");
	}
	
	@ExceptionHandler(UsuarioNoEncontradoException.class)
    public ProblemDetail handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
		return problemDetail(ex);
    }
	
	@ExceptionHandler(ErrorDeNegocioException.class)
    public ProblemDetail handleErrorDeNegocio(ErrorDeNegocioException ex) {
		return problemDetail(ex);
    }
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
		String mensaje = exception.getBindingResult().getFieldErrors()
				.stream()
				.findFirst()
				.orElse(null)
				.getDefaultMessage();
        return problemDetail(exception, HttpStatus.BAD_REQUEST, "Datos ingresados invalidos.", mensaje);
    }
	
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception) {
        ProblemDetail problema = problemDetail(exception, HttpStatus.UNAUTHORIZED, "Error del servidor", exception.getMessage());
        
        if (exception instanceof BadCredentialsException) {
            problema.setDetail("Las credenciales son incorrectas.");
        } else if (exception instanceof AccountStatusException) {
            problema.setDetail("La cuenta esta bloqueada.");
        } else if (exception instanceof AccessDeniedException) {
        	problema.setDetail("No tienes permiso para acceder a este recurso.");
        } else if (exception instanceof SignatureException) {
        	problema.setDetail("La firma JWT es invalido.");
        }else {
        	problema.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return problema;
    }
    
    private ProblemDetail problemDetail(Exception excepcion, HttpStatus status, String titulo, String mensaje) {
    	ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, mensaje);
    	problemDetail.setTitle(titulo);
    	return problemDetail;
    }
    private ProblemDetail problemDetail(Exception excepcion) {
    	return problemDetail(excepcion, HttpStatus.INTERNAL_SERVER_ERROR, "Error del servidor", excepcion.getMessage());
    }
}