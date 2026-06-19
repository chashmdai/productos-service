package cl.smid.productos.dominio.modelo;

import java.util.List;
import java.util.Objects;

/**
 * Contexto de sesion corporativa derivado del JWT validado en el borde.
 *
 * <p>Es un objeto de valor del dominio (POJO puro): transporta exclusivamente los
 * atributos de segmentacion necesarios para autorizar (rol) y filtrar territorialmente
 * (sede/unidad/alcance). <strong>No</strong> contiene el token bruto: la propagacion del
 * bearer hacia clientes de enriquecimiento se resuelve en infraestructura
 * ({@code ProveedorContexto}), manteniendo el dominio libre de detalles de transporte.</p>
 *
 * @param sub      alt_key del usuario autenticado (claim {@code sub}).
 * @param roles    roles del usuario (claim {@code roles}); nunca nulo.
 * @param idSede   alt_key de la sede del usuario (claim {@code idSede}).
 * @param idUnidad alt_key de la unidad del usuario (claim {@code idUnidad}).
 * @param alcance  alcance territorial maximo del usuario.
 * @param nombre   nombre legible del usuario (claim {@code nombre}); informativo.
 */
public record ContextoSesion(
        String sub,
        List<String> roles,
        String idSede,
        String idUnidad,
        Alcance alcance,
        String nombre
) {
    public ContextoSesion {
        Objects.requireNonNull(sub, "sub es obligatorio");
        Objects.requireNonNull(alcance, "alcance es obligatorio");
        roles = roles == null ? List.of() : List.copyOf(roles);
    }

    /** {@code true} si alguno de los roles del usuario esta en el conjunto indicado. */
    public boolean tieneAlgunRol(java.util.Collection<String> conjunto) {
        if (conjunto == null || conjunto.isEmpty()) {
            return false;
        }
        for (String r : roles) {
            if (conjunto.contains(r)) {
                return true;
            }
        }
        return false;
    }
}
