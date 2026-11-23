package com.sanmarcos.citas.controller;

import com.sanmarcos.citas.dto.ComentarioRequest;
import com.sanmarcos.citas.dto.ComentarioResponse;
import com.sanmarcos.citas.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comentarios")
@Tag(name = "Comentarios", description = "Publicar comentarios y moderarlos")
public class ComentarioController {

    private final ComentarioService servicio;

    public ComentarioController(ComentarioService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Publica un comentario (público, sin token)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResponse publicar(@Valid @RequestBody ComentarioRequest request) {
        return servicio.publicar(request);
    }

    @Operation(summary = "Lista los comentarios, del más nuevo al más antiguo (administrador)")
    @GetMapping
    public List<ComentarioResponse> listar() {
        return servicio.listar();
    }

    @Operation(summary = "Elimina un comentario (administrador, 404 si no existe)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
