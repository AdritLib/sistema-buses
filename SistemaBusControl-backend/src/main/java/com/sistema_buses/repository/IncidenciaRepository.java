package com.sistema_buses.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistema_buses.dto.incidencia.IncidenciaResponse;
import com.sistema_buses.model.Incidencia;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
	Page<Incidencia> findByUsuarioIdOrderByFechaHoraSucesoDesc(Long usuarioID, Pageable pageable);
	Page<Incidencia> findAllByOrderByFechaHoraSucesoDesc(Pageable pageable);
	
	@Query("""
		    SELECT new com.sistema_buses.dto.incidencia.IncidenciaResponse(
		        i.id,
		        i.recorrido.id,
		        CONCAT('#', i.recorrido.asignacion.id),
		        i.usuario.id,
		        i.usuario.nombre,
		        i.descripcion,
		        i.fechaHoraSuceso,
		        i.recorrido.asignacion.ruta.nombre,
				i.recorrido.asignacion.vehiculo.placa,
				CONCAT(i.recorrido.asignacion.vehiculo.marca, ' ', i.recorrido.asignacion.vehiculo.modelo),
				i.recorrido.estado
		    )
		    FROM Incidencia i
		    INNER JOIN Usuario u ON i.usuario.id = u.id
		    INNER JOIN Recorrido r ON i.recorrido.id = r.id
		    WHERE i.id = :incidenciaID
		""")
	Page<IncidenciaResponse> listar(Pageable page);
	
	@Query("""
		    SELECT new com.sistema_buses.dto.incidencia.IncidenciaResponse(
		        i.id,
		        i.recorrido.id,
		        CONCAT('#', i.recorrido.asignacion.id),
		        i.usuario.id,
		        i.usuario.nombre,
		        i.descripcion,
		        i.fechaHoraSuceso,
		        i.recorrido.asignacion.ruta.nombre,
				i.recorrido.asignacion.vehiculo.placa,
				CONCAT(i.recorrido.asignacion.vehiculo.marca, ' ', i.recorrido.asignacion.vehiculo.modelo),
				i.recorrido.estado
		    )
		    FROM Incidencia i
		    INNER JOIN Usuario u ON i.usuario.id = u.id
		    INNER JOIN Recorrido r ON i.recorrido.id = r.id
		    WHERE i.id = :incidenciaID
		""")
	Optional<IncidenciaResponse> encontrarPorID(Long incidenciaID);
	
	@Query("""
			SELECT new com.sistema_buses.dto.incidencia.IncidenciaResponse(
		        i.id,
		        i.recorrido.id,
		        CONCAT('#', i.recorrido.asignacion.id),
		        i.usuario.id,
		        i.usuario.nombre,
		        i.descripcion,
		        i.fechaHoraSuceso,
		        i.recorrido.asignacion.ruta.nombre,
				i.recorrido.asignacion.vehiculo.placa,
				CONCAT(i.recorrido.asignacion.vehiculo.marca, ' ', i.recorrido.asignacion.vehiculo.modelo),
				i.recorrido.estado
		    )
		    FROM Incidencia i ORDER BY i.fechaHoraSuceso DESC
			""")
	Page<IncidenciaResponse> listarPorFechaHoraSucesoDesc(Pageable page);
}
