package cl.smid.productos.dominio.puerto.salida;

/**
 * Puerto de generacion de identificadores opacos ({@code alt_key}). Permite inyectar un generador
 * determinista en pruebas. La implementacion de produccion emite UUID version 4 en formato canonico.
 */
public interface GeneradorIdentificadores {

    /** Nuevo identificador opaco (UUID canonico de 36 caracteres). */
    String nuevo();
}
