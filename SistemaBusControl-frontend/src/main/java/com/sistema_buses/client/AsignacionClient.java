package com.sistema_buses.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.sistema_buses.config.AppFrontConfig;
import com.sistema_buses.dto.asignacion.AsignacionRequest;
import com.sistema_buses.dto.asignacion.AsignacionResponse;

@FeignClient(name = "asignacion-client", url = "${backend.base-url}/api", configuration = AppFrontConfig.class)
public interface AsignacionClient {

	@GetMapping("/asignacion")
	List<AsignacionResponse> listarAsignaciones(@RequestParam("pagina") int pagina);

	@GetMapping("/asignacion/{id}")
	AsignacionResponse obtenerAsignacionPorId(@PathVariable("id") Long id);

	@PostMapping("/asignacion")
	AsignacionResponse registrarAsignacion(@RequestBody AsignacionRequest request);

	@PutMapping("/asignacion/{id}")
	AsignacionResponse actualizarAsignacion(
			@PathVariable("id") Long id, 
			@RequestBody AsignacionRequest request);

	@DeleteMapping("/asignacion/{id}")
	void eliminarAsignacion(@PathVariable("id") Long id);

	@PostMapping("/asignacion/{id}/asignarconductor/{conductorID}")
	String asignarConductor(
			@PathVariable("id") Long id,
			@PathVariable("conductorID") Long conductorID);

	@PostMapping("/asignacion/{id}/asignarruta/{rutaID}")
	String asignarRuta(
			@PathVariable("id") Long id,
			@PathVariable("rutaID") Long rutaID);
}