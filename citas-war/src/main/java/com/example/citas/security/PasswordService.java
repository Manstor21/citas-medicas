package com.example.citas.security;

import com.example.citas.persistence.entity.Usuario;
import com.example.citas.persistence.entity.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Verificacion y migracion de contrasenas.
 *
 * Las bases de datos ya instaladas guardan la contrasena en texto plano, por lo que
 * el login acepta ambos formatos y migra de forma transparente a bcrypt en el
 * primer inicio de sesion correcto de cada usuario.
 */
@Service
public class PasswordService {

    private static final Logger log = LoggerFactory.getLogger(PasswordService.class);

    private static final String BCRYPT_PREFIX_2A = "$2a$";
    private static final String BCRYPT_PREFIX_2B = "$2b$";
    private static final String BCRYPT_PREFIX_2Y = "$2y$";

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;

    public PasswordService(PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
    }

    /** Genera un hash bcrypt de la contrasena en claro. */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Compara la contrasena recibida con la almacenada.
     *
     * Si lo almacenado ya es un hash bcrypt delega en BCryptPasswordEncoder.
     * Si sigue siendo texto plano (base de datos heredada) usa comparacion en tiempo
     * constante para no filtrar informacion por analisis temporal.
     */
    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return constantTimeEquals(rawPassword, storedPassword);
    }

    /**
     * Indica si lo almacenado debe rehacerse: texto plano heredado o hash bcrypt con
     * un coste inferior al configurado (permite subir el coste en el futuro).
     */
    public boolean needsRehash(String storedPassword) {
        if (storedPassword == null || !isBcryptHash(storedPassword)) {
            return true;
        }
        return passwordEncoder.upgradeEncoding(storedPassword);
    }

    /**
     * Migra a bcrypt la contrasena almacenada en texto plano tras un login correcto.
     * Nunca re-hashea si la contrasena no verifica: un login fallido no debe alterar
     * la contrasena del usuario. Solo registra el email, nunca la contrasena ni su hash.
     *
     * @return true si se ha persistido un nuevo hash
     */
    @Transactional
    public boolean upgradeIfNeeded(Usuario usuario, String rawPassword) {
        String storedPassword = usuario.getPassword();
        if (!needsRehash(storedPassword)) {
            return false;
        }
        if (!matches(rawPassword, storedPassword)) {
            return false;
        }

        boolean legacyPlainText = !isBcryptHash(storedPassword);
        usuario.setPassword(encode(rawPassword));
        usuarioRepository.save(usuario);

        log.info("Contrasena del usuario {} actualizada a bcrypt ({}); no se registra su valor",
                usuario.getEmail(),
                legacyPlainText ? "migrada desde texto plano" : "coste de hash renovado");
        return true;
    }

    private static boolean isBcryptHash(String value) {
        return value.startsWith(BCRYPT_PREFIX_2A)
                || value.startsWith(BCRYPT_PREFIX_2B)
                || value.startsWith(BCRYPT_PREFIX_2Y);
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
