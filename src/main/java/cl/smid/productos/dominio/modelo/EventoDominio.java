package cl.smid.productos.dominio.modelo;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Evento de dominio a publicar hacia el bus corporativo.
 *
 * <p><b>Override 8 (metadata-only).</b> Los {@code metadatos} solo pueden contener identificadores
 * opacos y atributos no sensibles (tipo, prioridad, numero de producto). Esta clase no impone el
 * contenido, pero el orquestador es responsable de no incluir jamas datos personales de NNA. El
 * transporte (log o RabbitMQ) es conmutable y la publicacion es tolerante a fallos.</p>
 *
 * @param tipo       nombre del evento (p. ej. {@code producto.emitido}).
 * @param altKey     identificador opaco del agregado afectado.
 * @param ocurridoEn instante UTC de ocurrencia.
 * @param metadatos  pares no sensibles adicionales (nunca nulo; iteracion estable).
 */
public record EventoDominio(
        String tipo,
        String altKey,
        Instant ocurridoEn,
        Map<String, Object> metadatos
) {
    public EventoDominio {
        Objects.requireNonNull(tipo, "tipo es obligatorio");
        Objects.requireNonNull(altKey, "altKey es obligatorio");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn es obligatorio");
        metadatos = metadatos == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(metadatos));
    }

    /**
     * Crea un evento preservando el orden de insercion de los metadatos.
     *
     * @param tipo      nombre del evento.
     * @param altKey    identificador opaco del agregado.
     * @param ahora     instante UTC.
     * @param claves    secuencia plana clave1, valor1, clave2, valor2, ... (longitud par).
     */
    public static EventoDominio de(String tipo, String altKey, Instant ahora, Object... claves) {
        Map<String, Object> meta = new LinkedHashMap<>();
        if (claves != null) {
            if (claves.length % 2 != 0) {
                throw new IllegalArgumentException("Los metadatos requieren pares clave/valor completos");
            }
            for (int i = 0; i < claves.length; i += 2) {
                Object valor = claves[i + 1];
                if (valor != null) {
                    meta.put(String.valueOf(claves[i]), valor);
                }
            }
        }
        return new EventoDominio(tipo, altKey, ahora, meta);
    }
}
