package com.sistema_buses.controller;

import com.sistema_buses.client.EstacionClient;
import com.sistema_buses.client.ParaderoClient;
import com.sistema_buses.client.RegistroClient;
import com.sistema_buses.client.RutaClient;
import com.sistema_buses.client.RutaParaderoClient;
import com.sistema_buses.client.UsuarioClient;
import com.sistema_buses.client.VehiculoClient;
import com.sistema_buses.dto.EstacionRequest;
import com.sistema_buses.dto.EstacionResponse;
import com.sistema_buses.dto.RegistroResponse;
import com.sistema_buses.dto.paradero.ParaderoRequest;
import com.sistema_buses.dto.paradero.ParaderoResponse;
import com.sistema_buses.dto.ruta.RutaRequest;
import com.sistema_buses.dto.ruta.RutaResponse;
import com.sistema_buses.dto.ruta.RutaParaderoRequest;
import com.sistema_buses.dto.usuario.UsuarioCompletoResponse;
import com.sistema_buses.dto.usuario.UsuarioHabilitacionRequest;
import com.sistema_buses.dto.usuario.UsuarioRequest;
import com.sistema_buses.dto.usuario.UsuarioResponse;
import com.sistema_buses.dto.vehiculo.VehiculoRequest;
import com.sistema_buses.dto.vehiculo.VehiculoResponse;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminViewController {
    private final VehiculoClient vehiculoClient;
    private final UsuarioClient usuarioClient;
    private final ParaderoClient paraderoClient;
    private final EstacionClient estacionClient;
    private final RutaClient rutaClient;
    private final RutaParaderoClient rutaParaderoClient;
    private final RegistroClient registroClient;

	public AdminViewController(VehiculoClient vehiculoClient, UsuarioClient usuarioClient,
			ParaderoClient paraderoClient, EstacionClient estacionClient, RutaClient rutaClient,
			RutaParaderoClient rutaParaderoClient, RegistroClient registroClient) {
		this.vehiculoClient = vehiculoClient;
		this.usuarioClient = usuarioClient;
		this.paraderoClient = paraderoClient;
		this.estacionClient = estacionClient;
		this.rutaClient = rutaClient;
		this.rutaParaderoClient = rutaParaderoClient;
		this.registroClient = registroClient;
	}

	// ==========================================
    // DASHBOARD (INICIO)
    // ==========================================
    @GetMapping("/dashboard")
    public String dashboardAdmin(HttpSession session, Model model) {
        String rol = (String) session.getAttribute("rol");

        int totalBuses = 0, totalPersonal = 0, totalParaderos = 0, totalEstaciones = 0;

        try {
            List<VehiculoResponse> buses = vehiculoClient.listarVehiculos(0);
            if (buses != null) totalBuses = buses.size();

            List<UsuarioResponse> personal = usuarioClient.listarUsuarios(0, 100);
            if (personal != null) totalPersonal = personal.size();

            List<ParaderoResponse> paraderosList = paraderoClient.listarParaderos(0, 100);
            if (paraderosList != null) totalParaderos = paraderosList.size();

            List<EstacionResponse> estacionesList = estacionClient.listarEstaciones(0, 10); //(cabeceraAuth, 0);
            if (estacionesList != null) totalEstaciones = estacionesList.size();

        } catch (Exception e) {
            System.err.println("Error al cargar contadores de Dashboard: " + e.getMessage());
        }

        model.addAttribute("totalBuses", totalBuses);
        model.addAttribute("totalPersonal", totalPersonal); 
        model.addAttribute("totalParaderos", totalParaderos);
        model.addAttribute("totalEstaciones", totalEstaciones); 
        model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        model.addAttribute("rol", rol);
        model.addAttribute("menuActivo", "dashboard"); 

        return "dashboards/admin-view"; 
    }

    // ==========================================
    // MÓDULO DE VEHÍCULOS
    // ==========================================
    @GetMapping("/vehiculos")
    public String listarBuses(@RequestParam(defaultValue = "0") int pagina, 
                              @RequestParam(required = false) Long idBuscado, 
                              HttpSession session, Model model) {
        try {
            List<VehiculoResponse> lista = new ArrayList<>();
            
            if (idBuscado != null) {
                try {
                    lista.add(vehiculoClient.obtenerVehiculoPorId(idBuscado));
                } catch(Exception e) { model.addAttribute("error", "No se encontró el Vehículo con ID " + idBuscado); }
            } else {
                lista = vehiculoClient.listarVehiculos(pagina);
            }
            
            model.addAttribute("buses", lista);
            model.addAttribute("paginaActual", pagina);
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
            model.addAttribute("menuActivo", "vehiculos"); 
        } catch (Exception e) {
            model.addAttribute("error", "No se pudieron cargar los vehículos.");
        }
        return "admin/vehiculos-lista"; 
    }
    
    @GetMapping("/vehiculos/nuevo")
    public String formularioNuevoBus(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null || !"ADMIN".equals(session.getAttribute("rol"))) return "redirect:/login";
        model.addAttribute("vehiculoForm", new VehiculoRequest());
        model.addAttribute("menuActivo", "vehiculos"); 
        return "admin/vehiculos-form"; 
    }

    @PostMapping("/vehiculos/guardar")
    public String guardarBus(@ModelAttribute("vehiculoForm") VehiculoRequest request, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";
        try {
            vehiculoClient.registrarVehiculo(request);//"Bearer " + token, request);
            return "redirect:/admin/vehiculos?exito=true";
        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el vehículo. Asegúrate de que los campos sean válidos.");
            model.addAttribute("menuActivo", "vehiculos");
            return "admin/vehiculos-form";
        }
    }
    
    @GetMapping("/vehiculos/editar/{id}")
    public String formularioEditarBus(@PathVariable Long id, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null || !"ADMIN".equals(session.getAttribute("rol"))) return "redirect:/login";
        try {
            VehiculoResponse busExistente = vehiculoClient.obtenerVehiculoPorId(id);
            model.addAttribute("vehiculoForm", busExistente);
            model.addAttribute("esEdicion", true); 
            model.addAttribute("busId", id);
            model.addAttribute("menuActivo", "vehiculos");
            return "admin/vehiculos-form";
        } catch (Exception e) {
            return "redirect:/admin/vehiculos?errorEditar=true";
        }
    }

    @PostMapping("/vehiculos/actualizar/{id}")
    public String actualizarBus(@PathVariable("id") Long id, @ModelAttribute("vehiculoForm") VehiculoRequest request, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";
        try {
            vehiculoClient.actualizarVehiculo(id, request);
            return "redirect:/admin/vehiculos?actualizado=true";
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el vehículo.");
            model.addAttribute("esEdicion", true);
            model.addAttribute("busId", id);
            model.addAttribute("menuActivo", "vehiculos");
            return "admin/vehiculos-form";
        }
    }
    
    @GetMapping("/vehiculos/eliminar/{id}")
    public String eliminarBus(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";
        try {
            vehiculoClient.eliminarVehiculo(id);
            return "redirect:/admin/vehiculos?eliminado=true";
        } catch (Exception e) {
            return "redirect:/admin/vehiculos?errorEliminar=true";
        }
    }
    
    // ==========================================
    // MÓDULO DE PERSONAL
    // ==========================================
    @GetMapping("/personal")
    public String listarPersonal(@RequestParam(defaultValue = "0") int pagina, HttpSession session, Model model) {

        try {
            List<UsuarioResponse> lista = usuarioClient.listarUsuarios(pagina, 10);
            
            model.addAttribute("personal", lista);
            model.addAttribute("paginaActual", pagina); 
            model.addAttribute("menuActivo", "personal");
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo conectar con el servicio para listar el personal.");
        }
        return "admin/personal-lista";
    }

    @GetMapping("/personal/establecerActivo/{id}")
    public String establecerActivoPersonal(
    		@PathVariable Long id, 
    		@RequestParam boolean activo, 
    		@RequestParam String motivo,
    		HttpSession session, 
    		RedirectAttributes redirect) {
        try {
            UsuarioHabilitacionRequest request = new UsuarioHabilitacionRequest();
            request.setUsuarioID(id);
            request.setMotivo(motivo);
            request.setActivo(activo);
            usuarioClient.establecerActivo(request);
            return "redirect:/admin/personal?cambioExito=true";
        } catch (Exception e) {
        	redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/personal?errorAccion=true";
        }
    }

    @GetMapping("/personal/form")
    public String formularioNuevoPersonal(@RequestParam(required = false) Long id, HttpSession session, Model model) {
    	UsuarioRequest usuario = new UsuarioRequest();
    	if(id != null) {
    		UsuarioCompletoResponse datos = usuarioClient.obtenerPorId(id);
    		usuario.setCorreo(datos.getCorreo());
    		usuario.setNumDocumento(datos.getNumDocumento());
    		usuario.setTelefono(datos.getTelefono());
    		usuario.setRol(datos.getRol());
    		usuario.setTipoDocumento(datos.getTipoDocumento());
    		usuario.setNombre(datos.getNombre());
    		model.addAttribute("id", datos.getId());
    	}
        model.addAttribute("usuarioForm", usuario);
        model.addAttribute("menuActivo", "personal");
        model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        return "admin/personal-form"; 
    }

    @PostMapping("/personal/guardar")
    public String guardarPersonal(
    		@RequestParam(required = false) Long id,
    		@ModelAttribute("usuarioForm") UsuarioRequest request, HttpSession session, Model model) {

        try {
        	if(id == null) {
        		usuarioClient.registrar(request);
        	}else {
        		usuarioClient.actualizar(id, request);
        	}
            
            return "redirect:/admin/personal?exito=true";
            
        } catch (feign.FeignException e) {
            model.addAttribute("error", "No se pudo completar el proceso.");
            model.addAttribute("usuarioForm", request); 
            model.addAttribute("menuActivo", "personal");
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
            return "admin/personal-form";
            
        } catch (Exception e) {
            model.addAttribute("error", "Tuvimos un problema al intentar guardar la información. Por favor, inténtelo nuevamente.");
            model.addAttribute("usuarioForm", request);
            model.addAttribute("menuActivo", "personal");
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
            return "admin/personal-form";
        }
    }


    // ==========================================
    // MÓDULO DE PARADEROS
    // ==========================================
    @GetMapping("/paraderos")
    public String listarParaderos(@RequestParam(defaultValue = "0") int pagina, 
                                  @RequestParam(required = false) Long idBuscado,
                                  HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null || !"ADMIN".equals(session.getAttribute("rol"))) return "redirect:/login";

        try {
            //String cabeceraAuth = "Bearer " + token;
            List<ParaderoResponse> lista = new java.util.ArrayList<>();
            
            if (idBuscado != null) {
                try {
                    lista.add(paraderoClient.obtenerParaderoPorId(idBuscado));
                } catch(Exception e) { model.addAttribute("error", "No se encontró el Paradero con ID " + idBuscado); }
            } else {
                lista = paraderoClient.listarParaderos(pagina, 10);
            }
            
            for (ParaderoResponse p : lista) {
                if (p.getEstado() == null || p.getEstado().trim().isEmpty() || "0".equals(p.getEstado())) p.setEstado("ACTIVO");
                else if ("1".equals(p.getEstado())) p.setEstado("INACTIVO");
            }
            
            model.addAttribute("paraderos", lista);
            model.addAttribute("paginaActual", pagina);
            model.addAttribute("menuActivo", "paraderos");
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        } catch (Exception e) {
            model.addAttribute("error", "No se pudieron cargar los paraderos.");
        }
        return "admin/paraderos-lista";
    }

    @GetMapping("/paraderos/nuevo")
    public String formularioParadero(HttpSession session, Model model) {
        if (session.getAttribute("token") == null) return "redirect:/login";
        ParaderoRequest nuevo = new ParaderoRequest();
        nuevo.setEstado("ACTIVO");
        model.addAttribute("paraderoForm", nuevo);
        model.addAttribute("menuActivo", "paraderos");
        model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        return "admin/paraderos-form";
    }

    @PostMapping("/paraderos/guardar")
    public String guardarParadero(@ModelAttribute("paraderoForm") ParaderoRequest request, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";
        try {
            if (request.getEstado() == null || request.getEstado().isEmpty()) request.setEstado("ACTIVO");
            if (request.getReferencia() == null || request.getReferencia().isEmpty()) request.setReferencia("-");
            paraderoClient.registrarParadero(request); //("Bearer " + token, request);
            return "redirect:/admin/paraderos?exito=true";
        } catch (Exception e) {
            return "redirect:/admin/paraderos?error=true";
        }
    }

    @GetMapping("/paraderos/editar/{id}")
    public String editarParadero(@PathVariable("id") Long id, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null || !"ADMIN".equals(session.getAttribute("rol"))) return "redirect:/login";
        try {
            ParaderoResponse paradero = paraderoClient.obtenerParaderoPorId(id); //("Bearer " + token, id);
            ParaderoRequest form = new ParaderoRequest();
            form.setNombre(paradero.getNombre());
            form.setDireccion(paradero.getDireccion());
            form.setReferencia(paradero.getReferencia());
            form.setLatitud(paradero.getLatitud());
            form.setLongitud(paradero.getLongitud());
            
            String estadoForm = paradero.getEstado();
            if ("0".equals(estadoForm)) estadoForm = "INACTIVO";
            if ("2".equals(estadoForm) || "1".equals(estadoForm)) estadoForm = "ACTIVO"; 

            form.setEstado(estadoForm != null ? estadoForm : "ACTIVO");
            model.addAttribute("paraderoForm", form);
            model.addAttribute("esEdicion", true); 
            model.addAttribute("paraderoId", id);
            model.addAttribute("menuActivo", "paraderos");
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
            return "admin/paraderos-form";
        } catch (Exception e) {
            return "redirect:/admin/paraderos?error=true";
        }
    }

    @PostMapping("/paraderos/actualizar/{id}")
    public String actualizarParadero(@PathVariable("id") Long id, @ModelAttribute("paraderoForm") ParaderoRequest request, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";
        try {
            if (request.getReferencia() == null || request.getReferencia().isEmpty()) request.setReferencia("-");
            paraderoClient.actualizarParadero(id, request); //("Bearer " + token, id, request);
            return "redirect:/admin/paraderos?exito=true";
        } catch (Exception e) {
            return "redirect:/admin/paraderos?error=true";
        }
    }

    @GetMapping("/paraderos/eliminar/{id}")
    public String eliminarParadero(@PathVariable("id") Long id, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";
        try {
            paraderoClient.eliminarParadero("Bearer " + token, id);
            return "redirect:/admin/paraderos?eliminado=true";
        } catch (Exception e) {
            return "redirect:/admin/paraderos?errorEliminar=true";
        }
    }
    
    // ==========================================
    // MÓDULO DE ESTACIONES
    // ==========================================
    @GetMapping("/estaciones")
    public String listarEstaciones(@RequestParam(defaultValue = "0") int pagina, 
                                   @RequestParam(required = false) Long idBuscado,
                                   HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null || !"ADMIN".equals(session.getAttribute("rol"))) return "redirect:/login";

        try {
            //String cabeceraAuth = "Bearer " + token;
            List<EstacionResponse> lista = new java.util.ArrayList<>();
            
            if (idBuscado != null) {
                try {
                    lista.add(estacionClient.obtenerEstacionPorId(idBuscado));//(cabeceraAuth, idBuscado));
                } catch(Exception e) { model.addAttribute("error", "No se encontró la Estación con ID " + idBuscado); }
            } else {
                lista = estacionClient.listarEstaciones(pagina, 10);
            }
            
            List<UsuarioResponse> todosLosUsuarios = usuarioClient.listarUsuarios(0, 100);//(cabeceraAuth, 0, 100);
            List<UsuarioResponse> soloSupervisores = todosLosUsuarios.stream()
                    .filter(u -> u.getRol() != null && u.getRol().toUpperCase().contains("SUPERVISOR"))
                    .collect(java.util.stream.Collectors.toList());

            model.addAttribute("supervisores", soloSupervisores);
            model.addAttribute("estaciones", lista);
            model.addAttribute("paginaActual", pagina);
            model.addAttribute("menuActivo", "estaciones");
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        } catch (Exception e) {
            model.addAttribute("error", "No se pudieron cargar las estaciones.");
        }
        return "admin/estaciones-lista";
    }

    @GetMapping("/estaciones/nuevo")
    public String formularioEstacion(HttpSession session, Model model) {
        try {
            List<UsuarioResponse> soloSupervisores = usuarioClient.listarSupervisores(0, 100);
            model.addAttribute("supervisores", soloSupervisores);
        } catch (Exception ignored) {}

        model.addAttribute("estacionForm", new EstacionRequest());
        model.addAttribute("menuActivo", "estaciones");
        model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        return "admin/estaciones-form";
    }

    @PostMapping("/estaciones/guardar")
    public String guardarEstacion(@ModelAttribute("estacionForm") EstacionRequest request, HttpSession session) {
        try {
            estacionClient.registrarEstacion(request);
            return "redirect:/admin/estaciones?exito=true";
        } catch (Exception e) {
            return "redirect:/admin/estaciones?error=true";
        }
    }

    @GetMapping("/estaciones/editar/{id}")
    public String editarEstacion(@PathVariable("id") Long id, HttpSession session, Model model) {
        try {
            EstacionResponse estacion = estacionClient.obtenerEstacionPorId(id);
            
            EstacionRequest form = new EstacionRequest();
            form.setNombre(estacion.getNombre());
            form.setUbicacion(estacion.getUbicacion());
            form.setSupervisorId(estacion.getSupervisorId());

            List<UsuarioResponse> todos = usuarioClient.listarUsuarios(0, 100);
            List<UsuarioResponse> soloSupervisores = todos.stream()
                    .filter(u -> u.getRol() != null && u.getRol().toUpperCase().contains("SUPERVISOR"))
                    .collect(java.util.stream.Collectors.toList());

            model.addAttribute("supervisores", soloSupervisores);
            model.addAttribute("estacionForm", form);
            model.addAttribute("esEdicion", true); 
            model.addAttribute("estacionId", id);
            model.addAttribute("menuActivo", "estaciones");
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
            return "admin/estaciones-form";
        } catch (Exception e) {
            return "redirect:/admin/estaciones?error=true";
        }
    }

    @PostMapping("/estaciones/actualizar/{id}")
    public String actualizarEstacion(@PathVariable("id") Long id, @ModelAttribute("estacionForm") EstacionRequest request, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";
        try {
            estacionClient.actualizarEstacion(id, request); //("Bearer " + token, id, request);
            return "redirect:/admin/estaciones?exito=true";
        } catch (Exception e) {
            return "redirect:/admin/estaciones?error=true";
        }
    }

    @GetMapping("/estaciones/eliminar/{id}")
    public String eliminarEstacion(@PathVariable("id") Long id, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null || !"ADMIN".equals(session.getAttribute("rol"))) return "redirect:/login";
        try {
            estacionClient.eliminarEstacion(id); //("Bearer " + token, id);
            return "redirect:/admin/estaciones?eliminado=true";
        } catch (Exception e) {
            return "redirect:/admin/estaciones?errorEliminar=true";
        }
    }
    
    @GetMapping("/rutas")
    public String listarRutas(@RequestParam(defaultValue = "0") int pagina, HttpSession session, Model model) {
    	model.addAttribute("rutas", rutaClient.listar(pagina));
    	model.addAttribute("paginaActual", pagina);
        model.addAttribute("menuActivo", "rutas");
        model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
    	return "admin/rutas-lista";
    }
    
    @GetMapping("/rutas/nuevo")
    public String formularioRuta(HttpSession session, Model model) {
    	List<EstacionResponse> estaciones = estacionClient.listarEstaciones(0, 100);
    	List<ParaderoResponse> paraderos = paraderoClient.listarParaderos(0, 100);
    	
    	model.addAttribute("paraderos", paraderos);
    	model.addAttribute("estaciones", estaciones);
    	model.addAttribute("rutaForm", new RutaRequest());
        model.addAttribute("menuActivo", "rutas");
        model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        
    	return "admin/rutas-form";
    }
    
    @PostMapping("/rutas/guardar")
    public String guardarRuta(@ModelAttribute("rutaForm") RutaRequest request, HttpSession session) {
    	try {
            RutaResponse ruta = rutaClient.registrar(request);
            
            if(request.getParaderosId() != null && !request.getParaderosId().isEmpty()) {
	        	request.getParaderosId().forEach(e -> {
	 				RutaParaderoRequest rutaParadero = new RutaParaderoRequest();
	 				rutaParadero.setEstado("ACTIVO");
	 				rutaParadero.setRutaID(ruta.getId());
	 				rutaParadero.setParaderoID(e);
	 				rutaParaderoClient.registrar(rutaParadero);
     			});
            }
            
            return "redirect:/admin/rutas?exito=true";
        } catch (Exception e) {
        	e.printStackTrace();
            return "redirect:/admin/rutas?error=true";
        }
    }
    
    @GetMapping("/rutas/editar/{id}")
    public String editarRuta(@PathVariable Long id, HttpSession session, Model model) {
        try {
            RutaResponse ruta = rutaClient.obtenerPorId(id);
            
            RutaRequest form = new RutaRequest();
            form.setEstacionDestinoId(ruta.getEstacionDestinoId());
            form.setEstacionOrigenId(ruta.getEstacionOrigenId());
            form.setNombre(ruta.getNombre());
            form.setTipo(ruta.getTipo());
            form.setEstado(ruta.getEstado());
            form.setParaderosId(new ArrayList<>());
            
            rutaParaderoClient.listarPorRuta(id).forEach(rp -> {
				if (form.getParaderosId() == null) 
				form.getParaderosId().add(rp.getId());
			});
            
            List<EstacionResponse> estaciones = estacionClient.listarEstaciones(0, 100);
            List<ParaderoResponse> paraderos = paraderoClient.listarParaderos(0, 100);
            
            model.addAttribute("estaciones", estaciones);
            model.addAttribute("paraderos", paraderos);
            model.addAttribute("rutaForm", form);
            model.addAttribute("esEdicion", true); 
            model.addAttribute("rutaId", id);
            model.addAttribute("menuActivo", "rutas");
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
            return "admin/rutas-form";
        } catch (Exception e) {
            return "redirect:/admin/rutas?error=true";
        }
    }
    
    @PostMapping("/rutas/actualizar/{id}")
    public String actualizarRuta(@PathVariable Long id, @ModelAttribute("rutaForm") RutaRequest request, HttpSession session) {
        try {
            rutaClient.actualizar(id, request);
            return "redirect:/admin/rutas?exito=true";
        } catch (Exception e) {
            return "redirect:/admin/rutas?error=true";
        }
    }
    
    // ==========================================
    // MÓDULO DE REGISTROS
    // ==========================================
    
    @GetMapping("/registros")
    public String listarRegistros(@RequestParam(defaultValue = "0") int pagina, HttpSession session, Model model) {
        try {
            List<RegistroResponse> registros = registroClient.listar(pagina, 10);
            model.addAttribute("registros", registros);
            model.addAttribute("menuActivo", "registros");
            model.addAttribute("paginaActual", pagina);
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
            return "admin/registros-lista";
        } catch (Exception e) {
        	e.printStackTrace();
            return "redirect:/admin/dashboard?error=true";
        }
    }
}