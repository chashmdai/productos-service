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
    public TareaResponse crearSuelta(@Valid @RequestBody CrearTareaSueltaRequest peticion) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        Tarea tarea = gestionTareas.crearSuelta(new CrearTareaComando(
                ctx, peticion.idCaso(), null, peticion.titulo(), peticion.descripcion(),
                peticion.responsableAlt(), peticion.prioridad(), peticion.fechaVencimiento()));
        return MapeadorRespuesta.aTarea(tarea, false);
    }

    @GetMapping("/{altKey}")
    @Transactional(readOnly = true)
    public TareaResponse detalle(@PathVariable String altKey) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        return MapeadorRespuesta.aTarea(gestionTareas.detalleTarea(ctx, altKey), true);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public PaginaResponse<TareaResponse> listar(
            @RequestParam(required = false) String idCaso,
            @RequestParam(required = false) String idProducto,
            @RequestParam(required = false) String responsable,
            @RequestParam(required = false) EstadoTarea estado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        ContextoSesion ctx = proveedorContexto.contextoActual();
        FiltroTareas filtro = new FiltroTareas(ctx, idCaso, idProducto, responsable, estado,
                normalizarPagina(pagina), normalizarTamano(tamano));
        return PaginaResponse.de(gestionTareas.listar(filtro), t -> MapeadorRespuesta.aTarea(t, false));
    }

    @PostMapping("/{altKey}/transiciones")
    @Transactional
    public TareaResponse transicionar(@PathVariable String altKey,
                                      @Valid @RequestBody TransicionTareaRequest peticion) {
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
