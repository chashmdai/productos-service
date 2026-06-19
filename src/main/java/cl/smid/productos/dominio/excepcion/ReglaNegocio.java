package cl.smid.productos.dominio.excepcion;

/**
 * Regla de negocio incumplida con entrada sintacticamente valida (semantica): por ejemplo,
 * reasignar sin indicar responsable. Codigo {@code PRD-422}, HTTP 422.
 */
public class ReglaNegocio extends ErrorDominio {

    public ReglaNegocio(String mensaje) {
        super(mensaje);
    }

    @Override
    public CodigoError codigo() {
        return CodigoError.REGLA_NEGOCIO;
    }
}
