package cl.smid.productos.dominio.excepcion;

/**
 * Raiz de la jerarquia de errores de negocio del dominio.
 *
 * <p>Excepcion no comprobada (el dominio no obliga a capturar): el adaptador de API la traduce
 * al sobre de error unificado leyendo {@link #codigo()}. Cada subclase fija su {@link CodigoError}
 * mediante el metodo abstracto, de modo que el tipo de la excepcion determina por completo el
 * codigo de negocio y el estado HTTP, sin necesidad de pasarlos por constructor.</p>
 */
public abstract class ErrorDominio extends RuntimeException {

    protected ErrorDominio(String mensaje) {
        super(mensaje);
    }

    /** Codigo de negocio (y, por transitividad, estado HTTP) asociado a esta clase de error. */
    public abstract CodigoError codigo();
}
