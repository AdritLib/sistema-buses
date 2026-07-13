package com.sistema_buses.controller;

import com.sistema_buses.dto.usuario.LoginResponse;
import com.sistema_buses.dto.usuario.UsuarioCambiarClaveRequest;
import com.sistema_buses.dto.usuario.UsuarioLogin;
import com.sistema_buses.service.autenticacion.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Manejo de la autenticacion de usuarios.")
public class AutenticacionController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Iniciar sesion", description = "Devuelve un token de acceso.")
    @PostMapping("/ingresar")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody UsuarioLogin loginUserDto) {
        return ResponseEntity.ok(authenticationService.login(loginUserDto));
    }
    
    @GetMapping("/validar")
    public ResponseEntity<Void> validar(){
    	return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Cambiar clave", description = "Cambia la clave de un usuario.")
    @PostMapping("/cambiarClave")
    public ResponseEntity<Void> cambiarClave(@RequestBody UsuarioCambiarClaveRequest request){
    	authenticationService.cambiarClave(request);
    	return ResponseEntity.noContent().build();
    }
}