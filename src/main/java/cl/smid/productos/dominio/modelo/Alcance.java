package cl.smid.productos.dominio.modelo;

/**
 * Alcance territorial del usuario, derivado del claim {@code alcance} del JWT.
 * Determina el filtrado registro a registro (override 6: la denegacion territorial es 404).
 */
public enum Alcance {
    /** Ve solo registros de su unidad. */
    UNIDAD,
    /** Ve registros de su sede. */
    SEDE,
    /** Ve todos los registros del ecosistema. */
    NACIONAL
}
