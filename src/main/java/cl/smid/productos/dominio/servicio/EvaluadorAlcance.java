package cl.smid.productos.dominio.servicio;

import cl.smid.productos.dominio.modelo.Alcance;
import cl.smid.productos.dominio.modelo.ContextoSesion;

import java.util.Objects;

/**
 * Servicio de dominio que decide la visibilidad territorial de un registro para un contexto de
 * sesion, segun el override 6 (la denegacion territorial se expresa como 404 en el orquestador).
 *
 * <ul>
 *   <li>{@code NACIONAL}: ve todos los registros.</li>
 *   <li>{@code SEDE}: ve los registros cuya sede coincide con la del usuario.</li>
 *   <li>{@code UNIDAD}: ve los registros cuya unidad coincide con la del usuario.</li>
 * </ul>
 *
 * <p>Se aplica registro a registro en las operaciones puntuales (detalle, transiciones). El
 * filtrado de listados se materializa como predicado en el adaptador de persistencia para no
 * traer filas fuera de alcance.</p>
 */
public class EvaluadorAlcance {

    /**
     * Indica si el registro identificado por su sede/unidad es visible para el contexto.
     *
     * @param idSedeRegistro   sede del registro.
     * @param idUnidadRegistro unidad del registro.
     * @param ctx              contexto de sesion del solicitante.
     * @return {@code true} si el registro es visible bajo el alcance del contexto.
     */
    public boolean puedeVer(String idSedeRegistro, String idUnidadRegistro, ContextoSesion ctx) {
        Alcance alcance = ctx.alcance();
        return switch (alcance) {
            case NACIONAL -> true;
            case SEDE -> Objects.equals(idSedeRegistro, ctx.idSede());
            case UNIDAD -> Objects.equals(idUnidadRegistro, ctx.idUnidad());
        };
    }
}
