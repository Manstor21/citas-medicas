package com.example.citas.service.interfaces.impl;

import com.example.citas.persistence.entity.Cita;
import com.example.citas.persistence.entity.JornadaMedico;
import com.example.citas.persistence.entity.Usuario;
import com.example.citas.persistence.entity.repository.CitaRepository;
import com.example.citas.persistence.entity.repository.JornadaMedicoRepository;
import com.example.citas.persistence.entity.repository.UsuarioRepository;
import com.example.citas.service.CitaService;
import com.example.citas.util.CitaException;
import com.example.citas.util.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final JornadaMedicoRepository jornadaMedicoRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaServiceImpl(CitaRepository citaRepository, JornadaMedicoRepository jornadaMedicoRepository, UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.jornadaMedicoRepository = jornadaMedicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<String> getHorasLibres(Long medicoId, String fechaStr) {
        LocalDate fecha = LocalDate.parse(fechaStr);
        Usuario medico = usuarioRepository.findById(medicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado"));

        JornadaMedico jornada = jornadaMedicoRepository.findByMedico(medico)
                .orElseThrow(() -> new CitaException("El médico no tiene jornada asignada"));

        List<Cita> citasOcupadas = citaRepository.findByMedicoIdAndFecha(medicoId, fecha);
        List<String> horasLibres = new ArrayList<>();

        LocalTime actual = jornada.getHoraInicio();

        // Mientras quepa un slot de 30 min dentro de la jornada
        while (actual.plusMinutes(30).isBefore(jornada.getHoraFin()) || actual.plusMinutes(30).equals(jornada.getHoraFin())) {
            LocalTime inicioSlot = actual;
            LocalTime finSlot = actual.plusMinutes(30);

            // Un slot está ocupado si se solapa con cualquier cita existente
            boolean estaOcupado = citasOcupadas.stream().anyMatch(c ->
                    (inicioSlot.isBefore(c.getHoraFin()) && finSlot.isAfter(c.getHoraInicio()))
            );

            if (!estaOcupado) {
                horasLibres.add(inicioSlot.toString());
            }
            actual = actual.plusMinutes(30);
        }
        return horasLibres;
    }

    @Override
    public Cita crearCita(Cita cita) {
        // Cargar médico completo
        Usuario medico = usuarioRepository.findById(cita.getMedico().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado"));

        // Cargar paciente completo
        Usuario paciente = usuarioRepository.findById(cita.getPaciente().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        JornadaMedico jornada = jornadaMedicoRepository.findByMedico(medico)
                .orElseThrow(() -> new CitaException("El médico no tiene una jornada configurada"));

        // Validar que la cita está dentro de la jornada laboral
        LocalTime inicioCita = cita.getHoraInicio();
        LocalTime finCita = cita.getHoraFin();

        if (inicioCita.isBefore(jornada.getHoraInicio()) || finCita.isAfter(jornada.getHoraFin())) {
            throw new CitaException("El horario solicitado está fuera de la jornada laboral del médico ("
                    + jornada.getHoraInicio() + " - " + jornada.getHoraFin() + ")");
        }

        // Verificar si el hueco específico está libre
        List<String> libres = getHorasLibres(cita.getMedico().getId(), cita.getFecha().toString());
        if (!libres.contains(inicioCita.toString())) {
            throw new CitaException("El horario seleccionado ya está ocupado.");
        }

        cita.setMedico(medico);
        cita.setPaciente(paciente);
        cita.setEstado("PENDIENTE");

        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitasPaciente(Long pacienteId) { return citaRepository.findByPacienteId(pacienteId); }

    @Override
    public Page<Cita> listarCitasMedicoPaginadas(Long medicoId, LocalDate fecha, Pageable pageable) {
        return citaRepository.findByMedicoIdAndFecha(medicoId, fecha, pageable);
    }

    @Override
    public Cita actualizarCita(Long id, Cita detalles) {
        Cita cita = citaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        if ("REALIZADA".equals(cita.getEstado())) throw new CitaException("No se puede editar una cita ya realizada");
        cita.setFecha(detalles.getFecha());
        cita.setHoraInicio(detalles.getHoraInicio());
        cita.setHoraFin(detalles.getHoraFin());
        return citaRepository.save(cita);
    }

    @Override
    public Cita marcarComoRealizada(Long id, Long medicoIdIgnorado) {
        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        Cita cita = citaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("La cita no existe"));
        if (!cita.getMedico().getEmail().equals(emailAutenticado)) {
            throw new CitaException("Acceso denegado: No puedes gestionar citas de otros médicos");
        }
        cita.setEstado("REALIZADA");
        return citaRepository.save(cita);
    }

    @Override
    public void eliminarCita(Long id) {
        if (!citaRepository.existsById(id)) throw new ResourceNotFoundException("La cita no existe");
        citaRepository.deleteById(id);
    }
}