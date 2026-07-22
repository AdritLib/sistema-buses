package com.sistema_buses.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.sistema_buses.dto.usuario.LoginResponse;
import com.sistema_buses.service.autenticacion.JwtService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Component
@RequiredArgsConstructor
public class AutenticacionFiltro extends OncePerRequestFilter {
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    	String path = request.getRequestURI();
        return path.startsWith("/auth");
    }
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String jwt = leerCookie(request, "token");

        if(jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String userEmail = jwtService.extractUsername(jwt);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (userEmail != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                           userDetails, null, userDetails.getAuthorities()
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    if (!request.getRequestURI().equals("/api/usuario/validar") && jwtService.shouldTokenBeRenewed(jwt)) {
                        String newToken = jwtService.generateToken(userDetails);
                        String newExpiresIn = jwtService.expiresIn(newToken).toString();

                        crearCookiesNecesarias(response, LoginResponse.builder().token(newToken).expiresIn(Long.parseLong(newExpiresIn)).build());
                    }
                }
            }
            filterChain.doFilter(request, response);
        } catch (JwtException exception) {
        	SecurityContextHolder.clearContext();
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
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
    
    public static void crearCookiesNecesarias(
    		HttpServletResponse response,
    		LoginResponse loginResponse
    		) {
    	Map<String, String> cookies = new HashMap<>();
    	if(loginResponse != null) {
    		cookies.put("token", loginResponse.getToken());
    		cookies.put("expires-in", loginResponse.getExpiresIn()+"");
    	}
    	
    	for(Entry<String, String> cookie : cookies.entrySet()) {
    		ResponseCookie item = ResponseCookie.from(cookie.getKey(), cookie.getValue())
    				.httpOnly(true)
    				.secure(false)
    				.path("/")
    				.maxAge(35)
    				.build();
    		
    		response.addHeader(HttpHeaders.SET_COOKIE, item.toString());
    		//response.addCookie(item);
    	}
    }
}