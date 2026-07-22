package com.sistema_buses.service.implementado;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistema_buses.dto.incidencia.IncidenciaConductorRequest;
import com.sistema_buses.dto.incidencia.IncidenciaRequest;
import com.sistema_buses.dto.incidencia.IncidenciaResponse;
import com.sistema_buses.exception.IncidenciaNoEncontradoException;
import com.sistema_buses.mapper.IncidenciaMapper;
import com.sistema_buses.enums.RegistroAccion;
import com.sistema_buses.model.Incidencia;
import com.sistema_buses.model.Recorrido;
import com.sistema_buses.repository.IncidenciaRepository;
import com.sistema_buses.service.IncidenciaService;
import com.sistema_buses.service.autenticacion.UsuarioAutenticadoService;
import com.sistema_buses.service.rabbitmq.RabbitProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidenciaServiceImpl implements IncidenciaService {
	private final IncidenciaRepository incidenciaRepository;
	private final UsuarioAutenticadoService usuarioAutenticadoService;
	private final RabbitProducer producer;
	private final String entidadAfectada = "Incidencia";
	private final IncidenciaMapper incidenciaMapper;
	
	@Override
	public List<IncidenciaResponse> listar(int pagina, int size) {
		return incidenciaRepository.listarPorFechaHoraSucesoDesc(PageRequest.of(pagina, size)).toList();
	}

	@Override
	public List<IncidenciaResponse> listarParaConductor(Long usuarioID, int pagina, int size) {
		usuarioAutenticadoService.validarMismoUsuarioOAdmin(usuarioID);
		return incidenciaRepository.listarPorFechaHoraSucesoDesc(PageRequest.of(pagina, size)).toList();
	}

	@Override
	public IncidenciaResponse encontrarPorID(Long incidenciaId) {
		return incidenciaRepository.encontrarPorID(incidenciaId).orElseThrow(IncidenciaNoEncontradoException::new);
	}

	@Override
	public IncidenciaResponse registrar(IncidenciaRequest request) {
		return guardar(null, request);
	}

	@Override
	public IncidenciaResponse registrarComoConductor(IncidenciaConductorRequest request) {
		Long usuarioID = usuarioAutenticadoService.obtenerUsuarioID();
		
		Recorrido recorrido = incidenciaMapper.recorrido(request.getRecorridoID());
		if (!usuarioAutenticadoService.esAdmin()
				&& (recorrido.getAsignacion().getConductor() == null
						|| !usuarioID.equals(recorrido.getAsignacion().getConductor().getId()))) {
			throw new AccessDeniedException("No puedes reportar incidencias de un recorrido ajeno.");
		}

		IncidenciaRequest requestInterno = IncidenciaRequest.builder()
				.recorridoID(request.getRecorridoID())
				.usuarioID(usuarioID)
				.descripcion(request.getDescripcion())
				.fechaHoraSuceso(LocalDateTime.now())
				.build();
		return guardar(null, requestInterno);
	}

	@Override
	public IncidenciaResponse actualizar(Long incidencia, IncidenciaRequest request) {
		return guardar(incidencia, request);
	}
	
	@Transactional
	public IncidenciaResponse guardar(Long incidenciaID, IncidenciaRequest request) {
		Incidencia entidad = incidenciaMapper.toEntity(request);
		RegistroAccion accion = RegistroAccion.INSERTAR;
		
		if(incidenciaID != null) {
			accion = RegistroAccion.ACTUALIZAR;
		}
		
		Incidencia guardado = incidenciaRepository.save(entidad);
		producer.enviar(accion, guardado.toString(), entidadAfectada);
		
		return incidenciaMapper.toResponse(guardado);
	}

	@Override
	public void eliminar(Long id) {
		Incidencia entidad = incidenciaRepository.findById(id).orElseThrow();
		incidenciaRepository.delete(entidad);

		producer.enviar(RegistroAccion.ELIMINAR, entidadAfectada);
	}
}

