package com.sanmarcos.citas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanmarcos.citas.dto.ComentarioResponse;
import com.sanmarcos.citas.exception.RecursoNoEncontradoException;
import com.sanmarcos.citas.security.JwtService;
import com.sanmarcos.citas.security.SecurityConfig;
import com.sanmarcos.citas.security.TokenDePrueba;
import com.sanmarcos.citas.service.ComentarioService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ComentarioController.class)
@Import({SecurityConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "jwt.secret=" + TokenDePrueba.SECRETO,
        "cors.origins=http://localhost:5173"})
public class ComentarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ComentarioService servicio;

    private static final String COMENTARIO_VALIDO =
            "{\"nombre\":\"Ana\",\"correo\":\"ana@gmail.com\",\"comentario\":\"Excelente atención\"}";

    private ComentarioResponse unComentario() {
        return new ComentarioResponse(1L, "Ana", "ana@gmail.com", "Excelente atención",
                LocalDateTime.of(2026, 11, 20, 10, 30));
    }

    private String bearer(String rol) {
        return "Bearer " + TokenDePrueba.acceso(rol);
    }

    @Test
    void publicar_sinToken_responde201() throws Exception {
        when(servicio.publicar(any())).thenReturn(unComentario());

        mockMvc.perform(post("/api/v1/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMENTARIO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.comentario").value("Excelente atención"));
    }

    @Test
    void publicar_conCorreoDeOtroDominio_responde400ConElCampo() throws Exception {
        mockMvc.perform(post("/api/v1/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"correo\":\"ana@hotmail.com\",\"comentario\":\"Hola\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").exists());

        verifyNoInteractions(servicio);
    }

    @Test
    void publicar_sinNombre_responde400ConElCampo() throws Exception {
        mockMvc.perform(post("/api/v1/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"  \",\"correo\":\"ana@gmail.com\",\"comentario\":\"Hola\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());

        verifyNoInteractions(servicio);
    }

    @Test
    void publicar_conComentarioDeMasDe500Caracteres_responde400() throws Exception {
        String largo = "x".repeat(501);

        mockMvc.perform(post("/api/v1/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"correo\":\"ana@gmail.com\",\"comentario\":\"" + largo + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.comentario").exists());
    }

    @Test
    void listar_sinToken_responde401() throws Exception {
        mockMvc.perform(get("/api/v1/comentarios"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(servicio);
    }

    @Test
    void listar_comoCliente_responde403() throws Exception {
        mockMvc.perform(get("/api/v1/comentarios")
                        .header("Authorization", bearer("CLIENTE")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(servicio);
    }

    @Test
    void listar_comoAdministrador_responde200() throws Exception {
        when(servicio.listar()).thenReturn(List.of(unComentario()));

        mockMvc.perform(get("/api/v1/comentarios")
                        .header("Authorization", bearer("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("ana@gmail.com"));
    }

    @Test
    void eliminar_comoAdministrador_responde204() throws Exception {
        mockMvc.perform(delete("/api/v1/comentarios/1")
                        .header("Authorization", bearer("ADMINISTRADOR")))
                .andExpect(status().isNoContent());

        verify(servicio).eliminar(1L);
    }

    @Test
    void eliminar_comoVendedor_responde403() throws Exception {
        mockMvc.perform(delete("/api/v1/comentarios/1")
                        .header("Authorization", bearer("VENDEDOR")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(servicio);
    }

    @Test
    void eliminar_inexistente_responde404() throws Exception {
        org.mockito.Mockito.doThrow(new RecursoNoEncontradoException("No existe el comentario con id 99."))
                .when(servicio).eliminar(99L);

        mockMvc.perform(delete("/api/v1/comentarios/99")
                        .header("Authorization", bearer("ADMINISTRADOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el comentario con id 99."));
    }
}
