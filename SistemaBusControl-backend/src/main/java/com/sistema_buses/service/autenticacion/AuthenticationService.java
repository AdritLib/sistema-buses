package com.sistema_buses.service.autenticacion;

import com.sistema_buses.dto.usuario.LoginResponse;
import com.sistema_buses.dto.usuario.UsuarioCambiarClaveRequest;
import com.sistema_buses.dto.usuario.UsuarioLogin;
import com.sistema_buses.dto.usuario.UsuarioRequest;
import com.sistema_buses.dto.usuario.UsuarioResponse;
import com.sistema_buses.enums.RegistroAccion;
import com.sistema_buses.exception.ErrorDeNegocioException;
import com.sistema_buses.exception.RolNoEncontradoException;
import com.sistema_buses.exception.UsuarioNoEncontradoException;
import com.sistema_buses.model.Rol;
import com.sistema_buses.model.UserDetailsImpl;
import com.sistema_buses.model.Usuario;
import com.sistema_buses.repository.RolRepository;
import com.sistema_buses.repository.UsuarioRepository;
import com.sistema_buses.service.rabbitmq.RabbitProducer;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
    private final UsuarioRepository userRepository;
    private final RolRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RabbitProducer producer;
	private final String nombreEntidad = "Auth";
    private final AuthenticationManager authenticationManager;
    
    public AuthenticationService(UsuarioRepository userRepository, 
    		RolRepository roleRepository,
			PasswordEncoder passwordEncoder, 
			JwtService jwtService,
			@Lazy AuthenticationManager authenticationManager,
			RabbitProducer producer) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
		this.producer = producer;
	}

    @Transactional
	public UsuarioResponse signup(UsuarioRequest request) {
        Rol role = roleRepository.findByNombre(request.getRol()).orElseThrow(RolNoEncontradoException::new);
        
        Usuario user = new Usuario(
        		request.getCorreo(), 
        		request.getNombre(), 
        		passwordEncoder.encode(request.getClave()), 
        		request.getTelefono(), 
        		request.getTipoDocumento(), 
        		request.getNumDocumento(),
        		role);
        
        Usuario guardado = userRepository.save(user);
        producer.enviar(RegistroAccion.INSERTAR, nombreEntidad);
        
        return UsuarioResponse.builder()
        		.correo(guardado.getCorreo())
        		.nombre(guardado.getNombre())
        		.rol(guardado.getRol().getNombre().name())
        		.id(guardado.getId())
        		.activo(guardado.isActivo())
        		.build();
    }

    public LoginResponse login(UsuarioLogin login) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getCorreo(),login.getClave()));
        Usuario user = userRepository.findByCorreo(login.getCorreo()).orElseThrow(() 
        		-> new UsuarioNoEncontradoException(login.getCorreo()));
        UserDetailsImpl details = new UserDetailsImpl(user);
        LoginResponse respuesta = new LoginResponse(jwtService.generateToken(details), jwtService.getExpirationTime());
        return respuesta;
    }
    
	@Transactional
	public void cambiarClave(UsuarioCambiarClaveRequest request) {
		if(request.getCorreo() == null) throw new ErrorDeNegocioException("Debes ingresar un correo electrónico.");
		Usuario usuario = userRepository.findByCorreo(request.getCorreo()).orElseThrow(() -> new UsuarioNoEncontradoException());
		if(!passwordEncoder.matches(request.getClave(), usuario.getClave())) throw new ErrorDeNegocioException("Clave ingresada incorrecta.");
		String nueva = passwordEncoder.encode(request.getNuevaClave());
		if(nueva.equals(usuario.getClave())) throw new ErrorDeNegocioException("Estas enviando la misma clave.");
		usuario.setClave(nueva);
		Usuario guardado = userRepository.save(usuario);
		producer.enviar(RegistroAccion.CAMBIO_CLAVE, "Cambio de clave del usuario ID="+guardado.getId()+", Correo="+guardado.getCorreo(), guardado.getId(), nombreEntidad);
	}
}
