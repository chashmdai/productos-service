package cl.smid.productos.api.mapper;

import cl.smid.productos.api.dto.ProductoResponse;
import cl.smid.productos.api.dto.TareaResponse;
import cl.smid.productos.api.dto.TransicionResponse;
import cl.smid.productos.dominio.modelo.Producto;
import cl.smid.productos.dominio.modelo.Tarea;
import cl.smid.productos.dominio.modelo.Transicion;
import cl.smid.productos.dominio.modelo.vista.DetalleProducto;

import java.util.List;

/**
 * Traduce los agregados y vistas de dominio a los DTOs de respuesta de la API. Los enum se
 * exponen como texto y los identificadores son siempre opacos. El historial y las tareas embebidas
 * se incluyen solo donde corresponde; en los listados se omiten (nulos) para aligerar la salida.
 */
public final class MapeadorRespuesta {

    private MapeadorRespuesta() {
    }

    /** Detalle de Producto con sus tareas (sin historial por tarea) y su historial. */
    public static ProductoResponse aDetalle(DetalleProducto detalle) {
        Producto p = detalle.producto();
        List<TareaResponse> tareas = detalle.tareas().stream()
                .map(t -> aTarea(t, false))
                .toList();
        return construir(p, tareas, aTransiciones(p.historial()));
    }

    /** Resumen de Producto para listados: sin tareas ni historial. */
    public static ProductoResponse aResumen(Producto p) {
        return construir(p, null, null);
    }

    /** Tarea individual; incluye historial si se solicita y existe. */
    public static TareaResponse aTarea(Tarea t, boolean incluirHistorial) {
        List<TransicionResponse> historial =
                (incluirHistorial && !t.historial().isEmpty()) ? aTransiciones(t.historial()) : null;
        return new TareaResponse(
                t.altKey(),
                t.idCasoAlt(),
                t.idProductoAlt(),
                t.titulo(),
                t.descripcion(),
                t.responsableAlt(),
                t.responsableNombre(),
                t.prioridad().name(),
                t.estado().name(),
                t.fechaVencimiento(),
                t.idSedeAlt(),
                t.idUnidadAlt(),
                t.creadoEn(),
                t.actualizadoEn(),
                t.completadoEn(),
                historial);
    }

    private static ProductoResponse construir(Producto p, List<TareaResponse> tareas,
                                              List<TransicionResponse> historial) {
        return new ProductoResponse(
                p.altKey(),
                p.idCasoAlt(),
                p.tipo().name(),
                p.titulo(),
                p.descripcion(),
                p.estado().name(),
                p.numeroComoCadena(),
                p.idSedeAlt(),
                p.idUnidadAlt(),
                p.autorAlt(),
                p.vigente(),
                p.creadoEn(),
                p.actualizadoEn(),
                p.emitidoEn(),
                p.anuladoEn(),
                tareas,
                historial);
    }

    private static List<TransicionResponse> aTransiciones(List<Transicion> historial) {
        return historial.stream().map(MapeadorRespuesta::aTransicion).toList();
    }

    private static TransicionResponse aTransicion(Transicion t) {
        return new TransicionResponse(
                t.altKey(),
                t.estadoOrigen(),
                t.estadoDestino(),
                t.accion(),
                t.observacion(),
                t.actor(),
                t.ocurridoEn());
    }
}
