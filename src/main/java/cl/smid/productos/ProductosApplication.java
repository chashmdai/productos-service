package cl.smid.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada del microservicio <strong>productos-service</strong> (SMID 6.6), capa de
 * planificacion y entregables (Productos y Tareas) del ecosistema de la Defensoria de los Derechos
 * de la Ninez.
 *
 * <p>{@link ConfigurationPropertiesScan} habilita el registro de los records
 * {@code @ConfigurationProperties} (seguridad, enriquecimiento y sedes) sin declararlos uno a uno.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ProductosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductosApplication.class, args);
    }
}
