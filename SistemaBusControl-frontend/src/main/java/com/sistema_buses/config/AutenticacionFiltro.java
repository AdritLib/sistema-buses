package com.sistema_buses.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AutenticacionFiltro extends OncePerRequestFilter{
	
	private static final List<String> RUTAS_PUBLICAS = List.of(
        "/css/", "/js/", "/images/", "/login", "/cambiarClave", "/logout", "/cargarDashboard"
    );
	
	private static final Map<String, String> MAPA_ROL_ACCESO = Map.of(
        "/admin/", "ADMIN",
        "/supervisor/", "SUPERVISOR",
        "/conductor/", "CONDUCTOR"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return RUTAS_PUBLICAS.stream().anyMatch(path::startsWith);
    }

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		
        String token = leerCookie(request, "token");
        String expiresInSt = leerCookie(request, "expires-in");
        String rol = (String) session.getAttribute("rol");

    	if (token == null || expiresInSt == null) {
    		redirigirLogin(request, response);
            return;
        }
		Long expiresIn = (Long) Long.parseLong(expiresInSt);
        long tiempoActual = System.currentTimeMillis();
        
        if (tiempoActual >= (expiresIn - 5000)) {
            redirigirLogin(request, response);
            return;
        }
        
    	String path = request.getRequestURI();
        
        for(Entry<String, String> entry : MAPA_ROL_ACCESO.entrySet()) {
        	if (path.startsWith(entry.getKey()) && !entry.getValue().equals(rol)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: Se requiere rol " + entry.getValue());
                return;
            }
        }
        
    	filterChain.doFilter(request, response);
    }
	
	private String leerCookie(HttpServletRequest request, String nombre) {
	    if (request.getCookies() != null) {
	        for (Cookie cookie : request.getCookies()) {
	            if (cookie.getName().equals(nombre)) {
	                return cookie.getValue();
	            }
	        }
	    }
	    return null;
	}
	
	private void redirigirLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if(session != null) {
			session.invalidate();
		}
		String[] cookiesAEliminar = {"token", "expires-in"};
	    for (String nombre : cookiesAEliminar) {
	        Cookie cookie = new Cookie(nombre, null);
	        cookie.setPath("/");
	        cookie.setMaxAge(0);
	        response.addCookie(cookie);
	    }
	    
		response.sendRedirect(request.getContextPath() + "/login");
	}
}
