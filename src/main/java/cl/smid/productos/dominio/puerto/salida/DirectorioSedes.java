package cl.smid.productos.dominio.puerto.salida;

/**
 * Puerto de resolucion del codigo corto de una sede a partir de su {@code alt_key}. El codigo
 * se incrusta en el numero oficial del Producto ({@code PRD-<CODIGO_SEDE>-<n>/<anio>}). Cuando la
 * sede no tiene codigo configurado, la implementacion devuelve un valor por defecto estable.
 */
public interface DirectorioSedes {

    /** Codigo corto de la sede (p. ej. {@code STG}); valor por defecto si no esta configurada. */
    String codigoDe(String idSedeAlt);
}
