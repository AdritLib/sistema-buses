package com.sistema_buses.controller;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorViewController implements ErrorController {

	@GetMapping("/error")
	public String handleError(HttpServletRequest request, Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		Object mensaje = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
		if(mensaje == null || mensaje.toString().isBlank()) {
			mensaje = "Ocurrió un error inesperado en el servidor.";
		}
		if(status == null) status = 500;
		
		model.addAttribute("errorMensaje", mensaje);
		model.addAttribute("errorCode", status);
		return "error/error"; 
	}
}