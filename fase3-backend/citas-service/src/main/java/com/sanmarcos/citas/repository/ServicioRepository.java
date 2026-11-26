package com.sanmarcos.citas.repository;

import com.sanmarcos.citas.model.Servicio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByActivoTrueOrderByCategoriaAscNombreAsc();

    List<Servicio> findByActivoTrueAndCategoriaIgnoreCaseOrderByNombreAsc(String categoria);

    Optional<Servicio> findByIdAndActivoTrue(Long id);
}
