package com.sistema_buses.exception;

public class RutaNoEncontradaException extends ErrorDeNegocioException {
	private static final long serialVersionUID = 1L;
	
	public RutaNoEncontradaException(Long rutaID) {
        super("Ruta no encontrada con ID: " + rutaID);
    }
	public RutaNoEncontradaException() {
		super("Ruta no encontrada");
	}
}
