package com.sistema_buses.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstacionRequest {
    private String nombre;
    private String ubicacion;
    private Long supervisorId;
    private String estado;
}