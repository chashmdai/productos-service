package cl.smid.productos.dominio.servicio;

import cl.smid.productos.dominio.modelo.NumeroProducto;
import cl.smid.productos.dominio.puerto.salida.CorrelativoProductoPort;
import cl.smid.productos.dominio.puerto.salida.DirectorioSedes;

/**
 * Servicio de dominio que construye el numero oficial de un Producto al emitirlo.
 *
 * <p>Combina el codigo corto de la sede ({@link DirectorioSedes}) con el correlativo atomico de la
 * serie ({@link CorrelativoProductoPort}) para producir {@code PRD-<CODIGO_SEDE>-<n>/<anio>}. La
 * reserva del correlativo participa en la transaccion en curso, garantizando unicidad y contiguidad
 * ante concurrencia. No hay serie Beta en este servicio.</p>
 */
public class GeneradorNumeroProducto {

    private final CorrelativoProductoPort correlativo;
    private final DirectorioSedes directorioSedes;

    public GeneradorNumeroProducto(CorrelativoProductoPort correlativo, DirectorioSedes directorioSedes) {
        this.correlativo = correlativo;
        this.directorioSedes = directorioSedes;
    }

    /**
     * Genera el numero oficial para la sede y el anio dados, reservando el siguiente correlativo.
     *
     * @param idSedeAlt sede emisora (componente de la serie y origen del codigo corto).
     * @param anio      anio de la emision (UTC).
     * @return numero oficial formateado.
     */
    public NumeroProducto generar(String idSedeAlt, int anio) {
        long n = correlativo.siguiente(idSedeAlt, anio);
        String codigo = directorioSedes.codigoDe(idSedeAlt);
        return NumeroProducto.de(codigo, n, anio);
    }
}
