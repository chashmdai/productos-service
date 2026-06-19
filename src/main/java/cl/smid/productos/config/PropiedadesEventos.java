package cl.smid.productos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Propiedades de eventos del servicio (prefijo {@code smid.eventos}).
 *
 * @param transporte transporte de publicacion de eventos: {@code log} o {@code rabbitmq}.
 * @param consumo    consumo de eventos entrantes: {@code none} o {@code rabbitmq}.
 */
@ConfigurationProperties(prefix = "smid.eventos")
public record PropiedadesEventos(
        @DefaultValue("log") String transporte,
        @DefaultValue("none") String consumo
) {
}
