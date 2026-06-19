package cl.smid.productos.dominio.excepcion;

/**
 * Entrada malformada o que incumple una restriccion de formato u obligatoriedad
 * detectada por el dominio (codigo {@code PRD-001}, HTTP 400).
 */
public class ErrorValidacion extends ErrorDominio {

    public ErrorValidacion(String mensaje) {
        super(mensaje);
    }

    @Override
    public CodigoError codigo() {
        return CodigoError.VALIDACION;
    }
}
