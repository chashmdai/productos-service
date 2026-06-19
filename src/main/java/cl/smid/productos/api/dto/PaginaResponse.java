package cl.smid.productos.api.dto;

import cl.smid.productos.dominio.modelo.vista.PaginaDominio;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.function.Function;

/**
 * Sobre de paginacion expuesto al cliente. Refleja {@link PaginaDominio} con indices base 0.
 *
 * @param contenido      elementos de la pagina.
 * @param pagina         indice de pagina (base 0).
 * @param tamano         tamano de pagina.
 * @param totalElementos total de elementos que satisfacen el filtro.
 * @param totalPaginas   total de paginas.
 * @param <T>            tipo de elemento de respuesta.
 */
@Schema(description = "Página de resultados de la API.")
public record PaginaResponse<T>(
        @Schema(description = "Contenido de la página.")
        List<T> contenido,
        @Schema(description = "Número de página, base cero.", example = "0")
        int pagina,
        @Schema(description = "Tamaño de página aplicado.", example = "20")
        int tamano,
        @Schema(description = "Total de elementos encontrados.", example = "42")
        long totalElementos,
        @Schema(description = "Total de páginas.", example = "3")
        int totalPaginas
) {
    /** Transforma una pagina de dominio en pagina de respuesta aplicando el mapeo de cada elemento. */
    public static <D, R> PaginaResponse<R> de(PaginaDominio<D> pagina, Function<D, R> mapeo) {
        List<R> contenido = pagina.contenido().stream().map(mapeo).toList();
        return new PaginaResponse<>(contenido, pagina.pagina(), pagina.tamano(),
                pagina.totalElementos(), pagina.totalPaginas());
    }
}
