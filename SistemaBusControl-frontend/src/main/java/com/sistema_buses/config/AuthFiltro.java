package com.sistema_buses.config;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthFiltro extends OncePerRequestFilter{
	private static final Map<String, String> MAPA_ROL_ACCESO = Map.of(
        "/admin/", "ADMIN",
        "/supervisor/", "SUPERVISOR",
        "/conductor/", "CONDUCTOR"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/css") || path.equals("/login") || path.equals("/cambiarClave") || path.equals("/logout");
    }

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
        HttpSession session = request.getSession(false);

    	if (session == null || session.getAttribute("token") == null || session.getAttribute("rol") == null) {
    		response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
    	
    	String rol = (String) session.getAttribute("rol");
    	String path = request.getRequestURI();
        
        for(Map.Entry<String, String> entry : MAPA_ROL_ACCESO.entrySet()) {
        	if (path.startsWith(entry.getKey()) && !entry.getValue().equals(rol)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: Se requiere rol " + entry.getValue());
                return;
            }
        }
        
    	filterChain.doFilter(request, response);
    }
}
