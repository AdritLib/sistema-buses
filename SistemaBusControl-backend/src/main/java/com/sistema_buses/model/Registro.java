package com.sistema_buses.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.sistema_buses.enums.RegistroAccion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "registros")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Registro {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	private RegistroAccion accion;
	
	@Column(nullable = false)
	private Long usuarioID;
	
	@Column(nullable = false, length = 250)
	private String descripcion;
	
	@Column(nullable = false, length = 100)
	private String entidadAfectada;
	
	@CreationTimestamp
	private LocalDateTime fecha;
}
