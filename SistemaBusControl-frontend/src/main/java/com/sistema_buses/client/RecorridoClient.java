package com.sistema_buses.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.sistema_buses.config.AppFrontConfig;
import com.sistema_buses.dto.recorrido.RecorridoRequest;
import com.sistema_buses.dto.recorrido.RecorridoResponse;

@FeignClient(name = "recorrido", url = "${backend.base-url}/api", configuration = AppFrontConfig.class)
public interface RecorridoClient {

	@GetMapping("/recorrido")
	List<RecorridoResponse> listar(@RequestParam int pagina);

	@GetMapping("/recorrido/{id}")
	RecorridoResponse obtenerPorId(@PathVariable Long id);

	@PostMapping("/recorrido")
	RecorridoResponse registrar(@RequestBody RecorridoRequest request);

	@PutMapping("/recorrido/{id}")
	RecorridoResponse actualizar(
			@PathVariable Long id,
			@RequestBody RecorridoRequest request);
}