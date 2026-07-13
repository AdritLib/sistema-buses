package com.sistema_buses.client;

import com.sistema_buses.config.AppFrontConfig;
import com.sistema_buses.dto.EstacionRequest;
import com.sistema_buses.dto.EstacionResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "estacion-client", url = "${backend.base-url}/api", configuration = AppFrontConfig.class)
public interface EstacionClient {

    @GetMapping("/estacion")
    List<EstacionResponse> listarEstaciones(
    		@RequestParam("pagina") int pagina,
    		@RequestParam(defaultValue = "10") int size);

    @PostMapping("/estacion")
    EstacionResponse registrarEstacion(@RequestBody EstacionRequest request);

    @GetMapping("/estacion/{id}")
    EstacionResponse obtenerEstacionPorId(@PathVariable("id") Long id);

    @PutMapping("/estacion/{id}")
    EstacionResponse actualizarEstacion(
    		@PathVariable("id") Long id, 
    		@RequestBody EstacionRequest request);

    @DeleteMapping("/estacion/{id}")
    void eliminarEstacion(@PathVariable("id") Long id);
}