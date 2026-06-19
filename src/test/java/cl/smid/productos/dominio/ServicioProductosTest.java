package cl.smid.productos.dominio;

import cl.smid.productos.dominio.dobles.ClienteCasosDoble;
import cl.smid.productos.dominio.dobles.ClientePersonasDoble;
import cl.smid.productos.dominio.dobles.CorrelativoEnMemoria;
import cl.smid.productos.dominio.dobles.DirectorioSedesFijo;
import cl.smid.productos.dominio.dobles.GeneradorSecuencial;
import cl.smid.productos.dominio.dobles.PublicadorCapturador;
import cl.smid.productos.dominio.dobles.RelojFijo;
import cl.smid.productos.dominio.dobles.RepositorioProductosEnMemoria;
import cl.smid.productos.dominio.dobles.RepositorioTareasEnMemoria;
import cl.smid.productos.dominio.excepcion.ConflictoEstado;
import cl.smid.productos.dominio.excepcion.ErrorAutorizacion;
import cl.smid.productos.dominio.excepcion.RecursoNoEncontrado;
import cl.smid.productos.dominio.excepcion.ReglaNegocio;
import cl.smid.productos.dominio.modelo.Alcance;
import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.modelo.EstadoProducto;
import cl.smid.productos.dominio.modelo.EstadoTarea;
import cl.smid.productos.dominio.modelo.PrioridadTarea;
import cl.smid.productos.dominio.modelo.ResumenCaso;
import cl.smid.productos.dominio.modelo.Tarea;
import cl.smid.productos.dominio.modelo.TipoProducto;
import cl.smid.productos.dominio.modelo.vista.DetalleProducto;
import cl.smid.productos.dominio.puerto.entrada.comando.CrearProductoComando;
import cl.smid.productos.dominio.puerto.entrada.comando.CrearTareaComando;
import cl.smid.productos.dominio.puerto.entrada.comando.EditarProductoComando;
import cl.smid.productos.dominio.puerto.entrada.comando.TransicionProductoComando;
import cl.smid.productos.dominio.puerto.entrada.comando.TransicionTareaComando;
import cl.smid.productos.dominio.servicio.EvaluadorAlcance;
import cl.smid.productos.dominio.servicio.GeneradorNumeroProducto;
import cl.smid.productos.dominio.servicio.MaquinaEstadosProducto;
import cl.smid.productos.dominio.servicio.MaquinaEstadosTarea;
import cl.smid.productos.dominio.servicio.ServicioProductos;
import cl.smid.productos.dominio.modelo.AccionProducto;
import cl.smid.productos.dominio.modelo.AccionTarea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Orquestador de dominio: Productos y Tareas")
class ServicioProductosTest {

    private static final Instant AHORA = Instant.parse("2027-03-15T12:00:00Z");

    private RepositorioProductosEnMemoria repoProductos;
    private RepositorioTareasEnMemoria repoTareas;
    private PublicadorCapturador publicador;
    private ServicioProductos servicio;

    private final ContextoSesion operador = ctx("user-op", "OPERADOR", "sede-1", "unidad-1");
    private final ContextoSesion operadorZ = ctx("user-z", "OPERADOR", "sede-1", "unidad-1");
    private final ContextoSesion coordinador = ctx("user-coord", "COORDINACION", "sede-1", "unidad-1");
    private final ContextoSesion otraSede = ctx("user-2", "OPERADOR", "sede-2", "unidad-9");

    @BeforeEach
    void preparar() {
        servicio = construir(false, new ClienteCasosDoble(), new ClientePersonasDoble());
    }

    private ServicioProductos construir(boolean enriquecimientoCasos, ClienteCasosDoble casos,
                                        ClientePersonasDoble personas) {
        repoProductos = new RepositorioProductosEnMemoria();
        repoTareas = new RepositorioTareasEnMemoria();
        publicador = new PublicadorCapturador();
        GeneradorNumeroProducto generadorNumero =
                new GeneradorNumeroProducto(new CorrelativoEnMemoria(), new DirectorioSedesFijo("RM"));
        return new ServicioProductos(
                repoProductos,
                repoTareas,
                new MaquinaEstadosProducto(),
                new MaquinaEstadosTarea(),
                generadorNumero,
                new EvaluadorAlcance(),
                publicador,
                new RelojFijo(AHORA),
                new GeneradorSecuencial(),
                casos,
                personas,
                Set.of("COORDINACION"),
                enriquecimientoCasos);
    }

