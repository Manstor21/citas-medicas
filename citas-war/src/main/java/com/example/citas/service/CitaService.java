package com.example.citas.service;

import com.example.citas.persistence.entity.Cita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface CitaService {

    /**
     * Lista todas las citas asociadas a un paciente específico.
     */
    List<Cita> listarCitasPaciente(Long pacienteId);

    /**
     * Lista las citas de un médico para una fecha concreta con soporte de paginación.
     */
    Page<Cita> listarCitasMedicoPaginadas(Long medicoId, LocalDate fecha, Pageable pageable);

    /**
     * Calcula los intervalos de 30 minutos disponibles para un médico en una fecha dada.
     */
    List<String> getHorasLibres(Long medicoId, String fecha);

    /**
     * Registra una nueva cita verificando disponibilidad.
     */
    Cita crearCita(Cita cita);

    /**
     * Actualiza los datos de una cita existente (si no está ya realizada).
     */
    Cita actualizarCita(Long id, Cita detalles);

    /**
     * Cambia el estado de la cita a REALIZADA (Solo accesible para el médico asignado).
     */
    Cita marcarComoRealizada(Long id, Long medicoId);

    /**
     * Elimina una cita del sistema por su ID.
     */
    void eliminarCita(Long id);
}