package com.example.citas.util;

import com.example.citas.persistence.entity.Cita;
import com.example.citas.persistence.entity.JornadaMedico;
import com.example.citas.persistence.entity.Usuario;
import com.example.citas.persistence.entity.repository.CitaRepository;
import com.example.citas.persistence.entity.repository.JornadaMedicoRepository;
import com.example.citas.persistence.entity.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataSeed implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;
    private final JornadaMedicoRepository jornadaRepository;

    public DataSeed(UsuarioRepository usuarioRepository,
                    CitaRepository citaRepository,
                    JornadaMedicoRepository jornadaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.citaRepository = citaRepository;
        this.jornadaRepository = jornadaRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("SISTEMA: Usando datos existentes en la base de datos.");
    }
}
