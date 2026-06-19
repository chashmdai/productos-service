package cl.smid.productos.dominio.puerto.salida;

/**
 * Puerto de generacion del correlativo oficial de Productos, aislado por sede y anio.
 *
 * <p>La implementacion es segura ante concurrencia: reserva atomicamente el siguiente valor de la
 * serie ({@code sede}, {@code anio}) participando en la transaccion en curso, de modo que dos
 * emisiones simultaneas en la misma serie obtienen valores distintos y contiguos. No existe serie
 * Beta en este servicio (a diferencia de Requerimientos/FIR).</p>
 */
public interface CorrelativoProductoPort {

    /**
     * Reserva y devuelve el siguiente correlativo de la serie indicada.
     *
     * @param idSedeAlt alt_key de la sede (componente de la serie).
     * @param anio      anio de la serie.
     * @return correlativo estrictamente creciente y unico dentro de la serie (1, 2, 3, ...).
     */
    long siguiente(String idSedeAlt, int anio);
}
