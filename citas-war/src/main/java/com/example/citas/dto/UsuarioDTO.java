package com.example.citas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class UsuarioDTO {
    private String nombre;
    private String apellidos;
    private String dni;
    private List<String> roles;
}