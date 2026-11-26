package com.sanmarcos.citas.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanmarcos.citas.dto.ServicioResponse;
import com.sanmarcos.citas.exception.RecursoNoEncontradoException;
import com.sanmarcos.citas.security.JwtService;
import com.sanmarcos.citas.security.SecurityConfig;
import com.sanmarcos.citas.security.TokenDePrueba;
import com.sanmarcos.citas.service.ServicioService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ServicioController.class)
@Import({SecurityConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "jwt.secret=" + TokenDePrueba.SECRETO,
        "cors.origins=http://localhost:5173"})
public class ServicioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicioService servicio;

    private ServicioResponse consulta() {
        return new ServicioResponse(1L, "SV001", "Consultas", "Consulta general",
                "Perro / Gato", 30, 15000, null, true);
    }

    @Test
    void listar_sinToken_esPublicoYResponde200() throws Exception {
        when(servicio.listar(null)).thenReturn(List.of(consulta()));

        mockMvc.perform(get("/api/v1/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("SV001"))
                .andExpect(jsonPath("$[0].duracionMin").value(30))
                .andExpect(jsonPath("$[0].precio").value(15000))
                .andExpect(jsonPath("$[0].agendable").value(true));
    }

    @Test
    void listar_conCategoria_laPasaAlServicio() throws Exception {
        when(servicio.listar("Vacunación")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/servicios").param("categoria", "Vacunación"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(servicio).listar("Vacunación");
    }

    @Test
    void obtener_existente_responde200() throws Exception {
        when(servicio.obtener(1L)).thenReturn(consulta());

        mockMvc.perform(get("/api/v1/servicios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Consulta general"));
    }

    @Test
    void obtener_inexistente_responde404() throws Exception {
        when(servicio.obtener(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe el servicio con id 99."));

        mockMvc.perform(get("/api/v1/servicios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el servicio con id 99."));
    }

    @Test
    void crear_sinToken_responde401() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
