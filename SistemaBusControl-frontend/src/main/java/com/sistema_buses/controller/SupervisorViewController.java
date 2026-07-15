package com.sistema_buses.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sistema_buses.client.AsignacionClient;
import com.sistema_buses.client.IncidenciaClient;
import com.sistema_buses.client.RecorridoClient;
import com.sistema_buses.client.RutaClient;
import com.sistema_buses.client.UsuarioClient;
import com.sistema_buses.client.VehiculoClient;
import com.sistema_buses.client.VehiculoMantenimientoClient;
import com.sistema_buses.client.VehiculoSeguroClient;
import com.sistema_buses.dto.asignacion.AsignacionRequest;
import com.sistema_buses.dto.asignacion.AsignacionResponse;
import com.sistema_buses.dto.incidencia.IncidenciaRequest;
import com.sistema_buses.dto.incidencia.IncidenciaResponse;
import com.sistema_buses.dto.vehiculo.VehiculoMantenimientoRequest;
import com.sistema_buses.dto.vehiculo.VehiculoMantenimientoResponse;
import com.sistema_buses.dto.vehiculo.VehiculoSeguroRequest;
import com.sistema_buses.dto.vehiculo.VehiculoSeguroResponse;

import feign.FeignException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/supervisor")
public class SupervisorViewController {
	
	private final VehiculoClient vehiculoClient;
	private final VehiculoSeguroClient vehiculoSeguroClient;
	private final AsignacionClient asignacionClient;
	private final IncidenciaClient incidenciaClient;
	private final UsuarioClient usuarioClient;
	private final RutaClient rutaClient;
	private final VehiculoMantenimientoClient vehiculoMantenimientoClient;
	private final RecorridoClient recorridoClient;

	public SupervisorViewController(VehiculoClient vehiculoClient, VehiculoSeguroClient vehiculoSeguroClient,
			AsignacionClient asignacionClient, IncidenciaClient incidenciaClient, UsuarioClient usuarioClient,
			RutaClient rutaClient, VehiculoMantenimientoClient vehiculoMantenimientoClient,
			RecorridoClient recorridoClient) {
		this.vehiculoClient = vehiculoClient;
		this.vehiculoSeguroClient = vehiculoSeguroClient;
		this.asignacionClient = asignacionClient;
		this.incidenciaClient = incidenciaClient;
		this.usuarioClient = usuarioClient;
		this.rutaClient = rutaClient;
		this.vehiculoMantenimientoClient = vehiculoMantenimientoClient;
		this.recorridoClient = recorridoClient;
	}
	
	@GetMapping("/dashboard")
	public String dashboard(HttpSession session, Model model) {
		return "dashboards/supervisor-view";
	}
	
	// MANTENIMIENTO

	// LISTADO
	@GetMapping("/vehiculos-mantenimiento")
	public String listarMantenimientos(HttpSession session, Model model) {
		model.addAttribute("mantenimientos", vehiculoMantenimientoClient.listar(0));
		model.addAttribute("menuActivo", "vehiculos-mantenimiento");

		return "supervisor/vehiculos-mantenimiento-lista";
	}

	// FORMULARIO
	@GetMapping("/vehiculos-mantenimiento/form")
	public String formMantenimiento(@RequestParam(required = false) Long id, HttpSession session, Model model) {
		VehiculoMantenimientoRequest form = new VehiculoMantenimientoRequest();
		form.setEstado("ACTIVO");

		if (id != null) {
			VehiculoMantenimientoResponse mantenimiento = vehiculoMantenimientoClient.obtenerPorId(id);
			form.setVehiculoID(mantenimiento.getVehiculoID());
			form.setFechaInicio(mantenimiento.getFechaInicio());
			form.setFechaFin(mantenimiento.getFechaFin());
			form.setDescripcion(mantenimiento.getDescripcion());
			form.setEstado(mantenimiento.getEstado());
		}

		model.addAttribute("mantenimientoForm", form);
		model.addAttribute("mantenimientoId", id);
		model.addAttribute("modo", id == null ? "crear" : "editar");
		model.addAttribute("vehiculos", vehiculoClient.listarVehiculos(0));

		return "supervisor/vehiculos-mantenimiento-form";
	}

