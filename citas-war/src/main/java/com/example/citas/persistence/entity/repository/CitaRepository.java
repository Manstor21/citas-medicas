package com.example.citas.persistence.entity.repository;

import com.example.citas.persistence.entity.Cita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Listar citas de un paciente
    List<Cita> findByPacienteId(Long pacienteId);

    // Listar citas de un médico con paginación (5 por página)
    Page<Cita> findByMedicoIdAndFecha(Long medicoId, LocalDate fecha, Pageable pageable);

    // Validaciones internas de solapamiento
    List<Cita> findByMedicoIdAndFecha(Long medicoId, LocalDate fecha);
}