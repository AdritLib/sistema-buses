package com.sistema_buses.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.sistema_buses.config.AppFrontConfig;
import com.sistema_buses.dto.vehiculo.VehiculoSeguroRequest;
import com.sistema_buses.dto.vehiculo.VehiculoSeguroResponse;

@FeignClient(name = "vehiculo-seguro-client", url = "${backend.base-url}/api/vehiculo/seguro", configuration = AppFrontConfig.class)
public interface VehiculoSeguroClient {

	@GetMapping
	List<VehiculoSeguroResponse> listarSeguros(@RequestParam("pagina") int pagina);

	@GetMapping("/{id}")
	VehiculoSeguroResponse obtenerSeguroPorId(@PathVariable("id") Long id);

	@PostMapping
	VehiculoSeguroResponse registrarSeguro(@RequestBody VehiculoSeguroRequest request);

	@PutMapping("/{id}")
	VehiculoSeguroResponse actualizarSeguro(@PathVariable("id") Long id, @RequestBody VehiculoSeguroRequest request);

	@DeleteMapping("/{id}")
	void eliminarSeguro(@PathVariable("id") Long id);
}