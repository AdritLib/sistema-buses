package com.sistema_buses.controller;

import com.sistema_buses.config.security.HasAdminRol;
import com.sistema_buses.config.security.HasSupervisorOrAdminRol;
import com.sistema_buses.dto.usuario.UsuarioCompletoResponse;
import com.sistema_buses.dto.usuario.UsuarioHabilitacionRequest;
import com.sistema_buses.dto.usuario.UsuarioRequest;
import com.sistema_buses.dto.usuario.UsuarioResponse;
import com.sistema_buses.model.UserDetailsImpl;
import com.sistema_buses.service.UsuarioService;
import com.sistema_buses.service.autenticacion.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/usuario")
@RestController
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Manejo de los usuarios registrados.")
public class UsuarioController {
    private final AuthenticationService authService;
    private final UsuarioService usuarioService;
    
    @Operation(summary = "Perfil", description = "Devuelve los datos del usuario autenticado.")
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioCompletoResponse> obtenerPerfil(@AuthenticationPrincipal UserDetailsImpl usuario) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(usuario));
    }

    @Operation(summary = "Listar usuarios", description = "Devuelve los usuarios registrados en resultado simple.")
    @GetMapping
    @HasSupervisorOrAdminRol
    public ResponseEntity<List<UsuarioResponse>> listar(
    		@RequestParam(required = false) int tamanio, 
    		@RequestParam int pagina) {
        return ResponseEntity.ok(usuarioService.listar(pagina, tamanio));
    }
    
    @Operation(summary = "Listar usuarios con rol Conductor", description = "Devuelve los usuarios registrados en resultado simple.")
    @GetMapping("/conductores")
    @HasSupervisorOrAdminRol
    public ResponseEntity<List<UsuarioResponse>> listarConductores(
    		@RequestParam(required = false) int tamanio, 
    		@RequestParam int pagina) {
        return ResponseEntity.ok(usuarioService.listarConductores(pagina, tamanio));
    }
    
    @Operation(summary = "Listar usuarios con rol Supervisor", description = "Devuelve los usuarios registrados en resultado simple.")
    @GetMapping("/supervisores")
    @HasSupervisorOrAdminRol
    public ResponseEntity<List<UsuarioResponse>> listarSupervisores(
    		@RequestParam(required = false) int tamanio, 
    		@RequestParam int pagina) {
        return ResponseEntity.ok(usuarioService.listarSupervisores(pagina, tamanio));
    }
    
    @Operation(summary = "Obtener usuario por ID", description = "Devuelve a un usuario por su ID con todos sus datos.")
    @GetMapping("/{id}")
    @HasAdminRol
    public ResponseEntity<UsuarioCompletoResponse> obtenerPorId(@PathVariable Long id){
    	return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }
    
    @Operation(summary = "Registrar usuario", description = "Registra a un usuario y devuelve sus datos.")
    @PostMapping("/registrar")
    @HasAdminRol
    public ResponseEntity<UsuarioResponse> register(@RequestBody UsuarioRequest registerUserDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(registerUserDto));
    }
    
    @Operation(summary = "Establecer acceso de usuario", description = "Activar o desactivar el acceso de un usuario.")
    @PostMapping("/establecerActivo")
    @HasAdminRol
    public ResponseEntity<Void> establecerAcceso(@RequestBody UsuarioHabilitacionRequest request){
    	usuarioService.establecerAcceso(request);
    	return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario y lo devuelve.")
    @PutMapping("/{id}")
    @HasAdminRol
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id, @RequestBody UsuarioRequest request){
    	return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }
    
    
    @GetMapping("/validar")
    public ResponseEntity<Void> validar(){
    	return ResponseEntity.ok().build();
    }
}