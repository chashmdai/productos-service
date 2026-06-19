package cl.smid.productos.infraestructura.eventos;

import cl.smid.productos.dominio.modelo.EventoDominio;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Sobre serializable de un evento de dominio para su publicacion en el bus. Estructura comun a
 * todos los servicios SMID: {@code tipo}, {@code altKey} del agregado, {@code ocurridoEn} en
 * ISO-8601 UTC y {@code metadatos} no sensibles.
 *
 * @param tipo       nombre del evento.
 * @param altKey     identificador opaco del agregado.
 * @param ocurridoEn instante UTC en formato ISO-8601.
 * @param metadatos  pares no sensibles (override 8).
 */
public record SobreEvento(
        String tipo,
        String altKey,
        String ocurridoEn,
        Map<String, Object> metadatos
) {
    /** Construye el sobre a partir del evento de dominio, formateando la marca en ISO-8601 UTC. */
    public static SobreEvento desde(EventoDominio evento) {
        return new SobreEvento(
                evento.tipo(),
                evento.altKey(),
                DateTimeFormatter.ISO_INSTANT.format(evento.ocurridoEn()),
                evento.metadatos());
    }
}
