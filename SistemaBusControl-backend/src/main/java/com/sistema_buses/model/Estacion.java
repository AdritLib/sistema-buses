package com.sistema_buses.model;

import com.sistema_buses.enums.GenericoEstado;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estaciones")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Estacion {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, unique = true)
	private String nombre;
	
	@Column(nullable = false, unique = true)
	private String ubicacion;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "usuario_id", nullable = true)
	private Usuario supervisor;
	
	@Enumerated(EnumType.STRING)
	private GenericoEstado estado;

	@Override
	public String toString() {
		return "[id=" + id + ", nombre=" + nombre + ", ubicacion=" + ubicacion + ", supervisor=" + (supervisor == null ? null : supervisor.getId()) + ", estado=" + estado + "]";
	}
}
