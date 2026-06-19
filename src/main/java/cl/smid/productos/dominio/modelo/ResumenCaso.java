package cl.smid.productos.dominio.modelo;

/**
 * Resumen no sensible de un Caso, obtenido del servicio 6.4 (Casos) cuando el enriquecimiento
 * on-demand esta activo. Aporta la sede y la unidad que el Producto/Tarea hereda, ademas de
 * datos informativos de validacion (estado, expediente).
 *
 * <p>Solo transporta identificadores opacos y atributos de segmentacion; nunca datos personales.
 * Con el enriquecimiento desactivado (defecto) este resumen no se consulta y la herencia
 * territorial proviene del contexto del usuario.</p>
 *
 * @param idCasoAlt        identificador opaco del Caso.
 * @param numeroExpediente numero de expediente legible (informativo).
 * @param estado           estado del Caso (informativo).
 * @param idSedeAlt        sede del Caso (heredable).
 * @param idUnidadAlt      unidad del Caso (heredable).
 * @param complejidad      complejidad del Caso (informativa).
 */
public record ResumenCaso(
        String idCasoAlt,
        String numeroExpediente,
        String estado,
        String idSedeAlt,
        String idUnidadAlt,
        String complejidad
) {
}
