package com.sistema_buses.dto.asignacion;

import java.time.LocalDate;
import java.time.LocalTime;

import com.sistema_buses.enums.GenericoEstado;

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
	private LocalDate fecha;
	
	private GenericoEstado estado;
}