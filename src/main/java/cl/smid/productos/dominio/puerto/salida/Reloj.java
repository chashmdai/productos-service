package cl.smid.productos.dominio.puerto.salida;

import java.time.Instant;

/**
 * Puerto de tiempo. Abstrae el reloj del sistema para que el dominio sea determinista y testeable
 * (en pruebas se inyecta un reloj fijo). Siempre devuelve instantes en UTC.
 */
public interface Reloj {

    /** Instante actual en UTC. */
    Instant ahora();
}
