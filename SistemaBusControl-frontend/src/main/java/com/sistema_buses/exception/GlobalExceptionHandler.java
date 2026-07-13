package com.sistema_buses.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import feign.FeignException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public String handleFeignException(FeignException e, RedirectAttributes redirect) {
    	redirect.addFlashAttribute("errorTitulo", "Error al contactar con la aplicación");
    	redirect.addFlashAttribute("errorMensaje", e.contentUTF8());
    	redirect.addFlashAttribute("errorEstatus", e.status());

        return "redirect:/error";
    }
    
    @ExceptionHandler(ProblemaDetallesException.class)
    public String handleProblemDetailException(ProblemaDetallesException e, RedirectAttributes redirect) {
    	redirect.addFlashAttribute("errorTitulo", e.getProblemDetail().getTitle());
    	redirect.addFlashAttribute("errorMensaje", e.getProblemDetail().getDetail());
    	redirect.addFlashAttribute("errorEstatus", 500);

        return "redirect:/error";
    }
}