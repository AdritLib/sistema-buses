package com.sistema_buses.exception;

public class AsignacionNoEncontradoException extends ErrorDeNegocioException {
	private static final long serialVersionUID = 1L;
	
	public AsignacionNoEncontradoException(Long asignacionID) {
        super("Asignacion no encontrada con ID: " + asignacionID);
    }
	public AsignacionNoEncontradoException() {
        super("Asignacion no encontrada");
    }
}
