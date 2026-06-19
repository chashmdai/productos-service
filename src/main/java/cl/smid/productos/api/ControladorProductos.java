package cl.smid.productos.api;

import cl.smid.productos.api.dto.CrearProductoRequest;
import cl.smid.productos.api.dto.CrearTareaBajoProductoRequest;
import cl.smid.productos.api.dto.EditarProductoRequest;
import cl.smid.productos.api.dto.PaginaResponse;
import cl.smid.productos.api.dto.ProductoResponse;
import cl.smid.productos.api.dto.TareaResponse;
import cl.smid.productos.api.dto.TransicionProductoRequest;
import cl.smid.productos.api.mapper.MapeadorRespuesta;
import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.modelo.EstadoProducto;
import cl.smid.productos.dominio.modelo.Tarea;
import cl.smid.productos.dominio.modelo.TipoProducto;
import cl.smid.productos.dominio.modelo.vista.DetalleProducto;
import cl.smid.productos.dominio.puerto.entrada.GestionProductos;
import cl.smid.productos.dominio.puerto.entrada.GestionTareas;
import cl.smid.productos.dominio.puerto.entrada.comando.CrearProductoComando;
import cl.smid.productos.dominio.puerto.entrada.comando.CrearTareaComando;
import cl.smid.productos.dominio.puerto.entrada.comando.EditarProductoComando;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroProductos;
import cl.smid.productos.dominio.puerto.entrada.comando.TransicionProductoComando;
import cl.smid.productos.infraestructura.seguridad.ProveedorContexto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

/**
 * Controlador REST de Productos (y de la creacion de tareas bajo un Producto). Tras el
 * {@code StripPrefix=1} del gateway, las rutas externas {@code /api/productos/productos/**} llegan
 * aqui como {@code /productos/productos/**}.
 *
 * <p>La frontera transaccional vive en este controlador: las operaciones de escritura son
 * {@code @Transactional} (de modo que el correlativo JDBC y las inserciones participen en la misma
 * transaccion); las lecturas son transacciones de solo lectura. El contexto de sesion se obtiene
 * del {@link ProveedorContexto}.</p>
 */
@RestController
@RequestMapping("/productos/productos")
public class ControladorProductos {

    private static final int TAMANO_DEFECTO = 20;
    private static final int TAMANO_MAXIMO = 200;

    private final GestionProductos gestionProductos;
    private final GestionTareas gestionTareas;
    private final ProveedorContexto proveedorContexto;

    public ControladorProductos(GestionProductos gestionProductos,
                                GestionTareas gestionTareas,
                                ProveedorContexto proveedorContexto) {
        this.gestionProductos = gestionProductos;
        this.gestionTareas = gestionTareas;
        this.proveedorContexto = proveedorContexto;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ProductoResponse crear(@Valid @RequestBody CrearProductoRequest peticion) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        DetalleProducto detalle = gestionProductos.crear(new CrearProductoComando(
                ctx, peticion.idCaso(), peticion.tipo(), peticion.titulo(), peticion.descripcion()));
        return MapeadorRespuesta.aDetalle(detalle);
    }

    @GetMapping("/{altKey}")
    @Transactional(readOnly = true)
    public ProductoResponse detalle(@PathVariable String altKey) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        return MapeadorRespuesta.aDetalle(gestionProductos.detalle(ctx, altKey));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public PaginaResponse<ProductoResponse> listar(
            @RequestParam(required = false) String idCaso,
            @RequestParam(required = false) EstadoProducto estado,
            @RequestParam(required = false) TipoProducto tipo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        FiltroProductos filtro = new FiltroProductos(ctx, idCaso, estado, tipo,
                normalizarPagina(pagina), normalizarTamano(tamano));
        return PaginaResponse.de(gestionProductos.listar(filtro), MapeadorRespuesta::aResumen);
    }

    @RequestMapping(value = "/{altKey}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @Transactional
    public ProductoResponse editar(@PathVariable String altKey,
                                   @Valid @RequestBody EditarProductoRequest peticion) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        DetalleProducto detalle = gestionProductos.editar(new EditarProductoComando(
                ctx, altKey, peticion.titulo(), peticion.descripcion()));
        return MapeadorRespuesta.aDetalle(detalle);
    }

    @PostMapping("/{altKey}/transiciones")
    @Transactional
    public ProductoResponse transicionar(@PathVariable String altKey,
                                         @Valid @RequestBody TransicionProductoRequest peticion) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        DetalleProducto detalle = gestionProductos.transicionar(new TransicionProductoComando(
                ctx, altKey, peticion.accion(), peticion.observacion()));
        return MapeadorRespuesta.aDetalle(detalle);
    }

    @PostMapping("/{altKey}/tareas")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public TareaResponse crearTareaBajoProducto(@PathVariable String altKey,
                                                @Valid @RequestBody CrearTareaBajoProductoRequest peticion) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        Tarea tarea = gestionTareas.crearBajoProducto(new CrearTareaComando(
                ctx, null, altKey, peticion.titulo(), peticion.descripcion(),
                peticion.responsableAlt(), peticion.prioridad(), peticion.fechaVencimiento()));
        return MapeadorRespuesta.aTarea(tarea, false);
    }

    private int normalizarPagina(int pagina) {
        return Math.max(pagina, 0);
    }

    private int normalizarTamano(int tamano) {
        if (tamano < 1) {
            return TAMANO_DEFECTO;
        }
        return Math.min(tamano, TAMANO_MAXIMO);
    }
}
