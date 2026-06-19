package cl.smid.productos.infraestructura.persistencia.mapeo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Conversion entre el {@link Instant} del dominio y el {@link LocalDateTime} de las entidades JPA,
 * fijando siempre UTC (coherente con {@code hibernate.jdbc.time_zone=UTC} y columnas DATETIME(6)).
 */
public final class ConversorTiempo {

    private ConversorTiempo() {
    }

    /** Convierte un instante UTC a {@link LocalDateTime} (sin desplazamiento). */
    public static LocalDateTime aLocalDateTime(Instant instante) {
        return instante == null ? null : LocalDateTime.ofInstant(instante, ZoneOffset.UTC);
    }

    /** Convierte un {@link LocalDateTime} (interpretado en UTC) a instante. */
    public static Instant aInstant(LocalDateTime fechaHora) {
        return fechaHora == null ? null : fechaHora.toInstant(ZoneOffset.UTC);
    }
}
