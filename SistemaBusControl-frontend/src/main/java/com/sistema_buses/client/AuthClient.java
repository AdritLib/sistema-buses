package com.sistema_buses.client;

import com.sistema_buses.dto.usuario.LoginResponse;
import com.sistema_buses.dto.usuario.UsuarioCambiarClaveRequest;
import com.sistema_buses.dto.usuario.UsuarioLogin;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-client", url = "${backend.base-url}/auth")
public interface AuthClient {

    @PostMapping("/ingresar")
    LoginResponse login(@RequestBody UsuarioLogin loginRequest);
    
    @PostMapping("/cambiarClave")
    void cambiarClave(@RequestBody UsuarioCambiarClaveRequest request);
}