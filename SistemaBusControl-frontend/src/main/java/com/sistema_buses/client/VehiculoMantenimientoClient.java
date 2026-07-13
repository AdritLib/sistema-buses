package com.sistema_buses.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.sistema_buses.config.AppFrontConfig;
import com.sistema_buses.dto.vehiculo.VehiculoMantenimientoRequest;
import com.sistema_buses.dto.vehiculo.VehiculoMantenimientoResponse;

@FeignClient(name = "vehiculo-mantenimiento-client", url = "${backend.base-url}/api/vehiculo/mantenimiento", configuration = AppFrontConfig.class)
public interface VehiculoMantenimientoClient {

	@GetMapping
	List<VehiculoMantenimientoResponse> listar(@RequestParam("pagina") int pagina);

	@PostMapping
	VehiculoMantenimientoResponse registrar(@RequestBody VehiculoMantenimientoRequest request);

	@GetMapping("/{id}")
	VehiculoMantenimientoResponse obtenerPorId(@PathVariable("id") Long id);
	
	@PutMapping("/{id}")
	VehiculoMantenimientoResponse actualizar(@PathVariable("id") Long id, @RequestBody VehiculoMantenimientoRequest request);

	@DeleteMapping("/{id}")
	void eliminar(@PathVariable("id") Long id);
}