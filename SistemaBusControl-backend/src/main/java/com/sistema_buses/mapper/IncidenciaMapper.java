package com.sistema_buses.mapper;

import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.sistema_buses.dto.incidencia.IncidenciaRequest;
import com.sistema_buses.dto.incidencia.IncidenciaResponse;
import com.sistema_buses.enums.RecorridoEstado;
import com.sistema_buses.exception.ErrorDeNegocioException;
import com.sistema_buses.model.Incidencia;
import com.sistema_buses.model.Recorrido;
import com.sistema_buses.model.Usuario;
import com.sistema_buses.model.Vehiculo;
import com.sistema_buses.service.implementado.RecorridoServiceImpl;
import com.sistema_buses.service.implementado.UsuarioServiceImpl;

@Mapper(componentModel = "spring")
public abstract class IncidenciaMapper {
	@Autowired
	protected UsuarioServiceImpl usuarioServiceImpl;
	@Autowired
	protected RecorridoServiceImpl recorridoServiceImpl;

	@Mapping(source = "usuario.id", target = "usuarioID")
	@Mapping(source = "recorrido.id", target = "recorridoID")
	@Mapping(source = "usuario", target = "usuarioNombre", qualifiedByName = "nombreConductor")
	@Mapping(target = "ruta", source = "recorrido.asignacion.ruta.nombre")
	@Mapping(target = "placa", source = "recorrido.asignacion.vehiculo.placa")
	@Mapping(target = "vehiculo", source = "recorrido.asignacion.vehiculo", qualifiedByName = "vehiculoDetalles")
	@Mapping(target = "estadoRecorrido", source = "recorrido.estado")
	@Mapping(target = "recorridoNombre", source = ".", qualifiedByName = "recorridoNombre")
	public abstract IncidenciaResponse toResponse(Incidencia incidencia);
	
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "usuario", source = "usuarioID", qualifiedByName = "usuario")
	@Mapping(target = "recorrido", source = "recorridoID", qualifiedByName = "recorrido")
	@Mapping(target = "fechaHoraSuceso", source = ".", qualifiedByName = "requestFechaHoraSuceso")
	public abstract Incidencia toEntity(IncidenciaRequest request);
	
	@Named("nombreConductor")
	protected String nombreConductor(Usuario usuario) {
		if (usuario != null && usuario.getNombre() != null) {
			return usuario.getNombre();
		}
		/*Recorrido recorrido = incidencia.getRecorrido();
		if (recorrido.getAsignacion() != null && recorrido.getAsignacion().getConductor() != null) {
			return recorrido.getAsignacion().getConductor().getNombre();
		}*/
		return null;
	}
	
	@Named("vehiculoDetalles")
	protected String vehiculoDetalles(Vehiculo vehiculo) {
		return vehiculo.getMarca() + " " + vehiculo.getModelo();
	}
	
	@Named("recorridoNombre")
	protected String recorridoNombre(Incidencia incidencia) {
		if(incidencia.getRecorrido().getAsignacion().getRuta() == null) return "#" + incidencia.getRecorrido().getId();
		return "#" + incidencia.getRecorrido().getId() + " " + incidencia.getRecorrido().getAsignacion().getRuta().getNombre();
	}
	
	@Named("requestFechaHoraSuceso")
	protected LocalDateTime requestFechaHoraSuceso(IncidenciaRequest request) {
		return request.getFechaHoraSuceso() != null ? request.getFechaHoraSuceso() : LocalDateTime.now();
	}
	
	@Named("usuario")
	protected Usuario usuario(Long usuarioId) {
		return usuarioServiceImpl.buscarPorId(usuarioId);
	}
	
	@Named("recorrido")
	public Recorrido recorrido(Long recorriodId) {
		Recorrido recorrido = recorridoServiceImpl.buscarPorId(recorriodId);
		if(recorrido.getEstado() == RecorridoEstado.EN_CURSO) return recorrido;
		throw new ErrorDeNegocioException("No hay recorrido activo para registrar la incidencia.");
	}
}
