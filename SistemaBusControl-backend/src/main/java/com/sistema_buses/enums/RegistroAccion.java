package com.sistema_buses.enums;

public enum RegistroAccion {
	ACCION("%s"),
	INSERTAR("insertado"),
	ACTUALIZAR("actualizado"),
	ELIMINAR("eliminado"),
	RECORRIDO_INICIADO("iniciado"),
	LLEGADA_PARADERO("registrada"),
	INCIDENCIA_REGISTRADA("registrada"),
	RECORRIDO_FINALIZADO("finalizado"),
	HABILITACION("habilito"),
	DESHABILITACION("deshabilito"),
	CAMBIO_CLAVE("cambio de clave");
	
	private String verbo;
	private RegistroAccion(String verbo) {
		this.verbo = verbo;
	}
	
	public String verbo() {
		return verbo;
	}
	public String verbo(String verbo) {
		return verbo.formatted(verbo);
	}
}
