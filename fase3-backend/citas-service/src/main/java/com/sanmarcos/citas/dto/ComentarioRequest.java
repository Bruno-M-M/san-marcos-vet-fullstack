package com.sanmarcos.citas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre admite hasta 100 caracteres.")
        String nombre,

        @NotBlank(message = "El correo es obligatorio.")
        @Size(max = 120, message = "El correo admite hasta 120 caracteres.")
        @Pattern(regexp = "(?i)^[^@\\s]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$",
                message = "El correo debe terminar en @duoc.cl, @profesor.duoc.cl o @gmail.com.")
        String correo,

        @NotBlank(message = "El comentario es obligatorio.")
        @Size(max = 500, message = "El comentario admite hasta 500 caracteres.")
        String comentario) {
}
