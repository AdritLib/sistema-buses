package com.sistema_buses.dto.asignacion;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionRequest {
	private Long conductorID;
	private Long rutaID;
	private Long vehiculoID;
	
	@DateTimeFormat(pattern = "HH:mm:ss")
	private LocalTime horaInicio;
	
	@DateTimeFormat(pattern = "HH:mm:ss")
	private LocalTime horaFin;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate fecha;
	
	private String estado;

}