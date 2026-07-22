package com.sistema_buses.dto;

import com.sistema_buses.enums.GenericoEstado;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstacionResponse {
	private Long id;
	private String nombre;
	private String ubicacion;
	private Long supervisorId;
	private String supervisorNombre;
	private GenericoEstado estado;
}
