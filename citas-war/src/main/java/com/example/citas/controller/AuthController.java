package com.example.citas.controller;

import com.example.citas.dto.LoginResponseDTO;
import com.example.citas.dto.UsuarioDTO;
import com.example.citas.persistence.entity.Usuario;
import com.example.citas.persistence.entity.repository.UsuarioRepository;
import com.example.citas.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioRepository usuarioRepository, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        // Buscamos usuario y verificamos contraseña
        return usuarioRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password))
                .map(u -> {
                    // Generar Token
                    String token = jwtUtil.generateToken(u.getEmail());

                    // Crear DTO de Usuario
                    UsuarioDTO userDTO = new UsuarioDTO(
                            u.getNombre(),
                            u.getApellidos(),
                            u.getDni(),
                            u.getRoles()
                    );

                    // Devolver respuesta encapsulada en DTO
                    return ResponseEntity.ok(new LoginResponseDTO(userDTO, token));
                })
                .orElse(ResponseEntity.status(401).build());
    }
}