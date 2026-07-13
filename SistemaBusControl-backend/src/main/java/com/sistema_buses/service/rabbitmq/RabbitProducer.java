package com.sistema_buses.service.rabbitmq;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.sistema_buses.config.RabbitMQConfig;
import com.sistema_buses.enums.RegistroAccion;
import com.sistema_buses.model.Registro;
import com.sistema_buses.model.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RabbitProducer {
	private final RabbitTemplate template;
	
	public void enviar(RegistroAccion accion, String descripcion, String entidadAfectada) {
		UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		Registro registro = new Registro();
		registro.setAccion(accion);
		registro.setEntidadAfectada(entidadAfectada);
		registro.setDescripcion(descripcion);
		registro.setUsuarioID(user.getUser().getId());
		registro.setFecha(LocalDateTime.now());
		
		template.convertAndSend(RabbitMQConfig.COLA, registro);
	}
	public void enviar(RegistroAccion accion, String entidadAfectada) {
		enviar(accion, "Se "+accion.verbo()+" en la tabla de la entidad "+entidadAfectada, entidadAfectada);
	}
	
	public void enviar(RegistroAccion accion, String descripcion, Long usuarioId, String entidadAfectada) {
		Registro registro = new Registro();
		registro.setAccion(accion);
		registro.setEntidadAfectada(entidadAfectada);
		registro.setDescripcion(descripcion);
		registro.setUsuarioID(usuarioId);
		registro.setFecha(LocalDateTime.now());
		
		template.convertAndSend(RabbitMQConfig.COLA, registro);
	}
}
