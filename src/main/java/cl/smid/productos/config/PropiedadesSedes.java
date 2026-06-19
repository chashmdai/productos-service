package cl.smid.productos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

/**
 * Propiedades del directorio de codigos de sede (prefijo {@code smid.sedes}). Mapea el
 * {@code alt_key} de cada sede a su codigo corto, usado en el numero oficial del Producto. Las
 * sedes no configuradas reciben el codigo por defecto.
 *
 * @param codigos mapa {@code alt_key -> codigo corto} (en YAML: {@code smid.sedes.codigos[<alt_key>]}).
 * @param defecto codigo a usar cuando la sede no esta en el mapa.
 */
@ConfigurationProperties(prefix = "smid.sedes")
public record PropiedadesSedes(
        Map<String, String> codigos,
        @DefaultValue("STG") String defecto
) {
    public PropiedadesSedes {
        codigos = codigos == null ? Map.of() : Map.copyOf(codigos);
    }
}
