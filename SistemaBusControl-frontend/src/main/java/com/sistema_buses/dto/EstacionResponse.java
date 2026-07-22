package com.sistema_buses.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstacionResponse {
    private Long id;
    private String nombre;
    private String ubicacion;
    private Long supervisorId;
    private String supervisorNombre;
    private String estado;
}