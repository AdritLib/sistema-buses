package com.sistema_buses.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.sistema_buses.dto.EstacionResponse;
import com.sistema_buses.model.Estacion;
import com.sistema_buses.model.Usuario;

@Mapper(componentModel = "spring")
public abstract class EstacionMapper {

	@Mapping(source = "supervisor", target = "supervisorId", qualifiedByName = "supervisorId")
	@Mapping(source = "supervisor", target = "supervisorNombre", qualifiedByName = "supervisorNombre")
	public abstract EstacionResponse toResponse(Estacion estacion);
	
	@Named("supervisorNombre")
	protected String supervisorNombre(Usuario usuario) {
		return usuario == null ? null : usuario.getNombre();
	}
	
	@Named("supervisorId")
	protected Long supervisorId(Usuario usuario) {
		return usuario == null ? null : usuario.getId();
	}
}