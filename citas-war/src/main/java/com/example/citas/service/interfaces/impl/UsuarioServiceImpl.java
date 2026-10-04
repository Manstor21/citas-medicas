package com.example.citas.service.interfaces.impl;

import com.example.citas.persistence.entity.Usuario;
import com.example.citas.persistence.entity.repository.UsuarioRepository;
import com.example.citas.security.PasswordService;
import com.example.citas.service.UsuarioService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordService passwordService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordService = passwordService;
    }

    /**
     * Unica ruta de verificacion de credenciales: busca por email, comprueba la
     * contrasena con PasswordService (bcrypt o texto plano heredado) y, si el login es
     * correcto, persiste el hash bcrypt antes de devolver el usuario.
     */
    @Override
    @Transactional
    public Optional<Usuario> login(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmail(email)
                .filter(u -> passwordService.matches(password, u.getPassword()))
                .map(u -> {
                    passwordService.upgradeIfNeeded(u, password);
                    return u;
                });
    }
}
