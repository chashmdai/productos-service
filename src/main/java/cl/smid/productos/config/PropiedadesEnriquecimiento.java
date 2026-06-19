package cl.smid.productos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Propiedades del enriquecimiento on-demand (prefijo {@code smid.enriquecimiento}).
 *
 * <p>Cada bandera conmuta entre el cliente REST real y un cliente nulo (defecto). Con Casos
 * desactivado, la herencia territorial proviene del contexto del usuario; con Personas
 * desactivado, las tareas no guardan el snapshot de nombre del responsable.</p>
 *
 * @param casos            activa el cliente REST de Casos (6.4).
 * @param personas         activa el cliente REST de Personas (6.2).
 * @param casosUrlBase     URL base del recurso de Casos (p. ej. {@code http://localhost:8090/casos}).
 * @param personasUrlBase  URL base del recurso de Personas (p. ej. {@code http://localhost:8088/personas}).
 */
@ConfigurationProperties(prefix = "smid.enriquecimiento")
public record PropiedadesEnriquecimiento(
        @DefaultValue("false") boolean casos,
        @DefaultValue("false") boolean personas,
        String casosUrlBase,
        String personasUrlBase
) {
}
