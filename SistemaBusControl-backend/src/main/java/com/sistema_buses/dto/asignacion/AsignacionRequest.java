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
public class AsignacionRequest {
	private Long conductorID, rutaID, vehiculoID;
	private LocalTime horaInicio, horaFin;
	private LocalDate fecha;
	private GenericoEstado estado;
}
