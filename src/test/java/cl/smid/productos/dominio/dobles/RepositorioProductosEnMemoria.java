package cl.smid.productos.dominio.dobles;

import cl.smid.productos.dominio.excepcion.RecursoNoEncontrado;
import cl.smid.productos.dominio.modelo.Producto;
import cl.smid.productos.dominio.modelo.vista.PaginaDominio;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroProductos;
import cl.smid.productos.dominio.puerto.salida.RepositorioProductos;
import cl.smid.productos.dominio.servicio.EvaluadorAlcance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de Productos en memoria para pruebas de dominio. Emula la unicidad del numero, el
 * filtrado territorial (via {@link EvaluadorAlcance}) y la paginacion.
 */
public class RepositorioProductosEnMemoria implements RepositorioProductos {

    private final Map<String, Producto> porAltKey = new LinkedHashMap<>();
    private final EvaluadorAlcance alcance = new EvaluadorAlcance();

    @Override
    public void crear(Producto producto) {
        validarNumeroUnico(producto);
        porAltKey.put(producto.altKey(), producto);
    }

    @Override
    public void actualizar(Producto producto) {
        if (!porAltKey.containsKey(producto.altKey())) {
            throw new RecursoNoEncontrado("Producto no encontrado: " + producto.altKey());
        }
        validarNumeroUnico(producto);
        porAltKey.put(producto.altKey(), producto);
    }

    @Override
    public Optional<Producto> buscarPorAltKey(String altKey) {
        return Optional.ofNullable(porAltKey.get(altKey));
    }

    @Override
    public PaginaDominio<Producto> listar(FiltroProductos filtro) {
        List<Producto> filtrados = new ArrayList<>(porAltKey.values()).stream()
                .filter(p -> alcance.puedeVer(p.idSedeAlt(), p.idUnidadAlt(), filtro.contexto()))
                .filter(p -> vacio(filtro.idCasoAlt()) || filtro.idCasoAlt().equals(p.idCasoAlt()))
                .filter(p -> filtro.estado() == null || filtro.estado() == p.estado())
                .filter(p -> filtro.tipo() == null || filtro.tipo() == p.tipo())
                .toList();
        return paginar(filtrados, filtro.pagina(), filtro.tamano());
    }

    private void validarNumeroUnico(Producto producto) {
        String numero = producto.numeroComoCadena();
        if (numero == null) {
            return;
        }
        boolean duplicado = porAltKey.values().stream()
                .anyMatch(otro -> !otro.altKey().equals(producto.altKey())
                        && numero.equals(otro.numeroComoCadena()));
        if (duplicado) {
            throw new IllegalStateException("Numero de producto duplicado: " + numero);
        }
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private PaginaDominio<Producto> paginar(List<Producto> elementos, int pagina, int tamano) {
        int desde = Math.min(pagina * tamano, elementos.size());
        int hasta = Math.min(desde + tamano, elementos.size());
        return PaginaDominio.de(elementos.subList(desde, hasta), pagina, tamano, elementos.size());
    }
}