	// GUARDAR

	@PostMapping("/vehiculos-mantenimiento/guardar")
	public String guardarMantenimiento(
			@RequestParam(required = false) Long id,
			@ModelAttribute VehiculoMantenimientoRequest request, 
			HttpSession session, 
			RedirectAttributes redirect) {
		try {
			if (id == null) {
				vehiculoMantenimientoClient.registrar(request);
			} else {
				vehiculoMantenimientoClient.actualizar(id, request);
			}
			return "redirect:/supervisor/vehiculos-mantenimiento";
		} catch (Exception e) {
			redirect.addFlashAttribute("error", e.getMessage());
			return "redirect:/supervisor/vehiculos-mantenimiento/form";
		}
	}

	@GetMapping("/vehiculos-mantenimiento/eliminar/{id}")
	public String eliminarMantenimiento(@PathVariable Long id, HttpSession session) {
		vehiculoMantenimientoClient.eliminar(id);
		return "redirect:/supervisor/vehiculos-mantenimiento";
	}

	// ==================================================================
	// SEGUROS
	@GetMapping("/asignaciones")
	public String asignaciones(HttpSession session, Model model) {
		model.addAttribute("asignaciones", asignacionClient.listarAsignaciones(0));
		model.addAttribute("menuActivo", "asignaciones");

		return "supervisor/asignaciones-lista";
	}

	@PostMapping("/asignaciones/guardar")
	public String guardarAsignacion(@RequestParam(required = false) Long id,
			@ModelAttribute AsignacionRequest asignacionForm, HttpSession session) {
		if (id == null) {
			asignacionClient.registrarAsignacion(asignacionForm);
		} else {
			asignacionClient.actualizarAsignacion(id, asignacionForm);
		}

		return "redirect:/supervisor/asignaciones";
	}

	// =======================
	// FORM CREAR / EDITAR ASIGNACION
	// =======================
	@GetMapping("/asignaciones/form")
	public String formAsignacion(@RequestParam(required = false) Long id, HttpSession session, Model model) {
		AsignacionRequest form = new AsignacionRequest();

		try {
			if (id != null) {
				AsignacionResponse a = asignacionClient.obtenerAsignacionPorId(id);

				form.setConductorID(a.getConductorID());
				form.setRutaID(a.getRutaID());
				form.setVehiculoID(a.getVehiculoID());
				form.setFecha(a.getFecha());
				form.setHoraInicio(a.getHoraInicio());
				form.setHoraFin(a.getHoraFin());
				form.setEstado(a.getEstado());
			}

		} catch (Exception e) {
			model.addAttribute("error", "No se pudo cargar la asignación (ID inválido o no existe)");
		}

		model.addAttribute("asignacionForm", form);
		model.addAttribute("asignacionId", id);
		model.addAttribute("modo", (id == null) ? "crear" : "editar");
		model.addAttribute("conductores", usuarioClient.listarConductores(0, 10));
		model.addAttribute("rutas", rutaClient.listar(0));
		model.addAttribute("vehiculos", vehiculoClient.listarVehiculos(0));

		return "supervisor/asignaciones-form";
	}

	@PostMapping("/asignaciones/eliminar/{id}")
	public String eliminarAsignacion(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
		try {
			asignacionClient.eliminarAsignacion(id);
		}catch(Exception e) {
			redirect.addFlashAttribute("error", e.getMessage());
		}
		
		return "redirect:/supervisor/asignaciones";
	}

	@GetMapping("/incidencias")
	public String incidencias(HttpSession session, Model model) {
		try {
			model.addAttribute("incidencias", incidenciaClient.listar(0));
		} catch (Exception e) {
			model.addAttribute("incidencias", java.util.List.of());
			model.addAttribute("error", "Error cargando incidencias: " + e.getMessage());
		}

		model.addAttribute("menuActivo", "incidencias");
		return "supervisor/incidencias-lista";
	}

