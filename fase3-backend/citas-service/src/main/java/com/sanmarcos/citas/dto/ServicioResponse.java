package com.sanmarcos.citas.dto;

public record ServicioResponse(
        Long id,
        String codigo,
        String categoria,
        String nombre,
        String especie,
        Integer duracionMin,
        Integer precio,
        String observaciones,
        boolean agendable) {
}
