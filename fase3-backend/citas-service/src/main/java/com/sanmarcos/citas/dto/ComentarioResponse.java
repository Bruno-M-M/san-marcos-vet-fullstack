package com.sanmarcos.citas.dto;

import java.time.LocalDateTime;

public record ComentarioResponse(
        Long id,
        String nombre,
        String correo,
        String comentario,
        LocalDateTime fecha) {
}
