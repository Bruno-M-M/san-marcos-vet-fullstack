package com.sanmarcos.citas.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sanmarcos.citas.model.Servicio;
import com.sanmarcos.citas.repository.ServicioRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Usa el CSV real de seed/servicios.csv, así que también valida ese archivo. */
@ExtendWith(MockitoExtension.class)
public class DatosInicialesTest {

    @Mock
    private ServicioRepository servicios;

    @InjectMocks
    private DatosIniciales datos;

    @Test
    @SuppressWarnings("unchecked")
    void conLaTablaVacia_cargaElCatalogoCompleto() throws Exception {
        when(servicios.count()).thenReturn(0L);

        datos.run();

        ArgumentCaptor<List<Servicio>> captura = ArgumentCaptor.forClass(List.class);
        verify(servicios).saveAll(captura.capture());
        List<Servicio> cargados = captura.getValue();

        assertEquals(31, cargados.size());

        Set<String> codigos = cargados.stream().map(Servicio::getCodigo).collect(Collectors.toSet());
        assertEquals(cargados.size(), new HashSet<>(codigos).size(), "hay códigos repetidos");

        cargados.forEach(s -> {
            assertTrue(s.getDuracionMin() > 0, s.getCodigo() + " sin duración");
            assertTrue(s.getPrecio() > 0, s.getCodigo() + " sin precio");
            assertTrue(s.isActivo(), s.getCodigo() + " debe quedar activo");
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void interpretaBienLasColumnas() throws Exception {
        when(servicios.count()).thenReturn(0L);

        datos.run();

        ArgumentCaptor<List<Servicio>> captura = ArgumentCaptor.forClass(List.class);
        verify(servicios).saveAll(captura.capture());
        List<Servicio> cargados = captura.getValue();

        Servicio consulta = cargados.stream().filter(s -> s.getCodigo().equals("SV001")).findFirst().orElseThrow();
        assertEquals("Consultas", consulta.getCategoria());
        assertEquals("Consulta general", consulta.getNombre());
        assertEquals(30, consulta.getDuracionMin());
        assertEquals(15000, consulta.getPrecio());
        assertNull(consulta.getObservaciones(), "una observación vacía debe quedar nula");
        assertTrue(consulta.isAgendable());

        Servicio urgencia = cargados.stream().filter(s -> s.getCodigo().equals("SV002")).findFirst().orElseThrow();
        assertEquals("Fuera de horario +$10.000", urgencia.getObservaciones());

        Servicio hospitalizacion = cargados.stream().filter(s -> s.getCodigo().equals("OT004")).findFirst().orElseThrow();
        assertEquals(1440, hospitalizacion.getDuracionMin(), "24 h = 1440 min");
        assertFalse(hospitalizacion.isAgendable(), "la hospitalización no se agenda como cita");

        assertEquals(1, cargados.stream().filter(s -> !s.isAgendable()).count());
    }

    @Test
    void siYaHayServicios_noCargaNada() throws Exception {
        when(servicios.count()).thenReturn(31L);

        datos.run();

        verify(servicios, never()).saveAll(anyList());
    }
}
