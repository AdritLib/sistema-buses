package com.sistema_buses.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthFiltro extends OncePerRequestFilter{
	
	private static final List<String> RUTAS_PUBLICAS = List.of(
        "/css/", "/js/", "/images/", "/login", "/cambiarClave", "/logout"
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
        HttpSession session = request.getSession(false);

    	if (session == null || session.getAttribute("token") == null || session.getAttribute("rol") == null) {
    		redirigirLogin(request, response);
            return;
        }
    	
    	Long expiresIn = (Long) session.getAttribute("expires-in");
        long tiempoActual = System.currentTimeMillis();
        //											(esto es el margen)
        if (expiresIn == null || tiempoActual >= (expiresIn - 5000)) {
            session.invalidate();
            redirigirLogin(request, response);
            return;
        }
    	
    	/*try {
    		//usuarioClient.validar();
    	}catch(Exception e) {
    		session.invalidate();
    		redirigirLogin(request, response);
            return;
    	}*/
    	
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
	
	private void redirigirLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(request.getContextPath() + "/login");
	}
}