    private static ContextoSesion ctx(String sub, String rol, String sede, String unidad) {
        return new ContextoSesion(sub, List.of(rol), sede, unidad, Alcance.SEDE, "Usuario " + sub);
    }

    private DetalleProducto crearProductoBase(ContextoSesion ctx) {
        return servicio.crear(new CrearProductoComando(
                ctx, "caso-1", TipoProducto.INFORME, "Informe inicial", "Detalle"));
    }

    private DetalleProducto llevarAEnRevision(String altKey, ContextoSesion ctx) {
        return servicio.transicionar(new TransicionProductoComando(
                ctx, altKey, AccionProducto.ENVIAR_REVISION, null));
    }

    // ----------------------------- Productos -----------------------------

    @Test
    @DisplayName("Crear producto hereda sede/unidad del contexto y emite producto.creado")
    void crearProductoHeredaContexto() {
        DetalleProducto detalle = crearProductoBase(operador);

        assertEquals("sede-1", detalle.producto().idSedeAlt());
        assertEquals("unidad-1", detalle.producto().idUnidadAlt());
        assertEquals("user-op", detalle.producto().autorAlt());
        assertEquals(EstadoProducto.BORRADOR, detalle.producto().estado());
        assertTrue(detalle.tareas().isEmpty());
        assertTrue(publicador.tipos().contains("producto.creado"));
        assertEquals("caso-1", publicador.ultimo().metadatos().get("idCasoAlt"));
    }

    @Test
    @DisplayName("Editar un producto en estado no mutable lanza ConflictoEstado (409)")
    void editarEnEstadoNoMutableConflicto() {
        DetalleProducto base = crearProductoBase(operador);
        String altKey = base.producto().altKey();
        servicio.transicionar(new TransicionProductoComando(
                coordinador, altKey, AccionProducto.ANULAR, "cierre"));

        assertThrows(ConflictoEstado.class, () -> servicio.editar(new EditarProductoComando(
                operador, altKey, "Nuevo titulo", null)));
    }

    @Test
    @DisplayName("Emitir sin rol de Coordinacion lanza ErrorAutorizacion (403)")
    void emitirSinCoordinacionProhibido() {
        DetalleProducto base = crearProductoBase(operador);
        String altKey = base.producto().altKey();
        llevarAEnRevision(altKey, operador);

        assertThrows(ErrorAutorizacion.class, () -> servicio.transicionar(
                new TransicionProductoComando(operador, altKey, AccionProducto.EMITIR, null)));
    }

    @Test
    @DisplayName("Emitir con Coordinacion asigna numero PRD-RM-1/2027 y emite producto.emitido")
    void emitirConCoordinacionAsignaNumero() {
        DetalleProducto base = crearProductoBase(operador);
        String altKey = base.producto().altKey();
        llevarAEnRevision(altKey, operador);

        DetalleProducto emitido = servicio.transicionar(new TransicionProductoComando(
                coordinador, altKey, AccionProducto.EMITIR, "ok"));

        assertEquals(EstadoProducto.EMITIDO, emitido.producto().estado());
        assertEquals("PRD-RM-1/2027", emitido.producto().numeroComoCadena());
        assertTrue(publicador.tipos().contains("producto.emitido"));
        assertEquals("PRD-RM-1/2027", publicador.ultimo().metadatos().get("numeroProducto"));
    }

    @Test
    @DisplayName("Ver un producto fuera de alcance territorial lanza 404 (no 403)")
    void detalleFueraDeAlcance404() {
        DetalleProducto base = crearProductoBase(operador);
        String altKey = base.producto().altKey();

        assertThrows(RecursoNoEncontrado.class, () -> servicio.detalle(otraSede, altKey));
    }

    @Test
    @DisplayName("Crear con enriquecimiento y caso inaccesible lanza ReglaNegocio (422)")
    void crearConEnriquecimientoCasoInaccesible() {
        ServicioProductos conEnriquecimiento =
                construir(true, new ClienteCasosDoble(), new ClientePersonasDoble());

        assertThrows(ReglaNegocio.class, () -> conEnriquecimiento.crear(new CrearProductoComando(
                operador, "caso-x", TipoProducto.OFICIO, "Oficio", null)));
    }

