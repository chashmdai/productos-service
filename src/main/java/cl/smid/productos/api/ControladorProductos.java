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
import cl.smid.productos.infraestructura.web.SobreError;
import cl.smid.productos.infraestructura.seguridad.ProveedorContexto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Productos", description = "Productos, instrumentos y entregables asociados a casos.")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Crea un producto",
            description = "Crea un producto en estado BORRADOR asociado a un caso. La sede y unidad se heredan del caso o del contexto.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "PRD-001 - Validación de entrada",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "404", description = "PRD-404 - Recurso no encontrado o fuera de alcance",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "422", description = "PRD-422 - Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "500", description = "PRD-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = SobreError.class)))
    })
    public ProductoResponse crear(@io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = CrearProductoRequest.class),
                    examples = @ExampleObject("""
                            {
                              "idCaso": "7d6b3c3c-9d22-4b10-8d3d-1f3900c13f20",
                              "tipo": "INFORME",
                              "titulo": "Informe técnico sintético",
                              "descripcion": "Descripción sintética del entregable."
                            }
                            """)))
                                  @Valid @org.springframework.web.bind.annotation.RequestBody CrearProductoRequest peticion) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        DetalleProducto detalle = gestionProductos.crear(new CrearProductoComando(
                ctx, peticion.idCaso(), peticion.tipo(), peticion.titulo(), peticion.descripcion()));
        return MapeadorRespuesta.aDetalle(detalle);
    }

    @GetMapping("/{altKey}")
    @Transactional(readOnly = true)
    @Operation(summary = "Obtiene el detalle de un producto",
            description = "Retorna el producto con tareas e historial. Si está fuera de alcance territorial responde PRD-404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "404", description = "PRD-404 - No encontrado o fuera de alcance territorial",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "500", description = "PRD-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = SobreError.class)))
    })
    public ProductoResponse detalle(@Parameter(description = "Identificador opaco del producto.",
                                            example = "b2d9d6ec-f0de-4df7-aee6-3f7106e7da54")
                                    @PathVariable String altKey) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        return MapeadorRespuesta.aDetalle(gestionProductos.detalle(ctx, altKey));
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Lista productos paginados",
            description = "Lista productos dentro del alcance territorial con filtros por caso, estado y tipo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de productos",
                    content = @Content(schema = @Schema(implementation = PaginaResponse.class))),
            @ApiResponse(responseCode = "400", description = "PRD-001 - Parámetros inválidos",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "500", description = "PRD-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = SobreError.class)))
    })
    public PaginaResponse<ProductoResponse> listar(
            @Parameter(description = "Identificador opaco del caso.", example = "7d6b3c3c-9d22-4b10-8d3d-1f3900c13f20")
            @RequestParam(required = false) String idCaso,
            @Parameter(description = "Estado del producto.", schema = @Schema(allowableValues = {
                    "BORRADOR", "EN_REVISION", "EMITIDO", "ANULADO"}), example = "BORRADOR")
            @RequestParam(required = false) EstadoProducto estado,
            @Parameter(description = "Tipo de producto.", schema = @Schema(allowableValues = {
                    "INFORME", "GESTION", "OFICIO", "DERIVACION", "RESOLUCION", "OTRO"}), example = "INFORME")
            @RequestParam(required = false) TipoProducto tipo,
            @Parameter(description = "Número de página, base cero.", example = "0")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Tamaño de página, máximo 200.", example = "20")
            @RequestParam(defaultValue = "20") int tamano) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        FiltroProductos filtro = new FiltroProductos(ctx, idCaso, estado, tipo,
                normalizarPagina(pagina), normalizarTamano(tamano));
        return PaginaResponse.de(gestionProductos.listar(filtro), MapeadorRespuesta::aResumen);
    }

    @RequestMapping(value = "/{altKey}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @Transactional
    @Operation(summary = "Edita parcialmente un producto",
            description = "Actualiza título y/o descripción. Solo aplica en BORRADOR o EN_REVISION; otros estados responden PRD-409.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto editado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "PRD-001 - Validación de entrada",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "404", description = "PRD-404 - No encontrado o fuera de alcance territorial",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "409", description = "PRD-409 - Producto no mutable",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "500", description = "PRD-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = SobreError.class)))
    })
    public ProductoResponse editar(@Parameter(description = "Identificador opaco del producto.",
                                           example = "b2d9d6ec-f0de-4df7-aee6-3f7106e7da54")
                                   @PathVariable String altKey,
                                   @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                                           content = @Content(schema = @Schema(implementation = EditarProductoRequest.class),
                                                   examples = @ExampleObject("""
                                                           {
                                                             "titulo": "Informe técnico actualizado",
                                                             "descripcion": "Descripción sintética actualizada."
                                                           }
                                                           """)))
                                   @Valid @org.springframework.web.bind.annotation.RequestBody EditarProductoRequest peticion) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        DetalleProducto detalle = gestionProductos.editar(new EditarProductoComando(
                ctx, altKey, peticion.titulo(), peticion.descripcion()));
        return MapeadorRespuesta.aDetalle(detalle);
    }

    @PostMapping("/{altKey}/transiciones")
    @Transactional
    @Operation(summary = "Transiciona un producto",
            description = "Acciones: ENVIAR_REVISION, EMITIR, ANULAR. EMITIR y ANULAR requieren rol de Coordinación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto transicionado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "PRD-001 - Validación de entrada",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "403", description = "AUTZ-004 - Falta rol de Coordinación",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "404", description = "PRD-404 - No encontrado o fuera de alcance territorial",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "409", description = "PRD-409 - Transición inválida",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "422", description = "PRD-422 - Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "500", description = "PRD-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = SobreError.class)))
    })
    public ProductoResponse transicionar(@Parameter(description = "Identificador opaco del producto.",
                                                 example = "b2d9d6ec-f0de-4df7-aee6-3f7106e7da54")
                                         @PathVariable String altKey,
                                         @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                                                 content = @Content(schema = @Schema(implementation = TransicionProductoRequest.class),
                                                         examples = @ExampleObject("""
                                                                 {
                                                                   "accion": "ENVIAR_REVISION",
                                                                   "observacion": "Envío a revisión sintético."
                                                                 }
                                                                 """)))
                                         @Valid @org.springframework.web.bind.annotation.RequestBody TransicionProductoRequest peticion) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        DetalleProducto detalle = gestionProductos.transicionar(new TransicionProductoComando(
                ctx, altKey, peticion.accion(), peticion.observacion()));
        return MapeadorRespuesta.aDetalle(detalle);
    }

    @PostMapping("/{altKey}/tareas")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "Crea una tarea bajo un producto",
            description = "Crea una tarea hija heredando caso, sede y unidad del producto padre.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarea creada",
                    content = @Content(schema = @Schema(implementation = TareaResponse.class))),
            @ApiResponse(responseCode = "400", description = "PRD-001 - Validación de entrada",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "404", description = "PRD-404 - Producto no encontrado o fuera de alcance",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "409", description = "PRD-409 - Producto no admite alta de tareas",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "422", description = "PRD-422 - Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "500", description = "PRD-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = SobreError.class)))
    })
    public TareaResponse crearTareaBajoProducto(@Parameter(description = "Identificador opaco del producto.",
                                                        example = "b2d9d6ec-f0de-4df7-aee6-3f7106e7da54")
                                                @PathVariable String altKey,
                                                @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                                                        content = @Content(schema = @Schema(implementation = CrearTareaBajoProductoRequest.class),
                                                                examples = @ExampleObject("""
                                                                        {
                                                                          "titulo": "Revisar antecedentes sintéticos",
                                                                          "descripcion": "Descripción operativa sintética.",
                                                                          "responsableAlt": "c27f4500-f412-4fd1-86a8-6caa5933583b",
                                                                          "prioridad": "MEDIA",
                                                                          "fechaVencimiento": "2027-03-30"
                                                                        }
                                                                        """)))
                                                @Valid @org.springframework.web.bind.annotation.RequestBody CrearTareaBajoProductoRequest peticion) {
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