	@GetMapping("/incidencias/form")
	public String formIncidencia(@RequestParam(required = false) Long id, HttpSession session, Model model) {
		IncidenciaRequest form = new IncidenciaRequest();

		if (id != null) {
			IncidenciaResponse inc = incidenciaClient.obtenerPorId(id);

			form.setDescripcion(inc.getDescripcion());
			form.setFechaHoraSuceso(inc.getFechaHoraSuceso());
			form.setRecorridoID(inc.getRecorridoID());
			form.setUsuarioID(inc.getUsuarioID());
			model.addAttribute("usuarioNombre", inc.getUsuarioNombre());
		}

		model.addAttribute("incidenciaForm", form);
		model.addAttribute("modo", id == null ? "crear" : "editar");
		model.addAttribute("recorridos", recorridoClient.listar(0));

		return "supervisor/incidencias-form";
	}

	@PostMapping("/incidencias/eliminar/{id}")
	public String eliminarIncidencia(@PathVariable Long id, HttpSession session) {
		incidenciaClient.eliminarIncidencia(id);
		return "redirect:/supervisor/incidencias";
	}

	// GUARDAR INCIDENCIAS;
	@PostMapping("/incidencias/guardar")
	public String guardarIncidencia(@RequestParam(required = false) Long id,
			@ModelAttribute IncidenciaRequest request, HttpSession session, Model model, RedirectAttributes redirect) {
		try {
			if (id == null) {
				incidenciaClient.registrar(request);
			} else {
				incidenciaClient.actualizarIncidencia(id, request);
			}

			return "redirect:/supervisor/incidencias";
		}catch(Exception ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
			return id == null ? "redirect:/supervisor/incidencias/form" :
					"redirect:/supervisor/incidencias/form?id="+id;
		}
	}
	// ==================================================================
	// SEGUROS

	// =======================
	// LISTADO
	// =======================
	@GetMapping("/vehiculos-seguro")
	public String listarSeguros(HttpSession session, Model model) {
		model.addAttribute("seguros", vehiculoSeguroClient.listarSeguros(0));
		model.addAttribute("menuActivo", "vehiculos-seguro");

		return "supervisor/vehiculos-seguro-lista";
	}

	@GetMapping("/vehiculos-seguro/form")
	public String formSeguro(@RequestParam(required = false) Long id, HttpSession session, Model model) {
		VehiculoSeguroRequest form = new VehiculoSeguroRequest();

		if (id != null) {
			VehiculoSeguroResponse seguro = vehiculoSeguroClient.obtenerSeguroPorId(id);

			form.setNumero(seguro.getNumero());
			form.setFechaVencimiento(seguro.getFechaVencimiento());
		}

		model.addAttribute("seguroForm", form);
		model.addAttribute("seguroId", id);
		model.addAttribute("modo", (id == null) ? "crear" : "editar");

		return "supervisor/vehiculos-seguro-form";
	}

	// =======================
	// FORM EDITAR
	// =======================
	@GetMapping("/vehiculos-seguro/form/{id}")
	public String editarSeguro(@PathVariable Long id, HttpSession session, Model model) {
		VehiculoSeguroRequest form = new VehiculoSeguroRequest();

		try {
			VehiculoSeguroResponse seguro = vehiculoSeguroClient.obtenerSeguroPorId(id);

			form.setNumero(seguro.getNumero());
			form.setFechaVencimiento(seguro.getFechaVencimiento());

		} catch (Exception e) {}

		model.addAttribute("seguroForm", form);
		model.addAttribute("seguroId", id);
		model.addAttribute("modo", "editar");

		return "supervisor/vehiculos-seguro-form";
	}

	// =======================
	// GUARDAR
	// =======================
	@PostMapping("/vehiculos-seguro/guardar")
	public String guardarSeguro(@RequestParam(required = false) Long id,
			@ModelAttribute VehiculoSeguroRequest request, HttpSession session, Model model) {

		try {
			if (id == null) {
				vehiculoSeguroClient.registrarSeguro(request);
			} else {
				vehiculoSeguroClient.actualizarSeguro(id, request);
			}

			return "redirect:/supervisor/vehiculos-seguro";

		} catch (FeignException.Conflict e) {

			model.addAttribute("error", "Ya existe un seguro con ese número.");
			model.addAttribute("seguroForm", request);
			model.addAttribute("seguroId", id);
			model.addAttribute("modo", (id == null) ? "crear" : "editar");

			return "supervisor/vehiculos-seguro-form";
		}
	}
}
