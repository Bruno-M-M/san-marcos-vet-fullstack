package com.sanmarcos.citas.service;

import com.sanmarcos.citas.dto.ServicioResponse;
import com.sanmarcos.citas.exception.RecursoNoEncontradoException;
import com.sanmarcos.citas.model.Servicio;
import com.sanmarcos.citas.repository.ServicioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioService {

    private final ServicioRepository servicios;

    public ServicioService(ServicioRepository servicios) {
        this.servicios = servicios;
    }

    private ServicioResponse respuesta(Servicio s) {
        return new ServicioResponse(s.getId(), s.getCodigo(), s.getCategoria(), s.getNombre(),
                s.getEspecie(), s.getDuracionMin(), s.getPrecio(), s.getObservaciones(), s.isAgendable());
    }

    /** Sin categoría devuelve todos los servicios activos. */
    @Transactional(readOnly = true)
    public List<ServicioResponse> listar(String categoria) {
        List<Servicio> lista = (categoria == null || categoria.isBlank())
                ? servicios.findByActivoTrueOrderByCategoriaAscNombreAsc()
                : servicios.findByActivoTrueAndCategoriaIgnoreCaseOrderByNombreAsc(categoria.trim());
        return lista.stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtener(Long id) {
        return servicios.findByIdAndActivoTrue(id)
                .map(this::respuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el servicio con id " + id + "."));
    }
}
