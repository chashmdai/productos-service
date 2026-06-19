package cl.smid.productos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Propiedades de seguridad del servicio (prefijo {@code smid.seguridad}).
 *
 * <p><b>Override 10.</b> En lugar de un mapa de claves indexado por {@code kid} (cuyos placeholders
 * de entorno no resuelven de forma fiable en Spring), se usa un par fijo activo
 * ({@code kidActivo}/{@code secretoActivo}) y un par previo opcional
 * ({@code kidPrevio}/{@code secretoPrevio}) para permitir la rotacion sin downtime: los tokens
 * firmados con la clave previa siguen validandose mientras dure la ventana de rotacion.</p>
 *
 * @param kidActivo       identificador de la clave de firma vigente.
 * @param secretoActivo   secreto HMAC vigente (UTF-8, &ge; 32 bytes).
 * @param kidPrevio       identificador de la clave previa (opcional, rotacion).
 * @param secretoPrevio   secreto HMAC previo (opcional).
 * @param issuer          emisor esperado del token ({@code iss}).
 * @param audiencia       audiencia que el token debe contener ({@code aud}).
 * @param rolesCoordinacion roles que confieren capacidades de Coordinacion (emitir/anular, operar
 *                          cualquier tarea).
 */
@ConfigurationProperties(prefix = "smid.seguridad")
public record PropiedadesSeguridad(
        String kidActivo,
        String secretoActivo,
        String kidPrevio,
        String secretoPrevio,
        @DefaultValue("smid-auth") String issuer,
        @DefaultValue("smid-servicios") String audiencia,
        List<String> rolesCoordinacion
) {
    public PropiedadesSeguridad {
        rolesCoordinacion = rolesCoordinacion == null ? List.of() : List.copyOf(rolesCoordinacion);
    }
}
