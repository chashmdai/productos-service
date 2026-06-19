package cl.smid.productos.dominio.excepcion;

/**
 * Catalogo central de codigos de error del servicio de Productos y Tareas.
 *
 * <p>Cada valor asocia un codigo estable de negocio (expuesto en el campo {@code codigo} del
 * sobre de error unificado) con su estado HTTP. El dominio solo conoce este enum; la traduccion
 * a respuesta HTTP la realiza el adaptador de API. Mantener los codigos estables: forman parte
 * del contrato publico del servicio.</p>
 */
public enum CodigoError {

    /** Entrada malformada o que viola una restriccion de formato/obligatoriedad. */
    VALIDACION("PRD-001", 400, "Solicitud invalida"),

    /** El recurso no existe o esta fuera del alcance territorial del solicitante (override 6). */
    RECURSO_NO_ENCONTRADO("PRD-404", 404, "Recurso no encontrado"),

    /** La operacion es incompatible con el estado actual del agregado (maquina de estados). */
    CONFLICTO_ESTADO("PRD-409", 409, "Conflicto de estado"),

    /** Regla de negocio incumplida con entrada sintacticamente valida (semantica). */
    REGLA_NEGOCIO("PRD-422", 422, "Regla de negocio incumplida"),

    /** Falla interna no prevista. */
    ERROR_INTERNO("PRD-500", 500, "Error interno"),

    /** Credencial ausente, malformada o expirada (resuelto en el borde de seguridad). */
    NO_AUTENTICADO("AUTZ-003", 401, "No autenticado"),

    /** Autenticado pero sin el rol requerido para la accion. */
    NO_AUTORIZADO("AUTZ-004", 403, "No autorizado");

    private final String codigo;
    private final int httpStatus;
    private final String titulo;

    CodigoError(String codigo, int httpStatus, String titulo) {
        this.codigo = codigo;
        this.httpStatus = httpStatus;
        this.titulo = titulo;
    }

    /** Codigo estable de negocio (p. ej. {@code PRD-409}). */
    public String codigo() {
        return codigo;
    }

    /** Estado HTTP asociado a este codigo. */
    public int httpStatus() {
        return httpStatus;
    }

    /** Titulo legible en espanol (campo {@code error} del sobre). */
    public String titulo() {
        return titulo;
    }
}
