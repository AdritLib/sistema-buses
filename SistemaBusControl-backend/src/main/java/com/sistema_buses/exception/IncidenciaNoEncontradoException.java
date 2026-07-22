package com.sistema_buses.exception;

public class IncidenciaNoEncontradoException extends ErrorDeNegocioException {
	private static final long serialVersionUID = 1L;

    public IncidenciaNoEncontradoException() {
        super("Incidencia no encontrada");
    }
}
