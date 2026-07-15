package com.sistema_buses.service.implementado;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistema_buses.config.ApplicationConfig;
import com.sistema_buses.dto.asignacion.AsignacionRequest;
import com.sistema_buses.dto.asignacion.AsignacionResponse;
import com.sistema_buses.enums.RegistroAccion;
import com.sistema_buses.exception.AsignacionNoEncontradoException;
import com.sistema_buses.mapper.AsignacionMapper;
import com.sistema_buses.model.Asignacion;
import com.sistema_buses.repository.AsignacionRepository;
import com.sistema_buses.service.AsignacionService;
import com.sistema_buses.service.rabbitmq.RabbitProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsignacionServiceImpl implements AsignacionService {
	private final AsignacionRepository asignacionRepository;
	private final RabbitProducer producer;
	private final ApplicationConfig config;
	private final String nombreEntidad = "Asignacion";
	private final AsignacionMapper asignacionMapper;
	
	@Override
	public List<AsignacionResponse> listar(int pagina) {
		return asignacionRepository.listar(PageRequest.of(pagina, config.registrosPorPagina())).toList();
	}
	
	@Override
	public AsignacionResponse obtenerPorID(Long asignacionID) {
		return asignacionRepository.encontrarPorID(asignacionID).orElseThrow();
	}

	@Override
	public AsignacionResponse registrar(AsignacionRequest request) {
		return guardar(null, request);
	}

	@Override
	public AsignacionResponse actualizar(Long asignacionID, AsignacionRequest request) {
		return guardar(asignacionID, request);
	}
	
	@Override
	public void asignarConductor(Long asignacionID, Long usuarioID) {
		asignacionRepository.asignarConductor(asignacionID, usuarioID);
	}

	@Override
	public void asignarRuta(Long asignacionID, Long rutaID) {
		asignacionRepository.asignarRuta(asignacionID, rutaID);
	}
	
	@Transactional
	public AsignacionResponse guardar(Long asignacionID, AsignacionRequest request) {
		Asignacion entidad = asignacionMapper.toEntity(request);
		RegistroAccion accion;
		
		if(asignacionID == null) {
			accion = RegistroAccion.INSERTAR;
		}else {
			if(asignacionRepository.existsById(asignacionID)){
				entidad.setId(asignacionID);
			}else {
				throw new AsignacionNoEncontradoException(asignacionID);
			}
			accion = RegistroAccion.ACTUALIZAR;
		}
		/*
		Usuario conductor = usuarioRepository.findById(request.getConductorID()).orElseThrow(UsuarioNoEncontradoException::new);
		entidad.setConductor(conductor);

		Ruta ruta = rutaRepository.findById(request.getRutaID()).orElseThrow();
		entidad.setRuta(ruta);
		
		Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoID()).orElseThrow(VehiculoNoEncontradoException::new);
		entidad.setVehiculo(vehiculo);
		
		entidad.setFecha(request.getFecha());
		entidad.setHoraInicio(request.getHoraInicio());
		entidad.setHoraFin(request.getHoraFin());
		entidad.setEstado(request.getEstado());*/
		
		Asignacion guardado = asignacionRepository.save(entidad);
		producer.enviar(accion, nombreEntidad);

		return asignacionMapper.toResponse(guardado);
	}

	@Override
	public void eliminarPorID(Long id) {
		Asignacion entidad = asignacionRepository.findById(id)
				.orElseThrow(() -> new AsignacionNoEncontradoException());

		asignacionRepository.delete(entidad);

		producer.enviar(RegistroAccion.ELIMINAR, nombreEntidad);
	}
}
