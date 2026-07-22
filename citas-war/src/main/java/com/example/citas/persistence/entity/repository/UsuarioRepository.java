package com.example.citas.persistence.entity.repository;

import com.example.citas.persistence.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Método para buscar un usuario por email
    Optional<Usuario> findByEmail(String email);

    // Método para buscar un usuario por DNI
    Optional<Usuario> findByDni(String dni);
}
