package com.sistema_buses.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sistema_buses.client.UsuarioClient;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
	private final UsuarioClient usuarioClient;
	
    public DashboardController(UsuarioClient usuarioClient) {
		this.usuarioClient = usuarioClient;
	}

	private String verificarSesion(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }
        model.addAttribute("rol", session.getAttribute("rol"));
        return null;
    }

    @GetMapping("/admin")
    public String dashboardAdmin(HttpSession session, Model model) {
        String redirect = verificarSesion(session, model);
        if (redirect != null) return redirect;
        return "dashboards/admin-view"; // Vista HTML para el Administrador
    }

    @GetMapping("/supervisor")
    public String dashboardSupervisor(HttpSession session, Model model) {
        String redirect = verificarSesion(session, model);
        if (redirect != null) return redirect;
        return "dashboards/admin-view"; // Vista HTML para el Supervisor
    }

    @GetMapping("/conductor")
    public String dashboardConductor(HttpSession session, Model model) {
        String redirect = verificarSesion(session, model);
        if (redirect != null) return redirect;
        return "conductor/dashboard"; // Vista HTML para el Conductor
    }
    
    @GetMapping("/principal")
    public String dashboard(HttpSession session, Model model) {
    	String rol = (String) session.getAttribute("rol");
    	
    	switch(rol.toUpperCase()) {
    	case "ADMIN":
    		return "redirect:/admin/dashboard";
    	case "SUPERVISOR":
    		return "redirect:/supervisor/dashboard";
    	case "CONDUCTOR":
    		return "redirect:/conductor/dashboard";
    	default:
    		return "redirect:/logout";
    	}
    }
    
    @GetMapping("/perfil")
    public String perfil(Model model, HttpSession session) {
    	
    	model.addAttribute("usuarioForm", usuarioClient.obtenerPerfil());
        model.addAttribute("menuActivo", "personal");
        model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
	    return "dashboards/perfil";
    }
    
    
    @GetMapping("/validar")
    @ResponseBody
    public void validar() {
    	usuarioClient.validar();
    }
}