package com.sanmarcos.citas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sanmarcos.citas.dto.ComentarioRequest;
import com.sanmarcos.citas.dto.ComentarioResponse;
import com.sanmarcos.citas.exception.RecursoNoEncontradoException;
import com.sanmarcos.citas.model.Comentario;
import com.sanmarcos.citas.repository.ComentarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarios;

    @InjectMocks
    private ComentarioService servicio;

    private Comentario guardado(Long id, String nombre) {
        Comentario c = new Comentario();
        c.setId(id);
        c.setNombre(nombre);
        c.setCorreo("ana@gmail.com");
        c.setTexto("Excelente atención");
        c.setFecha(LocalDateTime.of(2026, 11, 20, 10, 30));
        return c;
    }

    @Test
    void publicar_limpiaEspaciosYPasaElCorreoAMinusculas() {
        when(comentarios.save(any(Comentario.class))).thenAnswer(inv -> {
            Comentario c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });

        ComentarioResponse respuesta = servicio.publicar(
                new ComentarioRequest("  Ana  ", "  ANA@Gmail.com ", "  Muy buena atención  "));

        ArgumentCaptor<Comentario> captura = ArgumentCaptor.forClass(Comentario.class);
        verify(comentarios).save(captura.capture());
        assertEquals("Ana", captura.getValue().getNombre());
        assertEquals("ana@gmail.com", captura.getValue().getCorreo());
        assertEquals("Muy buena atención", captura.getValue().getTexto());
        assertNotNull(captura.getValue().getFecha());
        assertEquals(5L, respuesta.id());
        assertEquals("Muy buena atención", respuesta.comentario());
    }

    @Test
    void listar_devuelveLosComentariosEnElOrdenDelRepositorio() {
        when(comentarios.findAllByOrderByFechaDesc())
                .thenReturn(List.of(guardado(2L, "Luis"), guardado(1L, "Ana")));

        List<ComentarioResponse> lista = servicio.listar();

        assertEquals(2, lista.size());
        assertEquals("Luis", lista.get(0).nombre());
        assertEquals("Ana", lista.get(1).nombre());
    }

    @Test
    void eliminar_conIdExistente_loBorra() {
        Comentario existente = guardado(1L, "Ana");
        when(comentarios.findById(1L)).thenReturn(Optional.of(existente));

        servicio.eliminar(1L);

        verify(comentarios).delete(existente);
    }

    @Test
    void eliminar_conIdInexistente_lanzaNoEncontradoYNoBorraNada() {
        when(comentarios.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.eliminar(99L));

        assertEquals("No existe el comentario con id 99.", ex.getMessage());
        verify(comentarios, never()).delete(any());
    }
}
