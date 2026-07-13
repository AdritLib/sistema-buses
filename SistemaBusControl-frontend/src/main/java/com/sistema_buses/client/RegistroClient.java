package com.sistema_buses.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sistema_buses.config.AppFrontConfig;
import com.sistema_buses.dto.RegistroResponse;

@FeignClient(name = "registro-service", url = "${backend.base-url}/api/admin/registro", configuration = AppFrontConfig.class)
public interface RegistroClient {
	@GetMapping
	public List<RegistroResponse> listar(@RequestParam int pagina, @RequestParam int tamanio);
}
