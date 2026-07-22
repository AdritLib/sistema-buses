package com.sistema_buses.client;

import com.sistema_buses.config.FeignInterceptor;
import com.sistema_buses.dto.usuario.UsuarioCompletoResponse;
import com.sistema_buses.dto.usuario.UsuarioHabilitacionRequest;
import com.sistema_buses.dto.usuario.UsuarioRequest;
import com.sistema_buses.dto.usuario.UsuarioResponse;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "usuario-client", url = "${backend.base-url}/api/usuario", configuration = FeignInterceptor.class)
public interface UsuarioClient {

    @GetMapping("/perfil")
    ResponseEntity<UsuarioCompletoResponse> obtenerPerfil();
    
    @GetMapping
    List<UsuarioResponse> listarUsuarios(
    		@RequestParam int pagina, 
    		@RequestParam int tamanio);
    
    @GetMapping("/conductores")
    List<UsuarioResponse> listarConductores(
    		@RequestParam int pagina, 
    		@RequestParam int tamanio);
    
    @GetMapping("/supervisores")
    List<UsuarioResponse> listarSupervisores(
    		@RequestParam int pagina, 
    		@RequestParam int tamanio);

    @GetMapping("/{id}")
    UsuarioCompletoResponse obtenerPorId(@PathVariable Long id);
    
    @PostMapping("/establecerActivo")
    void establecerActivo(@RequestBody UsuarioHabilitacionRequest request);
    
    @PostMapping("/registrar")
    UsuarioResponse registrar(@RequestBody UsuarioRequest request);
    
    @PutMapping("/{id}")
    UsuarioResponse actualizar(@PathVariable Long id, @RequestBody UsuarioRequest request);
    
    @GetMapping("/validar")
    void validar();
}