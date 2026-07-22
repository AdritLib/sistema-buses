package com.sistema_buses.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sistema_buses.dto.EstacionResponse;
import com.sistema_buses.model.Estacion;

@Repository
public interface EstacionRepository extends JpaRepository<Estacion, Long>{
	
	@Query(
			value = """
			SELECT NEW com.sistema_buses.dto.EstacionResponse(
					e.id, 
					e.nombre, 
					e.ubicacion, 
					s.id, 
					s.nombre,
					e.estado) 
			FROM Estacion e INNER JOIN e.supervisor s
			""",
			countQuery = "SELECT count(r) FROM Estacion r")
	Page<EstacionResponse> listar(Pageable page);
	
	@Query("""
			SELECT NEW com.sistema_buses.dto.EstacionResponse(
					e.id, 
					e.nombre, 
					e.ubicacion, 
					s.id, 
					s.nombre,
					e.estado) 
			FROM Estacion e INNER JOIN e.supervisor s WHERE e.id = :estacionId
			""")
	Optional<EstacionResponse> encontrarPorID(Long estacionId);
	
	@Modifying
	@Query(value = "UPDATE estaciones e SET e.usuario_id = null WHERE e.id = :id", nativeQuery = true)
	int desasignarSupervisor(Long id);
}
