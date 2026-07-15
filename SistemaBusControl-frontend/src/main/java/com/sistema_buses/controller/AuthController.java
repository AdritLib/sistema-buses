package com.sistema_buses.controller;


import com.sistema_buses.client.AuthClient;
import com.sistema_buses.client.UsuarioClient;
import com.sistema_buses.dto.usuario.LoginResponse;
import com.sistema_buses.dto.usuario.UsuarioCambiarClaveRequest;
import com.sistema_buses.dto.usuario.UsuarioCompletoResponse;
import com.sistema_buses.dto.usuario.UsuarioLogin;
import com.sistema_buses.exception.ProblemaDetallesException;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthClient authClient;
    private final UsuarioClient usuarioClient;

    public AuthController(AuthClient authClient, UsuarioClient usuarioClient) {
        this.authClient = authClient;
        this.usuarioClient = usuarioClient;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginData", new UsuarioLogin());
        return "login";
    }

    @PostMapping("/login")
    public String loginProcesar(@ModelAttribute UsuarioLogin loginData, 
                                HttpSession session, 
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            LoginResponse loginResponse = authClient.login(loginData);
            session.setAttribute("token", loginResponse.getToken());
            session.setAttribute("expires-in", loginResponse.getExpiresIn());
            
            UsuarioCompletoResponse perfilResponse = usuarioClient.obtenerPerfil();
            
            String rol = perfilResponse.getRol().toUpperCase();
            session.setAttribute("usuarioId", perfilResponse.getId());
            session.setAttribute("rol", rol);
            session.setAttribute("nombreUsuario", perfilResponse.getNombre());

            if ("ADMIN".equals(rol)) {
                return "redirect:/admin/dashboard";
            } else if ("SUPERVISOR".equals(rol)) {
                return "redirect:/supervisor/dashboard";
            } else if ("CONDUCTOR".equals(rol)) {
                return "redirect:/conductor/dashboard";
            }
            
            return "login";
        } catch (ProblemaDetallesException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
        	e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Ocurrio un error en la aplicación.");
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
    @GetMapping("/cambiarClave")
    public String cambiarClave(Model model) {
    	model.addAttribute("peticion", new UsuarioCambiarClaveRequest());
    	return "cambiarClave";
    }
    
    @PostMapping("/cambiarClave")
    public String procesarCambiarClave(@ModelAttribute UsuarioCambiarClaveRequest peticion, RedirectAttributes redirect) {
    	try {
    		authClient.cambiarClave(peticion);
    		redirect.addFlashAttribute("mensaje", "Cambio de clave exitoso. Inicie sesión.");
            return "redirect:/login";
        } catch (ProblemaDetallesException e) {
        	boolean vacio = e.getMessage() == null || e.getMessage().isBlank();
        	redirect.addFlashAttribute("error", vacio ? "Ocurrio un error en la aplicación." : e.getMessage());
        } catch (Exception e) {
        	redirect.addFlashAttribute("error", "Ocurrio un error en la aplicación.");
        }
    	return "redirect:/cambiarClave";
    }
}