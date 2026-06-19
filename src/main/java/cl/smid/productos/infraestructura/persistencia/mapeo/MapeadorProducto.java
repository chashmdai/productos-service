package cl.smid.productos.infraestructura.persistencia.mapeo;

import cl.smid.productos.dominio.modelo.EstadoProducto;
import cl.smid.productos.dominio.modelo.NumeroProducto;
import cl.smid.productos.dominio.modelo.Producto;
import cl.smid.productos.dominio.modelo.TipoProducto;
import cl.smid.productos.dominio.modelo.Transicion;
import cl.smid.productos.infraestructura.persistencia.entidad.ProductoEntidad;
import cl.smid.productos.infraestructura.persistencia.entidad.ProductoTransicionEntidad;

import java.util.List;

/**
 * Traduce entre el agregado de dominio {@link Producto} (POJO puro) y su entidad JPA
 * {@link ProductoEntidad}, asi como entre {@link Transicion} y {@link ProductoTransicionEntidad}.
 *
 * <p>Funciones puras y sin estado. Las marcas temporales se normalizan a UTC con
 * {@link ConversorTiempo}; los enum se serializan como su {@code name()}.</p>
 */
public final class MapeadorProducto {

    private MapeadorProducto() {
    }

    /**
     * Reconstituye el agregado a partir de la entidad y su historial ya cargado.
     *
     * @param entidad   fila de {@code producto}.
     * @param historial asientos previos (puede ser vacio si el caso de uso no los requiere).
     */
    public static Producto aDominio(ProductoEntidad entidad, List<Transicion> historial) {
        NumeroProducto numero = entidad.getNumeroProducto() == null
                ? null
                : new NumeroProducto(entidad.getNumeroProducto());
        return Producto.reconstituir(
                entidad.getAltKey(),
                entidad.getIdCasoAlt(),
                TipoProducto.valueOf(entidad.getTipo()),
                entidad.getTitulo(),
                entidad.getDescripcion(),
                EstadoProducto.valueOf(entidad.getEstado()),
                numero,
                entidad.getIdSedeAlt(),
                entidad.getIdUnidadAlt(),
                entidad.getAutorAlt(),
                entidad.isVigente(),
                ConversorTiempo.aInstant(entidad.getCreadoEn()),
                ConversorTiempo.aInstant(entidad.getActualizadoEn()),
                ConversorTiempo.aInstant(entidad.getEmitidoEn()),
                ConversorTiempo.aInstant(entidad.getAnuladoEn()),
                historial);
    }

    /** Construye una entidad nueva (sin {@code id}) a partir del agregado recien creado. */
    public static ProductoEntidad aEntidadNueva(Producto producto) {
        ProductoEntidad e = new ProductoEntidad();
        e.setAltKey(producto.altKey());
        e.setIdCasoAlt(producto.idCasoAlt());
        e.setTipo(producto.tipo().name());
        e.setTitulo(producto.titulo());
        e.setDescripcion(producto.descripcion());
        e.setEstado(producto.estado().name());
        e.setNumeroProducto(producto.numeroComoCadena());
        e.setIdSedeAlt(producto.idSedeAlt());
        e.setIdUnidadAlt(producto.idUnidadAlt());
        e.setAutorAlt(producto.autorAlt());
        e.setVigente(producto.vigente());
        e.setCreadoEn(ConversorTiempo.aLocalDateTime(producto.creadoEn()));
        e.setActualizadoEn(ConversorTiempo.aLocalDateTime(producto.actualizadoEn()));
        e.setEmitidoEn(ConversorTiempo.aLocalDateTime(producto.emitidoEn()));
        e.setAnuladoEn(ConversorTiempo.aLocalDateTime(producto.anuladoEn()));
        return e;
    }

    /**
     * Vuelca sobre la entidad gestionada los campos mutables del agregado (estado, marcas y
     * numero). Los campos inmutables (altKey, caso, tipo, sede, unidad, autor, creacion) no se
     * tocan.
     */
    public static void copiarADestino(Producto producto, ProductoEntidad destino) {
        destino.setTitulo(producto.titulo());
        destino.setDescripcion(producto.descripcion());
        destino.setEstado(producto.estado().name());
        destino.setNumeroProducto(producto.numeroComoCadena());
        destino.setVigente(producto.vigente());
        destino.setActualizadoEn(ConversorTiempo.aLocalDateTime(producto.actualizadoEn()));
        destino.setEmitidoEn(ConversorTiempo.aLocalDateTime(producto.emitidoEn()));
        destino.setAnuladoEn(ConversorTiempo.aLocalDateTime(producto.anuladoEn()));
    }

    /** Convierte una transicion de dominio en su entidad, ligada al Producto por su PK interna. */
    public static ProductoTransicionEntidad transicionAEntidad(Transicion t, Long productoId) {
        ProductoTransicionEntidad e = new ProductoTransicionEntidad();
        e.setProductoId(productoId);
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
    public static Transicion transicionADominio(ProductoTransicionEntidad e) {
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
