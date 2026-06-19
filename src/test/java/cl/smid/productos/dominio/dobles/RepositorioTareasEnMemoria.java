package cl.smid.productos.dominio.dobles;

import cl.smid.productos.dominio.excepcion.RecursoNoEncontrado;
import cl.smid.productos.dominio.modelo.Tarea;
import cl.smid.productos.dominio.modelo.vista.PaginaDominio;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroTareas;
import cl.smid.productos.dominio.puerto.salida.RepositorioTareas;
import cl.smid.productos.dominio.servicio.EvaluadorAlcance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de Tareas en memoria para pruebas. Emula el filtrado territorial y por atributos, la
 * paginacion, el listado por producto y la unicidad de semilla (id_caso_alt, tipo_semilla).
 */
public class RepositorioTareasEnMemoria implements RepositorioTareas {

    private final Map<String, Tarea> porAltKey = new LinkedHashMap<>();
    private final EvaluadorAlcance alcance = new EvaluadorAlcance();

    @Override
    public void crear(Tarea tarea) {
        validarSemillaUnica(tarea);
        porAltKey.put(tarea.altKey(), tarea);
    }

    @Override
    public void actualizar(Tarea tarea) {
        if (!porAltKey.containsKey(tarea.altKey())) {
            throw new RecursoNoEncontrado("Tarea no encontrada: " + tarea.altKey());
        }
        porAltKey.put(tarea.altKey(), tarea);
    }

    @Override
    public Optional<Tarea> buscarPorAltKey(String altKey) {
        return Optional.ofNullable(porAltKey.get(altKey));
    }

    @Override
    public PaginaDominio<Tarea> listar(FiltroTareas filtro) {
        List<Tarea> filtrados = new ArrayList<>(porAltKey.values()).stream()
                .filter(t -> alcance.puedeVer(t.idSedeAlt(), t.idUnidadAlt(), filtro.contexto()))
                .filter(t -> vacio(filtro.idCasoAlt()) || filtro.idCasoAlt().equals(t.idCasoAlt()))
                .filter(t -> vacio(filtro.idProductoAlt()) || filtro.idProductoAlt().equals(t.idProductoAlt()))
                .filter(t -> vacio(filtro.responsableAlt()) || filtro.responsableAlt().equals(t.responsableAlt()))
                .filter(t -> filtro.estado() == null || filtro.estado() == t.estado())
                .toList();
        int desde = Math.min(filtro.pagina() * filtro.tamano(), filtrados.size());
        int hasta = Math.min(desde + filtro.tamano(), filtrados.size());
        return PaginaDominio.de(filtrados.subList(desde, hasta), filtro.pagina(), filtro.tamano(),
                filtrados.size());
    }

    @Override
    public List<Tarea> listarPorProducto(String idProductoAlt) {
        return porAltKey.values().stream()
                .filter(t -> idProductoAlt != null && idProductoAlt.equals(t.idProductoAlt()))
                .toList();
    }

    @Override
    public boolean existeSemilla(String idCasoAlt, String tipoSemilla) {
        return porAltKey.values().stream()
                .anyMatch(t -> idCasoAlt.equals(t.idCasoAlt()) && tipoSemilla.equals(t.tipoSemilla()));
    }

    private void validarSemillaUnica(Tarea tarea) {
        if (tarea.tipoSemilla() == null) {
            return;
        }
        if (existeSemilla(tarea.idCasoAlt(), tarea.tipoSemilla())) {
            throw new IllegalStateException("Semilla duplicada para el caso " + tarea.idCasoAlt());
        }
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
