package com.example.citas.service;

import com.example.citas.persistence.entity.Usuario;
import java.util.Optional;

public interface UsuarioService {
    Optional<Usuario> login(String email, String password);
}
