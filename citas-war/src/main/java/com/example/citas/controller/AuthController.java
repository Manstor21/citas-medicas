package com.example.citas.controller;

import com.example.citas.dto.LoginRequestDTO;
import com.example.citas.dto.LoginResponseDTO;
import com.example.citas.dto.UsuarioDTO;
import com.example.citas.security.JwtUtil;
import com.example.citas.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Login con cuerpo JSON. La contrasena se recibe en el body (no en la query string)
     * y la verifica UsuarioService, que ademas migra a bcrypt las contrasenas heredadas
     * en texto plano antes de devolver el token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody(required = false) LoginRequestDTO request) {
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Buscamos usuario y verificamos contraseña (bcrypt o migración desde texto plano)
        return usuarioService.login(request.getEmail(), request.getPassword())
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
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
