package com.sistema_buses.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

	    ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.CONFLICT);
	    problema.setDetail(detalle);
	    problema.setTitle( "Conflicto con los datos proporcionados.");
	    return problema;
	}
	
	@ExceptionHandler(ExpiredJwtException.class)
	public ProblemDetail handleExpiredJwtException(ExpiredJwtException ex) {
		ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
		problema.setDetail("Token expiro, necesitas volver a iniciar sesion");
        problema.setTitle("El token JWT ya expiro");
        
		return problema;
	}
	
	@ExceptionHandler(UsuarioNoEncontradoException.class)
    public ProblemDetail handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
    	ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    	problema.setTitle("Error del sistema");
    	problema.setDetail(ex.getMessage());
    	return problema;
    }
	
	@ExceptionHandler(ErrorDeNegocioException.class)
    public ProblemDetail handleErrorDeNegocio(ErrorDeNegocioException ex) {
		ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    	problema.setTitle("Error del sistema");
    	problema.setDetail(ex.getMessage());
    	return problema;
    }
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
		String mensaje = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null).getDefaultMessage();
		ProblemDetail problema = ProblemDetail.forStatus(HttpStatusCode.valueOf(401));
		problema.setTitle("Datos ingresados invalidos");
		problema.setDetail(mensaje);
        return problema;
    }
	
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problema.setTitle("Error interno del servidor");
        problema.setDetail(exception.getMessage());
        
        if (exception instanceof BadCredentialsException) {
            problema.setDetail("Las credenciales son incorrectas");
        } else if (exception instanceof AccountStatusException) {
            problema.setDetail("La cuenta esta bloqueada");
        } else if (exception instanceof AccessDeniedException) {
        	problema.setDetail("No tienes permiso para acceder a este recurso");
        } else if (exception instanceof SignatureException) {
        	problema.setDetail("La firma JWT es invalido");
        }else {
        	problema.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return problema;
    }
}