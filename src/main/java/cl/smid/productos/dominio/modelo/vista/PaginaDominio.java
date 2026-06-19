package cl.smid.productos.dominio.modelo.vista;

import java.util.Collections;
import java.util.List;

/**
 * Pagina de resultados neutral del dominio: desacopla la paginacion de la API (Spring Data
 * {@code Page}) para que los puertos de salida no filtren tipos de infraestructura al dominio.
 *
 * <p>El adaptador de persistencia traduce su {@code Page<Entidad>} a esta vista; el adaptador
 * de API la traduce al sobre de paginacion JSON. Indices de pagina base 0.</p>
 *
 * @param contenido       elementos de la pagina actual.
 * @param pagina          indice de pagina solicitado (base 0).
 * @param tamano          tamano de pagina solicitado.
 * @param totalElementos  total de elementos que satisfacen el filtro.
 * @param totalPaginas    total de paginas disponibles.
 * @param <T>             tipo de elemento.
 */
public record PaginaDominio<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas
) {
    public PaginaDominio {
        contenido = contenido == null ? List.of() : List.copyOf(contenido);
    }

    /** Construye una pagina a partir de sus elementos y los totales calculados por el repositorio. */
    public static <T> PaginaDominio<T> de(List<T> contenido, int pagina, int tamano, long totalElementos) {
        int totalPaginas = tamano <= 0 ? 0 : (int) Math.ceil((double) totalElementos / (double) tamano);
        return new PaginaDominio<>(contenido, pagina, tamano, totalElementos, totalPaginas);
    }

    /** Pagina vacia (sin elementos) preservando los parametros de la consulta. */
    public static <T> PaginaDominio<T> vacia(int pagina, int tamano) {
        return new PaginaDominio<>(Collections.emptyList(), pagina, tamano, 0L, 0);
    }
}
