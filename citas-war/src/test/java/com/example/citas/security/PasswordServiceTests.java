package com.example.citas.security;

import com.example.citas.persistence.entity.Usuario;
import com.example.citas.persistence.entity.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PasswordServiceTests {

    private static final String RAW = "Clave#Segura2026";

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void clean() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("encode() genera un hash bcrypt y matches() lo verifica")
    void encodeAndMatchesBcryptHash() {
        String hash = passwordService.encode(RAW);

        assertThat(hash).startsWith("$2a$").isNotEqualTo(RAW);
        assertThat(passwordService.matches(RAW, hash)).isTrue();
        assertThat(passwordService.matches("otra-clave", hash)).isFalse();
        assertThat(passwordEncoder.matches(RAW, hash)).isTrue();
    }

    @Test
    @DisplayName("matches() acepta tambien la contrasena heredada en texto plano")
    void matchesLegacyPlainText() {
        assertThat(passwordService.matches(RAW, RAW)).isTrue();
        assertThat(passwordService.matches(RAW, "otra-clave")).isFalse();
        assertThat(passwordService.matches(RAW, null)).isFalse();
        assertThat(passwordService.matches(null, RAW)).isFalse();
    }

    @Test
    @DisplayName("needsRehash() detecta texto plano, hash vigente y hash con coste obsoleto")
    void needsRehash() {
        String currentHash = passwordService.encode(RAW);
        String weakHash = new BCryptPasswordEncoder(4).encode(RAW);

        assertThat(passwordService.needsRehash(RAW)).isTrue();
        assertThat(passwordService.needsRehash(currentHash)).isFalse();
        assertThat(passwordService.needsRehash(weakHash)).isTrue();
    }

    @Test
    @DisplayName("upgradeIfNeeded() re-hashea en bcrypt la contrasena en texto plano")
    void upgradeIfNeededMigratesPlainText() {
        Usuario usuario = persistir("migrado@citas.test", RAW);

        assertThat(passwordService.upgradeIfNeeded(usuario, RAW)).isTrue();

        String almacenada = usuarioRepository.findByEmail("migrado@citas.test").orElseThrow().getPassword();
        assertThat(almacenada).isNotEqualTo(RAW).startsWith("$2a$");
        assertThat(passwordService.matches(RAW, almacenada)).isTrue();
        assertThat(passwordService.needsRehash(almacenada)).isFalse();
    }

    @Test
    @DisplayName("upgradeIfNeeded() no toca la contrasena si la verificacion falla")
    void upgradeIfNeededIgnoresWrongPassword() {
        Usuario usuario = persistir("ajeno@citas.test", RAW);

        assertThat(passwordService.upgradeIfNeeded(usuario, "clave-incorrecta")).isFalse();

        assertThat(usuarioRepository.findByEmail("ajeno@citas.test").orElseThrow().getPassword()).isEqualTo(RAW);
    }

    @Test
    @DisplayName("upgradeIfNeeded() no rehace un hash bcrypt ya vigente")
    void upgradeIfNeededKeepsCurrentHash() {
        String hash = passwordService.encode(RAW);
        Usuario usuario = persistir("vigente@citas.test", hash);

        assertThat(passwordService.upgradeIfNeeded(usuario, RAW)).isFalse();

        assertThat(usuarioRepository.findByEmail("vigente@citas.test").orElseThrow().getPassword()).isEqualTo(hash);
    }

    private Usuario persistir(String email, String password) {
        Usuario usuario = new Usuario(null, "Ana", "Ruiz", "12345678Z", email, password, List.of("PACIENTE"));
        return usuarioRepository.saveAndFlush(usuario);
    }
}
