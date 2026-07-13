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
import com.sistema_buses.dto.incidencia.IncidenciaConductorRequest;
import com.sistema_buses.dto.incidencia.IncidenciaRequest;
import com.sistema_buses.dto.incidencia.IncidenciaResponse;

@FeignClient(name = "incidencia-client", url = "${backend.base-url}/api/incidencia", configuration = AppFrontConfig.class)
public interface IncidenciaClient {

    @GetMapping
    List<IncidenciaResponse> listar(@RequestParam int pagina);

    @GetMapping("/{id}")
    IncidenciaResponse obtenerPorId(@PathVariable Long id);

	@PostMapping
	IncidenciaResponse registrar(@RequestBody IncidenciaRequest request);
	
	@PostMapping("/comoconductor")
	IncidenciaResponse registrarComoConductor(@RequestBody IncidenciaConductorRequest request);
	
	@PutMapping("/{id}")
	IncidenciaResponse actualizarIncidencia(@PathVariable Long id, @RequestBody IncidenciaRequest request);

	@DeleteMapping("/{id}")
	void eliminarIncidencia(@PathVariable Long id);
}
