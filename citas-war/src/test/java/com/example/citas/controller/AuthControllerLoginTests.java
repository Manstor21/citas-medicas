package com.example.citas.controller;

import com.example.citas.persistence.entity.Usuario;
import com.example.citas.persistence.entity.repository.UsuarioRepository;
import com.example.citas.security.PasswordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerLoginTests {

    private static final String PASSWORD = "Clave#Segura2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordService passwordService;

    @BeforeEach
    void clean() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Login con hash bcrypt almacenado devuelve 200 y token")
    void loginOkConHashBcrypt() throws Exception {
        persistir("bcrypt@citas.test", passwordService.encode(PASSWORD));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("bcrypt@citas.test", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.nombre").value("Ana"))
                .andExpect(jsonPath("$.usuario.roles[0]").value("PACIENTE"));
    }

    @Test
    @DisplayName("Login con contrasea incorrecta devuelve 401")
    void loginKoConContrasenaIncorrecta() throws Exception {
        persistir("bcrypt@citas.test", passwordService.encode(PASSWORD));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("bcrypt@citas.test", "clave-incorrecta")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login con email desconocido devuelve 401")
    void loginKoConEmailDesconocido() throws Exception {
        persistir("bcrypt@citas.test", passwordService.encode(PASSWORD));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("nadie@citas.test", PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login con cuerpo vacio devuelve 401 y no filtra detalles")
    void loginKoConCuerpoVacio() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("   ", "")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login con contrasea heredada en texto plano devuelve 200 y migra el valor a bcrypt")
    void loginMigraContraseaEnTextoPlano() throws Exception {
        persistir("legacy@citas.test", PASSWORD);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("legacy@citas.test", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        String almacenada = usuarioRepository.findByEmail("legacy@citas.test").orElseThrow().getPassword();
        assertThat(almacenada).isNotEqualTo(PASSWORD).startsWith("$2a$");
        assertThat(passwordService.matches(PASSWORD, almacenada)).isTrue();
    }

    @Test
    @DisplayName("Un login fallido con contrasea en texto plano no modifica lo almacenado")
    void loginFallidoNoMigra() throws Exception {
        persistir("legacy@citas.test", PASSWORD);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("legacy@citas.test", "clave-incorrecta")))
                .andExpect(status().isUnauthorized());

        assertThat(usuarioRepository.findByEmail("legacy@citas.test").orElseThrow().getPassword())
                .isEqualTo(PASSWORD);
    }

    private String cuerpo(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(Map.of("email", email, "password", password));
    }

    private Usuario persistir(String email, String password) {
        Usuario usuario = new Usuario(null, "Ana", "Ruiz", "12345678Z", email, password, List.of("PACIENTE"));
        return usuarioRepository.saveAndFlush(usuario);
    }
}
