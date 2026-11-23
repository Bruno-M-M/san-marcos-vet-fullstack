package com.sanmarcos.citas.service;

import com.sanmarcos.citas.dto.ComentarioRequest;
import com.sanmarcos.citas.dto.ComentarioResponse;
import com.sanmarcos.citas.exception.RecursoNoEncontradoException;
import com.sanmarcos.citas.model.Comentario;
import com.sanmarcos.citas.repository.ComentarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarios;

    public ComentarioService(ComentarioRepository comentarios) {
        this.comentarios = comentarios;
    }

    private ComentarioResponse respuesta(Comentario c) {
        return new ComentarioResponse(c.getId(), c.getNombre(), c.getCorreo(), c.getTexto(), c.getFecha());
    }

    @Transactional
    public ComentarioResponse publicar(ComentarioRequest nuevo) {
        Comentario comentario = new Comentario();
        comentario.setNombre(nuevo.nombre().trim());
        comentario.setCorreo(nuevo.correo().trim().toLowerCase());
        comentario.setTexto(nuevo.comentario().trim());
        comentario.setFecha(LocalDateTime.now());
        return respuesta(comentarios.save(comentario));
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listar() {
        return comentarios.findAllByOrderByFechaDesc()
                .stream()
                .map(this::respuesta)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        Comentario comentario = comentarios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el comentario con id " + id + "."));
        comentarios.delete(comentario);
    }
}
