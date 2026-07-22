package com.example.citas.service.interfaces.impl;

import com.example.citas.persistence.entity.Usuario;
import com.example.citas.persistence.entity.repository.UsuarioRepository;
import com.example.citas.service.UsuarioService;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<Usuario> login(String email, String password) {
        // Busca usuario por email y compara contraseña
        return usuarioRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password));
    }
}
