package com.sistema_buses.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistema_buses.dto.asignacion.AsignacionResponse;
import com.sistema_buses.enums.GenericoEstado;
import com.sistema_buses.model.Asignacion;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Long>{
	List<Asignacion> findByConductorIdAndFechaAndEstadoOrderByHoraInicioAsc(
			Long conductorId,
			LocalDate fecha,
			GenericoEstado estado);

	boolean existsByConductorIdAndVehiculoIdAndFecha(Long conductorID, Long vehiculoID, LocalDate fecha);
	
	@Query("""
		    SELECT new com.sistema_buses.dto.asignacion.AsignacionResponse(
		        a.id,
		        a.conductor.id,
		        u.nombre,
		        a.ruta.id,
		        r.nombre,
		        a.vehiculo.id,
		        v.placa,
		        a.horaInicio,
		        a.horaFin,
		        a.fecha,
		        a.estado
		    )
		    FROM Asignacion a
		    JOIN a.conductor u
		    JOIN a.ruta r
		    JOIN a.vehiculo v
		""")
	Page<AsignacionResponse> listar(Pageable page);
	
	@Query("""
		    SELECT new com.sistema_buses.dto.asignacion.AsignacionResponse(
		        a.id,
		        a.conductor.id,
		        u.nombre,
		        a.ruta.id,
		        r.nombre,
		        a.vehiculo.id,
		        v.placa,
		        a.horaInicio,
		        a.horaFin,
		        a.fecha,
		        a.estado
		    )
		    FROM Asignacion a
		    JOIN a.conductor u
		    JOIN a.ruta r
		    JOIN a.vehiculo v
		    WHERE a.id = :asignacionID
		""")
	Optional<AsignacionResponse> encontrarPorID(Long asignacionID);
	
	@Modifying
	@Query(value = "UPDATE asignaciones a SET a.usuario_id = :usuarioID WHERE a.id = :asignacionID", nativeQuery = true)
	int asignarConductor(Long asignacionID, Long usuarioID);
	
	@Modifying
	@Query(value = "UPDATE asignaciones a SET a.ruta_id = :rutaID WHERE a.id = :asignacionID", nativeQuery = true)
	int asignarRuta(Long asignacionID, Long rutaID);
}
