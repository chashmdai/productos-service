package cl.smid.productos.api;

import cl.smid.productos.api.dto.CrearTareaSueltaRequest;
import cl.smid.productos.api.dto.PaginaResponse;
import cl.smid.productos.api.dto.TareaResponse;
import cl.smid.productos.api.dto.TransicionTareaRequest;
import cl.smid.productos.api.mapper.MapeadorRespuesta;
import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.modelo.EstadoTarea;
import cl.smid.productos.dominio.modelo.Tarea;
import cl.smid.productos.dominio.puerto.entrada.GestionTareas;
import cl.smid.productos.dominio.puerto.entrada.comando.CrearTareaComando;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroTareas;
import cl.smid.productos.dominio.puerto.entrada.comando.TransicionTareaComando;
import cl.smid.productos.infraestructura.seguridad.ProveedorContexto;
import cl.smid.productos.infraestructura.web.SobreError;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de Tareas sueltas (ligadas a un Caso) y de las operaciones de ciclo de vida de
 * cualquier Tarea. Tras el {@code StripPrefix=1}, las rutas externas
 * {@code /api/productos/tareas/**} llegan como {@code /productos/tareas/**}.
 *
 * <p>Misma politica transaccional que {@link ControladorProductos}: escrituras transaccionales,
 * lecturas de solo lectura, contexto via {@link ProveedorContexto}. El detalle individual incluye
 * el historial de la Tarea.</p>
 */
@RestController
@RequestMapping("/productos/tareas")
@Tag(name = "Tareas", description = "Tareas asociadas a casos o productos.")
@SecurityRequirement(name = "bearerAuth")
public class ControladorTareas {

    private static final int TAMANO_DEFECTO = 20;
    private static final int TAMANO_MAXIMO = 200;

    private final GestionTareas gestionTareas;
    private final ProveedorContexto proveedorContexto;

    public ControladorTareas(GestionTareas gestionTareas, ProveedorContexto proveedorContexto) {
        this.gestionTareas = gestionTareas;
        this.proveedorContexto = proveedorContexto;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "Crea una tarea suelta",
            description = "Crea una tarea ligada directamente a un caso. La sede y unidad se heredan del caso o del contexto.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarea creada",
                    content = @Content(schema = @Schema(implementation = TareaResponse.class))),
            @ApiResponse(responseCode = "400", description = "PRD-001 - Validación de entrada",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "422", description = "PRD-422 - Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "500", description = "PRD-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = SobreError.class)))
    })
    public TareaResponse crearSuelta(@io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = CrearTareaSueltaRequest.class),
                    examples = @ExampleObject("""
                            {
                              "idCaso": "7d6b3c3c-9d22-4b10-8d3d-1f3900c13f20",
                              "titulo": "Solicitar antecedente sintético",
                              "descripcion": "Descripción operativa sintética.",
                              "responsableAlt": "c27f4500-f412-4fd1-86a8-6caa5933583b",
                              "prioridad": "ALTA",
                              "fechaVencimiento": "2027-03-30"
                            }
                            """)))
                                      @Valid @org.springframework.web.bind.annotation.RequestBody CrearTareaSueltaRequest peticion) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        Tarea tarea = gestionTareas.crearSuelta(new CrearTareaComando(
                ctx, peticion.idCaso(), null, peticion.titulo(), peticion.descripcion(),
                peticion.responsableAlt(), peticion.prioridad(), peticion.fechaVencimiento()));
        return MapeadorRespuesta.aTarea(tarea, false);
    }

    @GetMapping("/{altKey}")
    @Transactional(readOnly = true)
    @Operation(summary = "Obtiene el detalle de una tarea",
            description = "Retorna una tarea con historial. Si está fuera de alcance territorial responde PRD-404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea encontrada",
                    content = @Content(schema = @Schema(implementation = TareaResponse.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "404", description = "PRD-404 - No encontrada o fuera de alcance territorial",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "500", description = "PRD-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = SobreError.class)))
    })
    public TareaResponse detalle(@Parameter(description = "Identificador opaco de la tarea.",
                                         example = "31abf51b-badf-4a3d-b2a7-1a37089e1370")
                                 @PathVariable String altKey) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        return MapeadorRespuesta.aTarea(gestionTareas.detalleTarea(ctx, altKey), true);
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Lista tareas paginadas",
            description = "Lista tareas dentro del alcance territorial con filtros por caso, producto, responsable y estado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de tareas",
                    content = @Content(schema = @Schema(implementation = PaginaResponse.class))),
            @ApiResponse(responseCode = "400", description = "PRD-001 - Parámetros inválidos",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "500", description = "PRD-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = SobreError.class)))
    })
    public PaginaResponse<TareaResponse> listar(
            @Parameter(description = "Identificador opaco del caso.", example = "7d6b3c3c-9d22-4b10-8d3d-1f3900c13f20")
            @RequestParam(required = false) String idCaso,
            @Parameter(description = "Identificador opaco del producto padre.", example = "b2d9d6ec-f0de-4df7-aee6-3f7106e7da54")
            @RequestParam(required = false) String idProducto,
            @Parameter(description = "Identificador opaco del responsable.", example = "c27f4500-f412-4fd1-86a8-6caa5933583b")
            @RequestParam(required = false) String responsable,
            @Parameter(description = "Estado de la tarea.", schema = @Schema(allowableValues = {
                    "PENDIENTE", "EN_CURSO", "COMPLETADA", "CANCELADA"}), example = "PENDIENTE")
            @RequestParam(required = false) EstadoTarea estado,
            @Parameter(description = "Número de página, base cero.", example = "0")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Tamaño de página, máximo 200.", example = "20")
            @RequestParam(defaultValue = "20") int tamano) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        FiltroTareas filtro = new FiltroTareas(ctx, idCaso, idProducto, responsable, estado,
                normalizarPagina(pagina), normalizarTamano(tamano));
        return PaginaResponse.de(gestionTareas.listar(filtro), t -> MapeadorRespuesta.aTarea(t, false));
    }

    @PostMapping("/{altKey}/transiciones")
    @Transactional
    @Operation(summary = "Transiciona una tarea",
            description = """
                    Acciones: TOMAR, COMPLETAR, CANCELAR, REASIGNAR.
                    Puede operar Coordinación o el responsable actual. Una tarea sin responsable solo la opera Coordinación.
                    REASIGNAR requiere responsableAlt y no cambia el estado.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea transicionada",
                    content = @Content(schema = @Schema(implementation = TareaResponse.class))),
            @ApiResponse(responseCode = "400", description = "PRD-001 - Validación de entrada",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "403", description = "AUTZ-004 - Falta rol o permiso de responsable",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "404", description = "PRD-404 - No encontrada o fuera de alcance territorial",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "409", description = "PRD-409 - Transición inválida",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "422", description = "PRD-422 - Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = SobreError.class))),
            @ApiResponse(responseCode = "500", description = "PRD-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = SobreError.class)))
    })
    public TareaResponse transicionar(@Parameter(description = "Identificador opaco de la tarea.",
                                              example = "31abf51b-badf-4a3d-b2a7-1a37089e1370")
                                      @PathVariable String altKey,
                                      @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                                              content = @Content(schema = @Schema(implementation = TransicionTareaRequest.class),
                                                      examples = @ExampleObject("""
                                                              {
                                                                "accion": "REASIGNAR",
                                                                "responsableAlt": "c27f4500-f412-4fd1-86a8-6caa5933583b",
                                                                "observacion": "Reasignación sintética."
                                                              }
                                                              """)))
                                      @Valid @org.springframework.web.bind.annotation.RequestBody TransicionTareaRequest peticion) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        Tarea tarea = gestionTareas.transicionar(new TransicionTareaComando(
                ctx, altKey, peticion.accion(), peticion.responsableAlt(), peticion.observacion()));
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
