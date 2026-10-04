package com.example.citas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cuerpo de la peticion de login: la contrasena viaja en el body, nunca en la query string. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    private String email;
    private String password;
}
