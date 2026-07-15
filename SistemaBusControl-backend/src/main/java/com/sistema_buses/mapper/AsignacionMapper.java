package com.sistema_buses.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.sistema_buses.dto.asignacion.AsignacionRequest;
import com.sistema_buses.dto.asignacion.AsignacionResponse;
import com.sistema_buses.exception.RutaNoEncontradaException;
import com.sistema_buses.exception.UsuarioNoEncontradoException;
import com.sistema_buses.exception.VehiculoNoEncontradoException;
import com.sistema_buses.model.Asignacion;
import com.sistema_buses.model.Ruta;
import com.sistema_buses.model.Usuario;
import com.sistema_buses.model.Vehiculo;
import com.sistema_buses.repository.AsignacionRepository;
import com.sistema_buses.repository.RutaRepository;
import com.sistema_buses.repository.UsuarioRepository;
import com.sistema_buses.repository.VehiculoRepository;

@Mapper(componentModel = "spring")
public abstract class AsignacionMapper {
	@Autowired
	protected AsignacionRepository asignacionRepository;
	
	@Autowired
	protected UsuarioRepository usuarioRepository;
	
	@Autowired
	protected VehiculoRepository vehiculoRepository;
	
	@Autowired
	protected RutaRepository rutaRepository;
	
	@Mapping(source = "conductor.id", target = "conductorID")
    @Mapping(source = "conductor.nombre", target = "conductorNombre")
    @Mapping(source = "ruta.id", target = "rutaID")
    @Mapping(source = "ruta.nombre", target = "rutaNombre")
    @Mapping(source = "vehiculo.id", target = "vehiculoID")
    @Mapping(source = "vehiculo.placa", target = "vehiculoPlaca")
	public abstract AsignacionResponse toResponse(Asignacion asignacion);
	
	@Mapping(source = "conductorID", target = "conductor", qualifiedByName = "conductor")
	@Mapping(source = "rutaID", target = "ruta", qualifiedByName = "ruta")
	@Mapping(source = "vehiculoID", target = "vehiculo", qualifiedByName = "vehiculo")
	@Mapping(target = "id", ignore = true)
	public abstract Asignacion toEntity(AsignacionRequest request);
	
	@Named("conductor")
	public Usuario conductor(Long conductorID) {
		return usuarioRepository.findById(conductorID).orElseThrow(UsuarioNoEncontradoException::new);
	}
	
	@Named("vehiculo")
	public Vehiculo vehiculo(Long vehiculoID) {
		return vehiculoRepository.findById(vehiculoID).orElseThrow(VehiculoNoEncontradoException::new);
	}
	
	@Named("ruta")
	public Ruta ruta(Long rutaID) {
		return rutaRepository.findById(rutaID).orElseThrow(RutaNoEncontradaException::new);
	}
}
