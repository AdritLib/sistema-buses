package com.sistema_buses.client;

import com.sistema_buses.config.ApplicacionConfig;
import com.sistema_buses.dto.paradero.ParaderoRequest;
import com.sistema_buses.dto.paradero.ParaderoResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "paradero-client", url = "${backend.base-url}/api", configuration = ApplicacionConfig.class)
public interface ParaderoClient {

    @GetMapping("/paradero")
    List<ParaderoResponse> listarParaderos(@RequestParam int pagina, @RequestParam int size);

    @PostMapping("/paradero")
    ParaderoResponse registrarParadero(@RequestBody ParaderoRequest request);

    @GetMapping("/paradero/{id}")
    ParaderoResponse obtenerParaderoPorId(@PathVariable Long id);

    @PutMapping("/paradero/{id}")
    ParaderoResponse actualizarParadero(@PathVariable Long id, @RequestBody ParaderoRequest request);

    @DeleteMapping("/paradero/{id}")
    void eliminarParadero(@RequestHeader("Authorization") String token, @PathVariable("id") Long id);
}