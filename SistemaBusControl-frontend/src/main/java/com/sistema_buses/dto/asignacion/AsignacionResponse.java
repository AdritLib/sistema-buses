package com.sistema_buses.dto.asignacion;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionResponse {

	private Long id;

	private Long conductorID;
	private String conductorNombre;

	private Long rutaID;
	private String rutaNombre;

	private Long vehiculoID;
	private String vehiculoPlaca;

	private LocalTime horaInicio;
	private LocalTime horaFin;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate fecha;
	
	private String estado;
}