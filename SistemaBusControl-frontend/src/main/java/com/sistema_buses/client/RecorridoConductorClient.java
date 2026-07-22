package com.sistema_buses.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.sistema_buses.config.ApplicacionConfig;
import com.sistema_buses.dto.paradero.ParaderoConductorResponse;
import com.sistema_buses.dto.recorrido.IniciarRecorridoRequest;
import com.sistema_buses.dto.recorrido.MarcarLlegadaRequest;
import com.sistema_buses.dto.recorrido.RecorridoConductorResponse;

@FeignClient(name = "recorrido-conductor-client", url = "${backend.base-url}/api", configuration = ApplicacionConfig.class)
public interface RecorridoConductorClient {

    @GetMapping("/recorridos/{idRecorrido}")
    RecorridoConductorResponse obtenerDetalle(
            @PathVariable("idRecorrido") Long idRecorrido);

    @GetMapping("/recorridos/conductor/{idUsuario}/activo")
    ResponseEntity<RecorridoConductorResponse> obtenerActivo(
            @PathVariable("idUsuario") Long idUsuario);

    @GetMapping("/recorridos/asignacion/{idAsignacion}/ultimo")
    ResponseEntity<RecorridoConductorResponse> obtenerUltimoPorAsignacion(
            @PathVariable("idAsignacion") Long idAsignacion);

    @GetMapping("/recorridos/{idRecorrido}/paraderos")
    List<ParaderoConductorResponse> listarParaderos(
            @PathVariable("idRecorrido") Long idRecorrido);

    @PostMapping("/recorridos/iniciar")
    RecorridoConductorResponse iniciar(
            @RequestBody IniciarRecorridoRequest request);

    @PostMapping("/recorridos/{idRecorrido}/paraderos/{idRutaParadero}/llegada")
    ParaderoConductorResponse marcarLlegada(
            @PathVariable("idRecorrido") Long idRecorrido,
            @PathVariable("idRutaParadero") Long idRutaParadero,
            @RequestBody MarcarLlegadaRequest request);

    @PostMapping("/recorridos/{idRecorrido}/finalizar")
    RecorridoConductorResponse finalizar(
            @PathVariable("idRecorrido") Long idRecorrido);
}
