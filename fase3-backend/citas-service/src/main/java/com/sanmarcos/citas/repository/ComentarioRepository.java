package com.sanmarcos.citas.repository;

import com.sanmarcos.citas.model.Comentario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findAllByOrderByFechaDesc();
}