    @Test
    @DisplayName("Crear con enriquecimiento hereda sede/unidad del Caso")
    void crearConEnriquecimientoHeredaCaso() {
        ResumenCaso caso = new ResumenCaso("caso-1", "EXP-1", "ABIERTO", "sede-9", "unidad-9", "ALTA");
        ServicioProductos conEnriquecimiento =
                construir(true, new ClienteCasosDoble(caso), new ClientePersonasDoble());

        DetalleProducto detalle = conEnriquecimiento.crear(new CrearProductoComando(
                operador, "caso-1", TipoProducto.INFORME, "Informe", null));

        assertEquals("sede-9", detalle.producto().idSedeAlt());
        assertEquals("unidad-9", detalle.producto().idUnidadAlt());
    }

    // ------------------------------ Tareas -------------------------------

    @Test
    @DisplayName("Crear tarea bajo producto hereda Caso/sede/unidad y emite tarea.creada")
    void crearTareaBajoProductoHereda() {
        DetalleProducto base = crearProductoBase(operador);
        String idProducto = base.producto().altKey();

        Tarea tarea = servicio.crearBajoProducto(new CrearTareaComando(
                operador, null, idProducto, "Revisar antecedentes", "desc", null,
                PrioridadTarea.ALTA, null));

        assertEquals("caso-1", tarea.idCasoAlt());
        assertEquals(idProducto, tarea.idProductoAlt());
        assertEquals("sede-1", tarea.idSedeAlt());
        assertEquals(EstadoTarea.PENDIENTE, tarea.estado());
        assertNull(tarea.responsableNombre());
        assertTrue(publicador.tipos().contains("tarea.creada"));
        assertEquals(idProducto, publicador.ultimo().metadatos().get("idProductoAlt"));
    }

    @Test
    @DisplayName("Flujo: Coordinacion REASIGNA, el responsable TOMA y COMPLETA")
    void flujoReasignarTomarCompletar() {
        Tarea tarea = servicio.crearSuelta(new CrearTareaComando(
                operador, "caso-1", null, "Gestion", null, null, PrioridadTarea.MEDIA, null));
        String altKey = tarea.altKey();

        Tarea reasignada = servicio.transicionar(new TransicionTareaComando(
                coordinador, altKey, AccionTarea.REASIGNAR, "user-op", "asignacion"));
        assertEquals("user-op", reasignada.responsableAlt());
        assertTrue(publicador.tipos().contains("tarea.asignada"));

        Tarea tomada = servicio.transicionar(new TransicionTareaComando(
                operador, altKey, AccionTarea.TOMAR, null, null));
        assertEquals(EstadoTarea.EN_CURSO, tomada.estado());

        Tarea completada = servicio.transicionar(new TransicionTareaComando(
                operador, altKey, AccionTarea.COMPLETAR, null, null));
        assertEquals(EstadoTarea.COMPLETADA, completada.estado());
        assertTrue(publicador.tipos().contains("tarea.completada"));
    }

    @Test
    @DisplayName("Operar una tarea sin ser responsable ni Coordinacion lanza 403")
    void operarTareaSinPermiso403() {
        Tarea tarea = servicio.crearSuelta(new CrearTareaComando(
                operador, "caso-1", null, "Gestion", null, "user-op", PrioridadTarea.MEDIA, null));
        String altKey = tarea.altKey();

        assertThrows(ErrorAutorizacion.class, () -> servicio.transicionar(
                new TransicionTareaComando(operadorZ, altKey, AccionTarea.TOMAR, null, null)));
    }

    @Test
    @DisplayName("REASIGNAR sin responsable lanza ReglaNegocio (422)")
    void reasignarSinResponsable422() {
        Tarea tarea = servicio.crearSuelta(new CrearTareaComando(
                operador, "caso-1", null, "Gestion", null, "user-op", PrioridadTarea.MEDIA, null));
        String altKey = tarea.altKey();

        assertThrows(ReglaNegocio.class, () -> servicio.transicionar(
                new TransicionTareaComando(operador, altKey, AccionTarea.REASIGNAR, null, null)));
    }
}
