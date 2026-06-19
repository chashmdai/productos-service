package cl.smid.productos.infraestructura.persistencia.mapeo;

import cl.smid.productos.dominio.modelo.EstadoTarea;
import cl.smid.productos.dominio.modelo.PrioridadTarea;
import cl.smid.productos.dominio.modelo.Tarea;
import cl.smid.productos.dominio.modelo.Transicion;
import cl.smid.productos.infraestructura.persistencia.entidad.TareaEntidad;
import cl.smid.productos.infraestructura.persistencia.entidad.TareaTransicionEntidad;

import java.util.List;

/**
 * Traduce entre el agregado {@link Tarea} y su entidad {@link TareaEntidad}, y entre
 * {@link Transicion} y {@link TareaTransicionEntidad}. Funciones puras y sin estado.
 */
public final class MapeadorTarea {

    private MapeadorTarea() {
    }

    /** Reconstituye el agregado a partir de la entidad y su historial ya cargado. */
    public static Tarea aDominio(TareaEntidad entidad, List<Transicion> historial) {
        return Tarea.reconstituir(
                entidad.getAltKey(),
                entidad.getIdCasoAlt(),
                entidad.getIdProductoAlt(),
                entidad.getTitulo(),
                entidad.getDescripcion(),
                entidad.getResponsableAlt(),
                entidad.getResponsableNombre(),
                PrioridadTarea.valueOf(entidad.getPrioridad()),
                EstadoTarea.valueOf(entidad.getEstado()),
                entidad.getFechaVencimiento(),
                entidad.getIdSedeAlt(),
                entidad.getIdUnidadAlt(),
                entidad.getTipoSemilla(),
                ConversorTiempo.aInstant(entidad.getCreadoEn()),
                ConversorTiempo.aInstant(entidad.getActualizadoEn()),
                ConversorTiempo.aInstant(entidad.getCompletadoEn()),
                historial);
    }

    /** Construye una entidad nueva (sin {@code id}) a partir del agregado recien creado. */
    public static TareaEntidad aEntidadNueva(Tarea tarea) {
        TareaEntidad e = new TareaEntidad();
        e.setAltKey(tarea.altKey());
        e.setIdCasoAlt(tarea.idCasoAlt());
        e.setIdProductoAlt(tarea.idProductoAlt());
        e.setTitulo(tarea.titulo());
        e.setDescripcion(tarea.descripcion());
        e.setResponsableAlt(tarea.responsableAlt());
        e.setResponsableNombre(tarea.responsableNombre());
        e.setPrioridad(tarea.prioridad().name());
        e.setEstado(tarea.estado().name());
        e.setFechaVencimiento(tarea.fechaVencimiento());
        e.setIdSedeAlt(tarea.idSedeAlt());
        e.setIdUnidadAlt(tarea.idUnidadAlt());
        e.setTipoSemilla(tarea.tipoSemilla());
        e.setCreadoEn(ConversorTiempo.aLocalDateTime(tarea.creadoEn()));
        e.setActualizadoEn(ConversorTiempo.aLocalDateTime(tarea.actualizadoEn()));
        e.setCompletadoEn(ConversorTiempo.aLocalDateTime(tarea.completadoEn()));
        return e;
    }

    /**
     * Vuelca sobre la entidad gestionada los campos mutables del agregado: responsable y su
     * snapshot, estado y marcas. Los inmutables (altKey, caso, producto, sede, unidad, prioridad,
     * vencimiento, creacion, tipoSemilla) no se tocan.
     */
    public static void copiarADestino(Tarea tarea, TareaEntidad destino) {
        destino.setResponsableAlt(tarea.responsableAlt());
        destino.setResponsableNombre(tarea.responsableNombre());
        destino.setEstado(tarea.estado().name());
        destino.setActualizadoEn(ConversorTiempo.aLocalDateTime(tarea.actualizadoEn()));
        destino.setCompletadoEn(ConversorTiempo.aLocalDateTime(tarea.completadoEn()));
    }

    /** Convierte una transicion de dominio en su entidad, ligada a la Tarea por su PK interna. */
    public static TareaTransicionEntidad transicionAEntidad(Transicion t, Long tareaId) {
        TareaTransicionEntidad e = new TareaTransicionEntidad();
        e.setTareaId(tareaId);
        e.setAltKey(t.altKey());
        e.setEstadoOrigen(t.estadoOrigen());
        e.setEstadoDestino(t.estadoDestino());
        e.setAccion(t.accion());
        e.setObservacion(t.observacion());
        e.setActor(t.actor());
        e.setOcurridoEn(ConversorTiempo.aLocalDateTime(t.ocurridoEn()));
        return e;
    }

    /** Convierte un asiento persistido en su transicion de dominio. */
    public static Transicion transicionADominio(TareaTransicionEntidad e) {
        return new Transicion(
                e.getAltKey(),
                e.getEstadoOrigen(),
                e.getEstadoDestino(),
                e.getAccion(),
                e.getObservacion(),
                e.getActor(),
                ConversorTiempo.aInstant(e.getOcurridoEn()));
    }
}
