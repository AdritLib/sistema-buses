package com.sistema_buses.service.implementado;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistema_buses.config.ApplicationConfig;
import com.sistema_buses.dto.ruta.RutaConductorResponse;
import com.sistema_buses.dto.paradero.ParaderoConductorResponse;
import com.sistema_buses.dto.paradero.ParaderoRecorridoConductorResponse;
import com.sistema_buses.dto.recorrido.MarcarLlegadaRequest;
import com.sistema_buses.dto.recorrido.RecorridoConductorResponse;
import com.sistema_buses.dto.recorrido.RecorridoRequest;
import com.sistema_buses.dto.recorrido.RecorridoResponse;
import com.sistema_buses.dto.vehiculo.VehiculoResponse;
import com.sistema_buses.enums.LlegadaEstado;
import com.sistema_buses.enums.RecorridoEstado;
import com.sistema_buses.enums.RegistroAccion;
import com.sistema_buses.exception.ErrorDeNegocioException;
import com.sistema_buses.model.Asignacion;
import com.sistema_buses.model.Paradero;
import com.sistema_buses.model.Recorrido;
import com.sistema_buses.model.RecorridoParadero;
import com.sistema_buses.model.Ruta;
import com.sistema_buses.model.RutaParadero;
import com.sistema_buses.model.Vehiculo;
import com.sistema_buses.repository.AsignacionRepository;
import com.sistema_buses.repository.RecorridoParaderoRepository;
import com.sistema_buses.repository.RecorridoRepository;
import com.sistema_buses.repository.RutaParaderoRepository;
import com.sistema_buses.service.RecorridoService;
import com.sistema_buses.service.autenticacion.UsuarioAutenticadoService;
import com.sistema_buses.service.rabbitmq.RabbitProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecorridoServiceImpl implements RecorridoService {
	private final RecorridoRepository recorridoRepository;
	private final AsignacionRepository asignacionRepository;
	private final RecorridoParaderoRepository recorridoParaderoRepository;
	private final RutaParaderoRepository rutaParaderoRepository;
	private final UsuarioAutenticadoService usuarioAutenticadoService;
	private final ApplicationConfig config;
	private final RabbitProducer producer;
	private final String entidadAfectada = "Recorrido";
	
	@Override
	public List<RecorridoResponse> listar(int pagina) {
		return recorridoRepository.listar(PageRequest.of(pagina, config.registrosPorPagina())).toList();
	}

	@Override
	public RecorridoResponse encontrarPorID(Long estacionID) {
		return recorridoRepository.encontrarPorID(estacionID).orElseThrow();
	}

	@Override
	public RecorridoResponse registrar(RecorridoRequest request) {
		return guardar(null, request);
	}

	@Override
	public RecorridoResponse actualizar(Long recorridoID, RecorridoRequest request) {
		return guardar(recorridoID, request);
	}

	@Override
	@Transactional
	public RecorridoConductorResponse iniciarParaConductor(Long asignacionID) {
		Asignacion asignacion = asignacionRepository.findById(asignacionID)
				.orElseThrow(() -> new ErrorDeNegocioException("La asignación indicada no existe."));
		validarPropietario(asignacion);
		if (!usuarioAutenticadoService.esAdmin() && !LocalDate.now().equals(asignacion.getFecha())) {
			throw new ErrorDeNegocioException("Solo se puede iniciar el recorrido de la asignación del día.");
		}

		Optional<Recorrido> recorridoEnCurso = recorridoRepository
				.findFirstByAsignacionIdAndEstadoOrderByIdDesc(asignacionID, RecorridoEstado.EN_CURSO);
		if (recorridoEnCurso.isPresent()) {
			return mapearRecorridoConductor(recorridoEnCurso.get());
		}
		Optional<Recorrido> recorridoFinalizado = recorridoRepository
				.findFirstByAsignacionIdAndEstadoOrderByIdDesc(asignacionID, RecorridoEstado.FINALIZADO);
		if (recorridoFinalizado.isPresent()) {
			throw new ErrorDeNegocioException("Esta asignación ya fue finalizada.");
		}

		Optional<Recorrido> existente = recorridoRepository.findFirstByAsignacionIdOrderByIdDesc(asignacionID);
		if (existente.isPresent() && existente.get().getEstado() == RecorridoEstado.EN_CURSO) {
			return mapearRecorridoConductor(existente.get());
		}
		if (existente.isPresent() && existente.get().getEstado() == RecorridoEstado.FINALIZADO) {
			throw new ErrorDeNegocioException("Esta asignación ya fue finalizada.");
		}
		if (existente.isPresent()
				&& (existente.get().getEstado() != null || existente.get().getHoraInicio() != null)) {
			throw new ErrorDeNegocioException("La asignación ya tiene un recorrido iniciado.");
		}

		Recorrido recorrido = existente.orElseGet(Recorrido::new);
		recorrido.setAsignacion(asignacion);
		recorrido.setHoraInicio(LocalTime.now());
		recorrido.setHoraFin(null);
		recorrido.setEstado(RecorridoEstado.EN_CURSO);
		Recorrido guardado = recorridoRepository.save(recorrido);

		Set<Long> paraderosRegistrados = recorridoParaderoRepository
				.findByRecorridoIdOrderByRutaParaderoOrdenAsc(guardado.getId())
				.stream()
				.map(parada -> parada.getRutaParadero().getId())
				.collect(Collectors.toSet());
		List<RecorridoParadero> paradas = rutaParaderoRepository
				.findByRutaIdOrderByOrdenAsc(asignacion.getRuta().getId())
				.stream()
				.filter(rutaParadero -> !paraderosRegistrados.contains(rutaParadero.getId()))
				.map(rutaParadero -> crearParadaPendiente(guardado, rutaParadero))
				.toList();
		if (!paradas.isEmpty()) {
			recorridoParaderoRepository.saveAll(paradas);
		}

		producer.enviar(
				RegistroAccion.RECORRIDO_INICIADO,
				"RECORRIDO_INICIADO recorrido=" + guardado.getId() + " asignacion=" + asignacionID,
				entidadAfectada);
		return mapearRecorridoConductor(guardado);
	}

	@Override
	@Transactional(readOnly = true)
	public RecorridoConductorResponse obtenerDetalleParaConductor(Long recorridoID) {
		return mapearRecorridoConductor(obtenerRecorridoAutorizado(recorridoID));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<RecorridoConductorResponse> obtenerActivoParaConductor(Long usuarioID) {
		usuarioAutenticadoService.validarMismoUsuarioOAdmin(usuarioID);
		return recorridoRepository
				.findFirstByAsignacionConductorIdAndEstadoOrderByIdDesc(usuarioID, RecorridoEstado.EN_CURSO)
				.map(this::mapearRecorridoConductor);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<RecorridoConductorResponse> obtenerUltimoParaAsignacion(Long asignacionID) {
		Asignacion asignacion = asignacionRepository.findById(asignacionID)
				.orElseThrow(() -> new ErrorDeNegocioException("La asignación indicada no existe."));
		validarPropietario(asignacion);
		return recorridoRepository
				.findFirstByAsignacionIdOrderByIdDesc(asignacionID)
				.map(this::mapearRecorridoConductor);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ParaderoConductorResponse> listarParaderosDelRecorrido(Long recorridoID) {
		obtenerRecorridoAutorizado(recorridoID);
		return recorridoParaderoRepository
				.findByRecorridoIdOrderByRutaParaderoOrdenAsc(recorridoID)
				.stream()
				.map(this::mapearLlegada)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ParaderoRecorridoConductorResponse> listarDetalleParaderosDelRecorrido(Long recorridoID) {
		obtenerRecorridoAutorizado(recorridoID);
		return recorridoParaderoRepository
				.findByRecorridoIdOrderByRutaParaderoOrdenAsc(recorridoID)
				.stream()
				.map(this::mapearDetalleLlegada)
				.toList();
	}

	@Override
	@Transactional
	public ParaderoConductorResponse marcarLlegada(
			Long recorridoID,
			Long rutaParaderoID,
			MarcarLlegadaRequest request) {
		Recorrido recorrido = obtenerRecorridoAutorizado(recorridoID);
		if (recorrido.getEstado() != RecorridoEstado.EN_CURSO) {
			throw new ErrorDeNegocioException("El recorrido no está activo.");
		}

		RutaParadero rutaParadero = rutaParaderoRepository.findById(rutaParaderoID)
				.orElseThrow(() -> new ErrorDeNegocioException("El paradero indicado no pertenece a una ruta registrada."));
		if (!rutaParadero.getRuta().getId().equals(recorrido.getAsignacion().getRuta().getId())) {
			throw new ErrorDeNegocioException("El paradero no pertenece a la ruta del recorrido.");
		}

		List<RecorridoParadero> paraderosOrdenados = sincronizarParaderosDelRecorrido(recorrido);
		RecorridoParadero llegada = paraderosOrdenados.stream()
				.filter(parada -> parada.getRutaParadero().getId().equals(rutaParaderoID))
				.findFirst()
				.orElseThrow(() -> new ErrorDeNegocioException("El paradero indicado no pertenece al recorrido."));
		validarParaderoAnteriorMarcado(paraderosOrdenados, llegada);
		if (llegada.getFechaHoraLlegada() != null || llegada.getEstadoLlegada() == LlegadaEstado.LLEGO) {
			throw new ErrorDeNegocioException("La llegada a este paradero ya fue registrada.");
		}

		llegada.setFechaHoraLlegada(LocalDateTime.now());
		llegada.setEstadoLlegada(LlegadaEstado.LLEGO);
		llegada.setObservaciones(request != null ? request.getObservaciones() : null);
		llegada = recorridoParaderoRepository.save(llegada);
		producer.enviar(
				RegistroAccion.LLEGADA_PARADERO,
				"[recorrido=" + recorridoID + ", rutaParadero=" + rutaParaderoID +", fechaHoraLlegada"+ llegada.getFechaHoraLlegada().toString() + "]",
				"Recorrido Paradero");

		return mapearLlegada(llegada);
	}

	@Override
	@Transactional
	public ParaderoConductorResponse marcarLlegadaParaConductor(
			Long rutaParaderoID,
			MarcarLlegadaRequest request) {
		Long usuarioID = usuarioAutenticadoService.obtenerUsuarioID();
		Recorrido recorrido = recorridoRepository
				.findFirstByAsignacionConductorIdAndEstadoOrderByIdDesc(
						usuarioID,
						RecorridoEstado.EN_CURSO)
				.orElseThrow(() -> new ErrorDeNegocioException("No tienes un recorrido en curso."));
		return marcarLlegada(recorrido.getId(), rutaParaderoID, request);
	}

	@Override
	@Transactional
	public RecorridoConductorResponse finalizarParaConductor(Long recorridoID) {
		Recorrido recorrido = obtenerRecorridoAutorizado(recorridoID);
		if (recorrido.getEstado() != RecorridoEstado.EN_CURSO) {
			throw new ErrorDeNegocioException("El recorrido ya fue finalizado.");
		}
		if (sincronizarParaderosDelRecorrido(recorrido).stream()
				.anyMatch(parada -> parada.getEstadoLlegada() != LlegadaEstado.LLEGO)) {
			throw new ErrorDeNegocioException("No puedes finalizar el recorrido. Aún hay paraderos pendientes.");
		}
		recorrido.setHoraFin(LocalTime.now());
		recorrido.setEstado(RecorridoEstado.FINALIZADO);
		recorrido = recorridoRepository.save(recorrido);
		producer.enviar(
				RegistroAccion.RECORRIDO_FINALIZADO,
				"RECORRIDO_FINALIZADO recorrido=" + recorridoID,
				entidadAfectada);
		return mapearRecorridoConductor(recorrido);
	}

	@Override
	@Transactional
	public RecorridoConductorResponse finalizarActivoParaConductor() {
		Long usuarioID = usuarioAutenticadoService.obtenerUsuarioID();
		Recorrido recorrido = recorridoRepository
				.findFirstByAsignacionConductorIdAndEstadoOrderByIdDesc(
						usuarioID,
						RecorridoEstado.EN_CURSO)
				.orElseThrow(() -> new ErrorDeNegocioException("No tienes un recorrido en curso."));
		return finalizarParaConductor(recorrido.getId());
	}

	@Transactional
	private RecorridoResponse guardar(Long recorridoID, RecorridoRequest request) {
		Recorrido entidad;
		RegistroAccion accion;
		if(recorridoID == null) {
			entidad = new Recorrido();
			accion = RegistroAccion.INSERTAR;
		}else {
			entidad = recorridoRepository.findById(recorridoID).orElseThrow();
			accion = RegistroAccion.ACTUALIZAR;
		}
		entidad.setEstado(request.getEstado());
		entidad.setHoraFin(request.getHoraFin());
		entidad.setHoraInicio(request.getHoraInicio());
		
		Asignacion asignacion = asignacionRepository.findById(request.getAsignacionID()).orElseThrow();
		entidad.setAsignacion(asignacion);
		
		Recorrido guardado = recorridoRepository.save(entidad);
		producer.enviar(accion, entidadAfectada);
		return RecorridoResponse.builder()
				.id(guardado.getId())
				.asignacionID(guardado.getAsignacion().getId())
				.estado(guardado.getEstado())
				.horaFin(guardado.getHoraFin())
				.horaInicio(guardado.getHoraInicio())
				.build();
	}
	
	public Recorrido buscarPorId(Long recorridoId) {
		return recorridoRepository.findById(recorridoId).orElseThrow();
	}

	private Recorrido obtenerRecorridoAutorizado(Long recorridoID) {
		Recorrido recorrido = recorridoRepository.findById(recorridoID)
				.orElseThrow(() -> new ErrorDeNegocioException("El recorrido indicado no existe."));
		validarPropietario(recorrido.getAsignacion());
		return recorrido;
	}

	private void validarPropietario(Asignacion asignacion) {
		if (asignacion.getConductor() == null) {
			throw new ErrorDeNegocioException("La asignación no tiene conductor.");
		}
		if (!usuarioAutenticadoService.esAdmin()
				&& !usuarioAutenticadoService.obtenerUsuarioID().equals(asignacion.getConductor().getId())) {
			throw new AccessDeniedException("El recorrido no pertenece al conductor autenticado.");
		}
	}

	private RecorridoParadero crearParadaPendiente(Recorrido recorrido, RutaParadero rutaParadero) {
		RecorridoParadero parada = new RecorridoParadero();
		parada.setRecorrido(recorrido);
		parada.setRutaParadero(rutaParadero);
		parada.setEstadoLlegada(LlegadaEstado.PENDIENTE);
		return parada;
	}

	private List<RecorridoParadero> sincronizarParaderosDelRecorrido(Recorrido recorrido) {
		List<RecorridoParadero> existentes = recorridoParaderoRepository
				.findByRecorridoIdOrderByRutaParaderoOrdenAsc(recorrido.getId());
		Set<Long> paraderosRegistrados = existentes.stream()
				.map(parada -> parada.getRutaParadero().getId())
				.collect(Collectors.toSet());
		List<RecorridoParadero> faltantes = rutaParaderoRepository
				.findByRutaIdOrderByOrdenAsc(recorrido.getAsignacion().getRuta().getId())
				.stream()
				.filter(rutaParadero -> !paraderosRegistrados.contains(rutaParadero.getId()))
				.map(rutaParadero -> crearParadaPendiente(recorrido, rutaParadero))
				.toList();
		if (!faltantes.isEmpty()) {
			recorridoParaderoRepository.saveAll(faltantes);
			return recorridoParaderoRepository.findByRecorridoIdOrderByRutaParaderoOrdenAsc(recorrido.getId());
		}
		return existentes;
	}

	private void validarParaderoAnteriorMarcado(
			List<RecorridoParadero> paraderosOrdenados,
			RecorridoParadero llegada) {
		int indice = paraderosOrdenados.indexOf(llegada);
		if (indice > 0) {
			RecorridoParadero anterior = paraderosOrdenados.get(indice - 1);
			if (anterior.getEstadoLlegada() != LlegadaEstado.LLEGO) {
				throw new ErrorDeNegocioException("Primero debes marcar el paradero anterior.");
			}
		}
	}

	private RecorridoConductorResponse mapearRecorridoConductor(Recorrido recorrido) {
		Asignacion asignacion = recorrido.getAsignacion();
		return RecorridoConductorResponse.builder()
				.id(recorrido.getId())
				.asignacionID(asignacion.getId())
				.fecha(asignacion.getFecha())
				.horaInicio(recorrido.getHoraInicio())
				.horaFin(recorrido.getHoraFin())
				.estado(recorrido.getEstado())
				.vehiculo(mapearVehiculo(asignacion.getVehiculo()))
				.ruta(mapearRuta(asignacion.getRuta()))
				.build();
	}

	private VehiculoResponse mapearVehiculo(Vehiculo vehiculo) {
		if (vehiculo == null) {
			return null;
		}
		return VehiculoResponse.builder()
				.id(vehiculo.getId())
				.placa(vehiculo.getPlaca())
				.marca(vehiculo.getMarca())
				.modelo(vehiculo.getModelo())
				.year(vehiculo.getYear())
				.numAsientos(vehiculo.getNumAsientos())
				.estado(vehiculo.getEstado())
				.build();
	}

	private RutaConductorResponse mapearRuta(Ruta ruta) {
		if (ruta == null) {
			return null;
		}
		List<ParaderoConductorResponse> paraderos = rutaParaderoRepository
				.findByRutaIdOrderByOrdenAsc(ruta.getId())
				.stream()
				.map(rutaParadero -> {
					Paradero paradero = rutaParadero.getParadero();
					return ParaderoConductorResponse.builder()
							.rutaParaderoID(rutaParadero.getId())
							.paraderoID(paradero.getId())
							.orden(rutaParadero.getOrden())
							.nombre(paradero.getNombre())
							.direccion(paradero.getDireccion())
							.referencia(paradero.getReferencia())
							.latitud(paradero.getLatitud())
							.longitud(paradero.getLongitud())
							.estado(rutaParadero.getEstado() != null ? rutaParadero.getEstado() : paradero.getEstado())
							.build();
				})
				.toList();
		return RutaConductorResponse.builder()
				.id(ruta.getId())
				.nombre(ruta.getNombre())
				.tipo(ruta.getTipo())
				.estado(ruta.getEstado())
				.estacionOrigenId(ruta.getOrigen() != null ? ruta.getOrigen().getId() : null)
				.estacionOrigenNombre(ruta.getOrigen() != null ? ruta.getOrigen().getNombre() : null)
				.estacionDestinoId(ruta.getDestino() != null ? ruta.getDestino().getId() : null)
				.estacionDestinoNombre(ruta.getDestino() != null ? ruta.getDestino().getNombre() : null)
				.paraderos(paraderos)
				.build();
	}

	private ParaderoConductorResponse mapearLlegada(RecorridoParadero llegada) {
		RutaParadero rutaParadero = llegada.getRutaParadero();
		Paradero paradero = rutaParadero.getParadero();
		return ParaderoConductorResponse.builder()
				.rutaParaderoID(rutaParadero.getId())
				.paraderoID(paradero.getId())
				.orden(rutaParadero.getOrden())
				.nombre(paradero.getNombre())
				.direccion(paradero.getDireccion())
				.referencia(paradero.getReferencia())
				.latitud(paradero.getLatitud())
				.longitud(paradero.getLongitud())
				.estadoLlegada(llegada.getEstadoLlegada())
				.fechaHoraLlegada(llegada.getFechaHoraLlegada())
				.build();
	}

	private ParaderoRecorridoConductorResponse mapearDetalleLlegada(RecorridoParadero llegada) {
		RutaParadero rutaParadero = llegada.getRutaParadero();
		Paradero paradero = rutaParadero.getParadero();
		return ParaderoRecorridoConductorResponse.builder()
				.idRutaParadero(rutaParadero.getId())
				.orden(rutaParadero.getOrden())
				.nombre(paradero.getNombre())
				.direccion(paradero.getDireccion())
				.referencia(paradero.getReferencia())
				.estadoLlegada(llegada.getEstadoLlegada())
				.horaLlegadaReal(llegada.getFechaHoraLlegada())
				.observaciones(llegada.getObservaciones())
				.build();
	}

}
