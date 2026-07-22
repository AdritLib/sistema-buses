package com.sistema_buses.controller;

import com.sistema_buses.client.AuthClient;
import com.sistema_buses.client.UsuarioClient;
import com.sistema_buses.dto.usuario.LoginResponse;
import com.sistema_buses.dto.usuario.UsuarioCambiarClaveRequest;
import com.sistema_buses.dto.usuario.UsuarioCompletoResponse;
import com.sistema_buses.dto.usuario.UsuarioLogin;
import com.sistema_buses.exception.ProblemaDetallesException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AutenticacionController {
    private final AuthClient authClient;
    private final UsuarioClient usuarioClient;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginData", new UsuarioLogin());
        return "login";
    }

    @PostMapping("/login")
    public String loginProcesar(@ModelAttribute UsuarioLogin loginData, 
                                HttpSession session, 
                                HttpServletResponse response,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
        	ResponseEntity<LoginResponse> responseEntity = authClient.login(loginData);
        	LoginResponse loginResponse = responseEntity.getBody();

        	crearCookiesNecesarias(response, loginResponse);
            
            return "redirect:/cargarDashboard";
        } catch (ProblemaDetallesException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
        	e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Ocurrio un error en la aplicación.");
        }
        return "redirect:/login";
    }
    
    @GetMapping("/cargarDashboard")
    public String cargarDashboard(
    		HttpSession session, 
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            Model model){
    	try {
    		ResponseEntity<UsuarioCompletoResponse> responseEntity = usuarioClient.obtenerPerfil();

            UsuarioCompletoResponse perfil = responseEntity.getBody();
            String rol = perfil.getRol().toUpperCase();
            
            agregarAtributosNecesarios(session, perfil);
            
			if ("ADMIN".equals(rol)) return "redirect:/admin/dashboard";
			if ("SUPERVISOR".equals(rol)) return "redirect:/supervisor/dashboard";
			if ("CONDUCTOR".equals(rol)) return "redirect:/conductor/dashboard";
            
			System.out.println(rol);
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
    public String logout(HttpSession session, HttpServletResponse response) {
    	if(session != null) {
    		session.invalidate();
    	}
        
        String[] cookiesAEliminar = {"token", "expires-in"};
        for (String nombre : cookiesAEliminar) {
            Cookie cookie = new Cookie(nombre, null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setSecure(false);
            response.addCookie(cookie);
        }
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
    
    public static void crearCookiesNecesarias(
    		HttpServletResponse response, 
    		LoginResponse loginResponse
    		) {
    	Map<String, String> cookies = new HashMap<>();
    	cookies.put("token", loginResponse.getToken());
    	cookies.put("expires-in", loginResponse.getExpiresIn()+"");
    
    	for(Entry<String, String> cookie : cookies.entrySet()) {
    		Cookie item = new Cookie(cookie.getKey(), cookie.getValue());
    		item.setHttpOnly(true);
    		item.setSecure(false); //Sin HTTPS
    		item.setPath("/");
    		item.setMaxAge(35);
    		response.addCookie(item);
    	}
    }
    
    public static void agregarAtributosNecesarios(
    		HttpSession session,
    		UsuarioCompletoResponse usuario
    		) {
    	session.setAttribute("rol", usuario.getRol());
    	session.setAttribute("nombreUsuario", usuario.getNombre());
    	session.setAttribute("usuarioId", usuario.getId());
    }
}