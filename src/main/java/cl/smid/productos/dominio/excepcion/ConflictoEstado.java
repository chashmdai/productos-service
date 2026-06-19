package cl.smid.productos.dominio.excepcion;

/**
 * La operacion solicitada es incompatible con el estado actual del agregado segun su maquina
 * de estados (p. ej. emitir un producto ya emitido, tomar una tarea cancelada). Codigo
 * {@code PRD-409}, HTTP 409.
 */
public class ConflictoEstado extends ErrorDominio {

    public ConflictoEstado(String mensaje) {
        super(mensaje);
    }

    @Override
    public CodigoError codigo() {
        return CodigoError.CONFLICTO_ESTADO;
    }
}
