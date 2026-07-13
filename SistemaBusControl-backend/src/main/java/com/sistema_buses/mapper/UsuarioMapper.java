package com.sistema_buses.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.sistema_buses.dto.usuario.UsuarioCompletoResponse;
import com.sistema_buses.dto.usuario.UsuarioRequest;
import com.sistema_buses.dto.usuario.UsuarioResponse;
import com.sistema_buses.enums.Roles;
import com.sistema_buses.model.Rol;
import com.sistema_buses.model.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    
	UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "activo", ignore = true)
	@Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "rol", source = "rol", qualifiedByName = "rolesToRol")
	Usuario toEntity(UsuarioRequest request);
	
	@Mapping(target = "rol", source = "rol", qualifiedByName = "nombreRol")
	UsuarioCompletoResponse toCompleto(Usuario usuario);
	
	@Mapping(target = "rol", source = "rol", qualifiedByName = "nombreRol")
	UsuarioResponse toResponse(Usuario usuario);

    @Named("rolesToRol")
	default Rol rolesToRol(Roles roles) {
        if (roles == null) {
            return null;
        }
        Rol rolEntidad = new Rol();
        rolEntidad.setNombre(roles);
        return rolEntidad;
    }
    
    @Named("nombreRol")
    default String nombreRol(Rol rol) {
    	return rol.getNombre().toString();
    }
}
