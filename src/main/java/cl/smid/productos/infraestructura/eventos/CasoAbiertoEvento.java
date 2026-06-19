package cl.smid.productos.infraestructura.eventos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Proyeccion de consumo del evento {@code caso.abierto} emitido por el servicio de Casos (6.4).
 * Solo se modelan los campos necesarios para sembrar la tarea inicial; el resto se ignora.
 *
 * @param tipo       nombre del evento (informativo).
 * @param altKey     identificador opaco del Caso (sera el {@code idCasoAlt} de la tarea sembrada).
 * @param ocurridoEn marca de ocurrencia (informativa).
 * @param metadatos  metadatos del Caso; se usan {@code idSede} e {@code idUnidad} para heredar.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CasoAbiertoEvento(
        String tipo,
        String altKey,
        String ocurridoEn,
        Map<String, Object> metadatos
) {
    /** Valor textual de un metadato, o {@code null} si no esta presente. */
    public String metadato(String clave) {
        if (metadatos == null) {
            return null;
        }
        Object valor = metadatos.get(clave);
        return valor == null ? null : String.valueOf(valor);
    }
}
