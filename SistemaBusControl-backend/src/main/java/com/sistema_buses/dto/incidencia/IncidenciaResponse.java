package com.sistema_buses.dto.incidencia;

import java.time.LocalDateTime;

import com.sistema_buses.enums.RecorridoEstado;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaResponse {
	private Long id;

	private Long recorridoID;
	private String recorridoNombre;

	private Long usuarioID;
	private String usuarioNombre;

	private String descripcion;
	private LocalDateTime fechaHoraSuceso;
	
	private String ruta;
	private String placa;
	private String vehiculo;
	private RecorridoEstado estadoRecorrido;
}