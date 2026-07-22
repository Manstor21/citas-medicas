package com.example.citas.persistence.entity.repository;

import com.example.citas.persistence.entity.JornadaMedico;
import com.example.citas.persistence.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JornadaMedicoRepository extends JpaRepository<JornadaMedico, Long> {
     Optional<JornadaMedico> findByMedico(Usuario medico);
}
