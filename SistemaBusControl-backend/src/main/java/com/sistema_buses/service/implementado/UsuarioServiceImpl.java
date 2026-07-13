package com.sistema_buses.service.implementado;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistema_buses.dto.usuario.UsuarioCompletoResponse;
import com.sistema_buses.dto.usuario.UsuarioHabilitacionRequest;
import com.sistema_buses.dto.usuario.UsuarioRequest;
import com.sistema_buses.dto.usuario.UsuarioResponse;
import com.sistema_buses.enums.RegistroAccion;
import com.sistema_buses.exception.ErrorDeNegocioException;
import com.sistema_buses.exception.RolNoEncontradoException;
import com.sistema_buses.exception.UsuarioNoEncontradoException;
import com.sistema_buses.mapper.UsuarioMapper;
import com.sistema_buses.model.Rol;
import com.sistema_buses.model.UserDetailsImpl;
import com.sistema_buses.model.Usuario;
import com.sistema_buses.repository.RolRepository;
import com.sistema_buses.repository.UsuarioRepository;
import com.sistema_buses.service.UsuarioService;
import com.sistema_buses.service.rabbitmq.RabbitProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
	private final UsuarioRepository usuarioRepository;
	private final RolRepository rolRepository;
	private final RabbitProducer producer;
	private final String nombreEntidad = "Usuario";
	private final UsuarioMapper usuarioMapper;

	@Override
	public UsuarioCompletoResponse obtenerPerfil(@AuthenticationPrincipal UserDetailsImpl detalles) {
        if(detalles == null || detalles.getUser() == null) throw new UsuarioNoEncontradoException();
        return usuarioMapper.toCompleto(detalles.getUser());
    }
	@Override
	public List<UsuarioResponse> listar(int pagina, int tamanio) {
        return usuarioRepository.listar(PageRequest.of(pagina, tamanio, Sort.by("id"))).toList();
    }
    
	@Override
    public List<UsuarioResponse> listarConductores(int pagina, int tamanio) {
        return usuarioRepository.listarConductores(PageRequest.of(pagina, tamanio, Sort.by("id"))).toList();
    }
	
	@Override
	public List<UsuarioResponse> listarSupervisores(int pagina, int tamanio) {
		return usuarioRepository.listarSupervisores(PageRequest.of(pagina, tamanio, Sort.by("id"))).toList();
	}
    
	@Override
    public UsuarioCompletoResponse obtenerPorId(Long id) {
    	return usuarioRepository.obtenerPorId(id).orElseThrow(UsuarioNoEncontradoException::new);
    }

	@Override
	@Transactional
    public void establecerAcceso(UsuarioHabilitacionRequest request) {
		Long id = request.getUsuarioID();
    	Usuario user = buscarPorId(id);
    	boolean activo = request.isActivo();
    	
    	if(user.isActivo() == activo) {
    		throw new ErrorDeNegocioException(user.isActivo() ? "El usuario #"+id+" ya fue activado" : "Ese usuario #"+id+" ya fue desactivado");
    	}
    	
    	RegistroAccion accion = (activo) ? RegistroAccion.HABILITACION : RegistroAccion.DESHABILITACION;
    	user.setActivo(activo);
    	usuarioRepository.save(user);
    	producer.enviar(accion, "El acceso del usuario ID="+id+" se "+accion.verbo()+" por el motivo="+request.getMotivo(), nombreEntidad);
    }
	@Override
	@Transactional
	public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
		Usuario usuario = buscarPorId(id);
		usuario.setNumDocumento(request.getNumDocumento());
		usuario.setTipoDocumento(request.getTipoDocumento());
		usuario.setNombre(request.getNombre());
		usuario.setTelefono(request.getTelefono());
		usuario.setCorreo(request.getCorreo());
		
		Rol rol = rolRepository.findByNombre(request.getRol()).orElseThrow(RolNoEncontradoException::new);
		usuario.setRol(rol);
		
		Usuario guardado = usuarioRepository.save(usuario);
		producer.enviar(RegistroAccion.ACTUALIZAR, guardado.toString(), nombreEntidad);
		
		return usuarioMapper.toResponse(usuario);
	}
	
	private Usuario buscarPorId(Long id) {
		if(id == null) throw new ErrorDeNegocioException("No puede usar una ID nula.");
		return usuarioRepository.findById(id).orElseThrow(UsuarioNoEncontradoException::new);
	}
}
