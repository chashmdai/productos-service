package cl.smid.productos.dominio.excepcion;

/**
 * El solicitante esta autenticado pero carece del rol requerido para la accion (p. ej. emitir
 * o anular sin rol de Coordinacion). Codigo {@code AUTZ-004}, HTTP 403.
 *
 * <p>Se distingue de la denegacion territorial: esta ultima se modela como
 * {@link RecursoNoEncontrado} (404), mientras que la ausencia de rol sobre un recurso visible
 * se expresa explicitamente como 403.</p>
 */
public class ErrorAutorizacion extends ErrorDominio {

    public ErrorAutorizacion(String mensaje) {
        super(mensaje);
    }

    @Override
    public CodigoError codigo() {
        return CodigoError.NO_AUTORIZADO;
    }
}
