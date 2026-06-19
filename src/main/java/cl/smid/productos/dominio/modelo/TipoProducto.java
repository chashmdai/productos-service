package cl.smid.productos.dominio.modelo;

/**
 * Tipo de entregable que representa un Producto dentro de un Caso.
 *
 * <p>Se modela como enum del dominio (fuente de verdad). En persistencia se almacena
 * como {@code VARCHAR(N) + CHECK} (override 3) y en la frontera HTTP se expone como
 * {@code String}, nunca como ordinal.</p>
 */
public enum TipoProducto {
    INFORME,
    GESTION,
    OFICIO,
    DERIVACION,
    RESOLUCION,
    OTRO
}
