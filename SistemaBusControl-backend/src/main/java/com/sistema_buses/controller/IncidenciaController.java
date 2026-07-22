package com.sistema_buses.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sistema_buses.config.security.HasSupervisorOrAdminRol;
import com.sistema_buses.dto.incidencia.IncidenciaConductorRequest;
import com.sistema_buses.dto.incidencia.IncidenciaRequest;
import com.sistema_buses.dto.incidencia.IncidenciaResponse;
import com.sistema_buses.service.IncidenciaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/incidencia")
@RequiredArgsConstructor
@Tag(name = "Incidencia", description = "Manejo de las incidencias registradas.")
public class IncidenciaController {
	private final IncidenciaService incidenciaService;

	@Operation(summary = "Listar incidencias", description = "Devuelve una lista de incidencias.")
	@GetMapping
	@HasSupervisorOrAdminRol
	public ResponseEntity<List<IncidenciaResponse>> listar(@RequestParam int pagina, @RequestParam(defaultValue = "10") int size){
		return ResponseEntity.ok(incidenciaService.listar(pagina, size));
	}
	
	@Operation(summary = "Obtener incidencia por ID", description = "Devuelve una incidencia por ID.")
	@GetMapping("/{id}")
	@HasSupervisorOrAdminRol
	public ResponseEntity<IncidenciaResponse> encontrarPorID(@PathVariable Long id){
		return ResponseEntity.ok(incidenciaService.encontrarPorID(id));
	}
	
	@Operation(summary = "Registra incidencia", description = "Devuelve la incidencia registrada.")
	@PostMapping
	public ResponseEntity<IncidenciaResponse> registrar(@RequestBody IncidenciaRequest request){
		return ResponseEntity.ok(incidenciaService.registrar(request));
	}
	
	@Operation(summary = "Registra incidencia como conductor", description = "Devuelve la incidencia registrada.")
	@PostMapping("/comoconductor")
	public ResponseEntity<IncidenciaResponse> registrarComoConductor(@RequestBody IncidenciaConductorRequest request){
		return ResponseEntity.ok(incidenciaService.registrarComoConductor(request));
	}
	
	@PutMapping("/{id}")
	@Operation(summary = "Actualiza incidencia", description = "Devuelve la incidencia actualizada.")
	@HasSupervisorOrAdminRol
	public ResponseEntity<IncidenciaResponse> actualizar(@PathVariable Long id, @RequestBody IncidenciaRequest request){
		return ResponseEntity.ok(incidenciaService.actualizar(id, request));
	}
	
	@DeleteMapping("/{id}")
	@HasSupervisorOrAdminRol
	public ResponseEntity<Void> eliminar(@PathVariable Long id){
		incidenciaService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
