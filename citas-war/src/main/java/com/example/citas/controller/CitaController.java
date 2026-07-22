package com.example.citas.controller;

import com.example.citas.dto.CitaDTO;
import com.example.citas.persistence.entity.Cita;
import com.example.citas.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    // Crear cita
    @PostMapping
    public ResponseEntity<?> crearCita(@Valid @RequestBody Cita cita) {
        Cita nuevaCita = citaService.crearCita(cita);
        return ResponseEntity.ok(convertToDto(nuevaCita));
    }

    // Listar horas libres
    @GetMapping("/libres/{medicoId}")
    public ResponseEntity<List<String>> obtenerHorasLibres(
            @PathVariable Long medicoId,
            @RequestParam String fecha) {
        List<String> horasLibres = citaService.getHorasLibres(medicoId, fecha);
        return ResponseEntity.ok(horasLibres);
    }

    // Listar citas del paciente
    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasRole('PACIENTE') or hasRole('MEDICO')")
    public ResponseEntity<List<CitaDTO>> citasPaciente(@PathVariable Long pacienteId) {
        List<Cita> citas = citaService.listarCitasPaciente(pacienteId);
        List<CitaDTO> citasDTO = citas.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(citasDTO);
    }

    // Listar citas del médico (paginado)
    @GetMapping("/medico/{id}")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<Page<CitaDTO>> citasMedico(
            @PathVariable Long id,
            @RequestParam String fecha,
            @RequestParam(defaultValue = "0") int page) {
        Page<Cita> pagina = citaService.listarCitasMedicoPaginadas(id, LocalDate.parse(fecha), PageRequest.of(page, 5));
        return ResponseEntity.ok(pagina.map(this::convertToDto));
    }

    // Editar cita
    @PutMapping("/{id}")
    public ResponseEntity<?> editarCita(
            @PathVariable Long id,
            @Valid @RequestBody Cita detalles) {
        Cita citaActualizada = citaService.actualizarCita(id, detalles);
        return ResponseEntity.ok(convertToDto(citaActualizada));
    }

    // Marcar cita como realizada
    @PatchMapping("/{id}/realizada")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<?> marcarRealizada(@PathVariable Long id) {
        // el service identifica al médico por el token jwt
        Cita actualizada = citaService.marcarComoRealizada(id, null);
        return ResponseEntity.ok(convertToDto(actualizada));
    }

    // Eliminar cita
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCita(@PathVariable Long id) {
        citaService.eliminarCita(id);
        return ResponseEntity.ok("cita eliminada correctamente");
    }

    // Conversor a dto
    private CitaDTO convertToDto(Cita cita) {
        CitaDTO dto = new CitaDTO();
        dto.setId(cita.getId());
        dto.setFecha(cita.getFecha());
        dto.setHoraInicio(cita.getHoraInicio());
        dto.setHoraFin(cita.getHoraFin());
        dto.setEstado(cita.getEstado());
        dto.setNombreMedico(cita.getMedico().getNombre() + " " + cita.getMedico().getApellidos());
        dto.setNombrePaciente(cita.getPaciente().getNombre() + " " + cita.getPaciente().getApellidos());
        return dto;
    }
}