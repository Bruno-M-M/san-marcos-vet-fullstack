package com.sanmarcos.citas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sanmarcos.citas.dto.ServicioResponse;
import com.sanmarcos.citas.exception.RecursoNoEncontradoException;
import com.sanmarcos.citas.model.Servicio;
import com.sanmarcos.citas.repository.ServicioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ServicioServiceTest {

    @Mock
    private ServicioRepository servicios;

    @InjectMocks
    private ServicioService servicio;

    private Servicio unServicio(Long id, String codigo, String categoria, boolean agendable) {
        Servicio s = new Servicio();
        s.setId(id);
        s.setCodigo(codigo);
        s.setCategoria(categoria);
        s.setNombre("Servicio " + codigo);
        s.setEspecie("Perro / Gato");
        s.setDuracionMin(30);
        s.setPrecio(15000);
        s.setAgendable(agendable);
        return s;
    }

    @Test
    void listar_sinCategoria_devuelveTodosLosActivos() {
        when(servicios.findByActivoTrueOrderByCategoriaAscNombreAsc())
                .thenReturn(List.of(unServicio(1L, "SV001", "Consultas", true),
                        unServicio(2L, "VA001", "Vacunación", true)));

        List<ServicioResponse> lista = servicio.listar(null);

        assertEquals(2, lista.size());
        assertEquals("SV001", lista.get(0).codigo());
        assertEquals("Vacunación", lista.get(1).categoria());
    }

    @Test
    void listar_conCategoriaEnBlanco_tambienDevuelveTodos() {
        when(servicios.findByActivoTrueOrderByCategoriaAscNombreAsc()).thenReturn(List.of());

        servicio.listar("   ");

        verify(servicios).findByActivoTrueOrderByCategoriaAscNombreAsc();
        verify(servicios, never()).findByActivoTrueAndCategoriaIgnoreCaseOrderByNombreAsc("   ");
    }

    @Test
    void listar_conCategoria_filtraYLimpiaEspacios() {
        when(servicios.findByActivoTrueAndCategoriaIgnoreCaseOrderByNombreAsc("Vacunación"))
                .thenReturn(List.of(unServicio(2L, "VA001", "Vacunación", true)));

        List<ServicioResponse> lista = servicio.listar("  Vacunación ");

        assertEquals(1, lista.size());
        assertEquals("VA001", lista.get(0).codigo());
    }

    @Test
    void obtener_existente_devuelveLosDatosYSiEsAgendable() {
        when(servicios.findByIdAndActivoTrue(5L))
                .thenReturn(Optional.of(unServicio(5L, "OT004", "Otros", false)));

        ServicioResponse respuesta = servicio.obtener(5L);

        assertEquals("OT004", respuesta.codigo());
        assertFalse(respuesta.agendable());
    }

    @Test
    void obtener_agendable_marcaTrue() {
        when(servicios.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(unServicio(1L, "SV001", "Consultas", true)));

        assertTrue(servicio.obtener(1L).agendable());
    }

    @Test
    void obtener_inexistenteOInactivo_lanzaNoEncontrado() {
        when(servicios.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.obtener(99L));

        assertEquals("No existe el servicio con id 99.", ex.getMessage());
    }
}
