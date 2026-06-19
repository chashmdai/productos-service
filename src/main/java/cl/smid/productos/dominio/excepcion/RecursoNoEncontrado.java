package cl.smid.productos.dominio.excepcion;

/**
 * El recurso solicitado no existe, o existe pero queda fuera del alcance territorial del
 * solicitante. Por el override 6, la denegacion territorial se expresa como ausencia del
 * recurso para no revelar su existencia entre sedes/unidades (codigo {@code PRD-404}, HTTP 404).
 */
public class RecursoNoEncontrado extends ErrorDominio {

    public RecursoNoEncontrado(String mensaje) {
        super(mensaje);
    }

    @Override
    public CodigoError codigo() {
        return CodigoError.RECURSO_NO_ENCONTRADO;
    }
}
