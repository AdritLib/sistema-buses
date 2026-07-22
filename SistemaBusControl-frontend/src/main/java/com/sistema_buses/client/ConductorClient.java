package com.sistema_buses.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.sistema_buses.config.ApplicacionConfig;
import com.sistema_buses.dto.asignacion.AsignacionConductorResponse;
import com.sistema_buses.dto.recorrido.RecorridoConductorResponse;
import com.sistema_buses.dto.ruta.RutaConductorResponse;
import com.sistema_buses.dto.vehiculo.VehiculoResponse;

@FeignClient(name = "conductor-client", url = "${backend.base-url}/api", configuration = ApplicacionConfig.class)
public interface ConductorClient {

    @GetMapping("/conductor/{idUsuario}/asignacion-activa")
    AsignacionConductorResponse obtenerAsignacionActiva(
            @PathVariable("idUsuario") Long idUsuario);

    @GetMapping("/conductor/{idUsuario}/vehiculo-asignado")
    VehiculoResponse obtenerVehiculoAsignado(
            @PathVariable("idUsuario") Long idUsuario);

    @GetMapping("/conductor/{idUsuario}/ruta-hoy")
    RutaConductorResponse obtenerRutaHoy(
            @PathVariable("idUsuario") Long idUsuario);

    @GetMapping("/conductor/{idUsuario}/historial")
    List<RecorridoConductorResponse> obtenerHistorial(
            @PathVariable("idUsuario") Long idUsuario,
            @RequestParam("pagina") int pagina,
            @RequestParam("size") int size);
}
