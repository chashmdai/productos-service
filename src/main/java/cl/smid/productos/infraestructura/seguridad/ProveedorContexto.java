package cl.smid.productos.infraestructura.seguridad;

import cl.smid.productos.dominio.modelo.ContextoSesion;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Acceso al contexto de sesion de la peticion en curso. El filtro de autenticacion deposita el
 * {@link ContextoSesion} y el token bruto como atributos de la peticion; este proveedor los lee
 * bajo demanda.
 *
 * <p>Se respalda en los atributos de peticion (no en un bean {@code @RequestScope}) para no
 * acoplar el grafo de beles al ciclo de la peticion y permitir su uso desde adaptadores salientes
 * (clientes REST) que necesitan propagar el bearer del solicitante.</p>
 */
@Component
public class ProveedorContexto {

    /** Atributo de peticion donde el filtro guarda el contexto de sesion. */
    public static final String ATRIBUTO_CONTEXTO = "smid.contexto";

    /** Atributo de peticion donde el filtro guarda el token bruto (para propagacion). */
    public static final String ATRIBUTO_TOKEN = "smid.token";

    /**
     * Contexto de sesion de la peticion actual.
     *
     * @throws IllegalStateException si se invoca fuera de una peticion autenticada.
     */
    public ContextoSesion contextoActual() {
        Object valor = atributo(ATRIBUTO_CONTEXTO);
        if (valor instanceof ContextoSesion contexto) {
            return contexto;
        }
        throw new IllegalStateException("No hay contexto de sesion disponible en la peticion");
    }

    /** Token bruto de la peticion actual, o {@code null} si no esta disponible. */
    public String tokenActual() {
        Object valor = atributo(ATRIBUTO_TOKEN);
        return valor instanceof String token ? token : null;
    }

    private Object atributo(@NonNull String nombre) {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (atributos == null) {
            return null;
        }
        return atributos.getAttribute(nombre, RequestAttributes.SCOPE_REQUEST);
    }
}
