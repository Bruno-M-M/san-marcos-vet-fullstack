package com.sanmarcos.citas.controller;

import com.sanmarcos.citas.dto.ServicioResponse;
import com.sanmarcos.citas.service.ServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/servicios")
@Tag(name = "Servicios", description = "Catálogo de servicios de la veterinaria (público)")
public class ServicioController {

    private final ServicioService servicio;

    public ServicioController(ServicioService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Lista los servicios activos, opcionalmente de una categoría (público)")
    @GetMapping
    public List<ServicioResponse> listar(@RequestParam(required = false) String categoria) {
        return servicio.listar(categoria);
    }

    @Operation(summary = "Obtiene un servicio por id (público, 404 si no existe)")
    @GetMapping("/{id}")
    public ServicioResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }
}
