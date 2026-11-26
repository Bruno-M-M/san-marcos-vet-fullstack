package com.sanmarcos.citas.config;

import com.sanmarcos.citas.model.Servicio;
import com.sanmarcos.citas.repository.ServicioRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/** Carga el catálogo inicial de servicios la primera vez (si la tabla está vacía). */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final ServicioRepository servicios;

    public DatosIniciales(ServicioRepository servicios) {
        this.servicios = servicios;
    }

    @Override
    public void run(String... args) throws IOException {
        if (servicios.count() > 0) {
            return;
        }

        List<Servicio> nuevos = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(
                new ClassPathResource("seed/servicios.csv").getInputStream(), StandardCharsets.UTF_8))) {

            lector.readLine(); // encabezado

            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                String[] c = linea.split("\\|", -1);

                Servicio s = new Servicio();
                s.setCodigo(c[0]);
                s.setCategoria(c[1]);
                s.setNombre(c[2]);
                s.setEspecie(c[3]);
                s.setDuracionMin(Integer.valueOf(c[4]));
                s.setPrecio(Integer.valueOf(c[5]));
                s.setObservaciones(c[6].isEmpty() ? null : c[6]);
                s.setAgendable(Boolean.parseBoolean(c[7]));
                s.setActivo(true);
                nuevos.add(s);
            }
        }
        servicios.saveAll(nuevos);
    }
}
