package cl.smid.productos.dominio.servicio;

import cl.smid.productos.dominio.excepcion.ErrorAutorizacion;
import cl.smid.productos.dominio.excepcion.ErrorValidacion;
import cl.smid.productos.dominio.excepcion.ConflictoEstado;
import cl.smid.productos.dominio.excepcion.RecursoNoEncontrado;
import cl.smid.productos.dominio.excepcion.ReglaNegocio;
import cl.smid.productos.dominio.modelo.AccionProducto;
import cl.smid.productos.dominio.modelo.AccionTarea;
import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.modelo.EstadoProducto;
import cl.smid.productos.dominio.modelo.EstadoTarea;
import cl.smid.productos.dominio.modelo.EventoDominio;
import cl.smid.productos.dominio.modelo.NumeroProducto;
import cl.smid.productos.dominio.modelo.Producto;
import cl.smid.productos.dominio.modelo.ResumenCaso;
import cl.smid.productos.dominio.modelo.Tarea;
import cl.smid.productos.dominio.modelo.vista.DetalleProducto;
import cl.smid.productos.dominio.modelo.vista.PaginaDominio;
import cl.smid.productos.dominio.puerto.entrada.GestionProductos;
import cl.smid.productos.dominio.puerto.entrada.GestionTareas;
import cl.smid.productos.dominio.puerto.entrada.comando.CrearProductoComando;
import cl.smid.productos.dominio.puerto.entrada.comando.CrearTareaComando;
import cl.smid.productos.dominio.puerto.entrada.comando.EditarProductoComando;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroProductos;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroTareas;
import cl.smid.productos.dominio.puerto.entrada.comando.TransicionProductoComando;
import cl.smid.productos.dominio.puerto.entrada.comando.TransicionTareaComando;
import cl.smid.productos.dominio.puerto.salida.ClienteCasos;
import cl.smid.productos.dominio.puerto.salida.ClientePersonas;
import cl.smid.productos.dominio.puerto.salida.GeneradorIdentificadores;
import cl.smid.productos.dominio.puerto.salida.PublicadorEventos;
import cl.smid.productos.dominio.puerto.salida.Reloj;
import cl.smid.productos.dominio.puerto.salida.RepositorioProductos;
import cl.smid.productos.dominio.puerto.salida.RepositorioTareas;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Orquestador de dominio: unico bean que implementa los dos casos de uso del servicio
 * ({@link GestionProductos} y {@link GestionTareas}). Coordina los agregados, las maquinas de
 * estados, el generador de numero, el evaluador de alcance, la persistencia y la publicacion de
 * eventos.
 *
 * <p>POJO puro: no abre transacciones (la frontera es el controlador) ni conoce framework alguno.
 * Sus reglas centrales:</p>
 * <ul>
 *   <li><b>Territorio (override 6):</b> un registro fuera de alcance se trata como inexistente (404).</li>
 *   <li><b>Rol:</b> EMITIR/ANULAR de Producto exigen Coordinacion (403 si falta).</li>
 *   <li><b>Permiso de Tarea:</b> Coordinacion o responsable actual (403 si no).</li>
 *   <li><b>Herencia territorial:</b> con enriquecimiento activo se toma del Caso (422 si inaccesible);
 *       en su defecto, del contexto del usuario.</li>
 *   <li><b>Eventos:</b> solo metadatos no sensibles; publicacion tolerante a fallos.</li>
 * </ul>
 */
public class ServicioProductos implements GestionProductos, GestionTareas {

    private final RepositorioProductos repoProductos;
    private final RepositorioTareas repoTareas;
    private final MaquinaEstadosProducto maquinaProducto;
    private final MaquinaEstadosTarea maquinaTarea;
    private final GeneradorNumeroProducto generadorNumero;
    private final EvaluadorAlcance evaluadorAlcance;
    private final PublicadorEventos publicador;
    private final Reloj reloj;
    private final GeneradorIdentificadores generadorIds;
    private final ClienteCasos clienteCasos;
    private final ClientePersonas clientePersonas;
    private final Set<String> rolesCoordinacion;
    private final boolean enriquecimientoCasosActivo;

    public ServicioProductos(RepositorioProductos repoProductos,
                             RepositorioTareas repoTareas,
                             MaquinaEstadosProducto maquinaProducto,
                             MaquinaEstadosTarea maquinaTarea,
                             GeneradorNumeroProducto generadorNumero,
                             EvaluadorAlcance evaluadorAlcance,
                             PublicadorEventos publicador,
                             Reloj reloj,
                             GeneradorIdentificadores generadorIds,
                             ClienteCasos clienteCasos,
                             ClientePersonas clientePersonas,
                             Set<String> rolesCoordinacion,
                             boolean enriquecimientoCasosActivo) {
        this.repoProductos = repoProductos;
        this.repoTareas = repoTareas;
        this.maquinaProducto = maquinaProducto;
        this.maquinaTarea = maquinaTarea;
        this.generadorNumero = generadorNumero;
        this.evaluadorAlcance = evaluadorAlcance;
        this.publicador = publicador;
        this.reloj = reloj;
        this.generadorIds = generadorIds;
        this.clienteCasos = clienteCasos;
        this.clientePersonas = clientePersonas;
        this.rolesCoordinacion = rolesCoordinacion == null ? Set.of() : Set.copyOf(rolesCoordinacion);
        this.enriquecimientoCasosActivo = enriquecimientoCasosActivo;
    }

    // ===================== GestionProductos =====================

    @Override
    public DetalleProducto crear(CrearProductoComando comando) {
        ContextoSesion ctx = comando.contexto();
        exigirTexto(comando.idCasoAlt(), "idCaso");
        exigirObjeto(comando.tipo(), "tipo");
        exigirTexto(comando.titulo(), "titulo");

        String[] territorio = resolverTerritorio(comando.idCasoAlt(), ctx);
        String idSedeAlt = territorio[0];
        String idUnidadAlt = territorio[1];

        Instant ahora = reloj.ahora();
        String altKey = generadorIds.nuevo();
        String altKeyAsiento = generadorIds.nuevo();

        Producto producto = Producto.crear(altKey, comando.idCasoAlt(), comando.tipo(),
                comando.titulo(), comando.descripcion(), idSedeAlt, idUnidadAlt, ctx.sub(),
                altKeyAsiento, ahora);
        repoProductos.crear(producto);

        publicador.publicar(EventoDominio.de("producto.creado", altKey, ahora,
                "idCasoAlt", comando.idCasoAlt(), "tipo", comando.tipo().name()));

        return DetalleProducto.sinTareas(producto);
    }

    @Override
    public DetalleProducto detalle(ContextoSesion contexto, String altKey) {
        Producto producto = cargarProductoVisible(altKey, contexto);
        List<Tarea> tareas = repoTareas.listarPorProducto(altKey);
        return new DetalleProducto(producto, tareas);
    }

    @Override
    public PaginaDominio<Producto> listar(FiltroProductos filtro) {
        return repoProductos.listar(filtro);
    }

    @Override
    public DetalleProducto editar(EditarProductoComando comando) {
        Producto producto = cargarProductoVisible(comando.altKey(), comando.contexto());
        if (!producto.estado().esMutable()) {
            throw new ConflictoEstado(
                    "No se puede editar un producto en estado " + producto.estado());
        }
        producto.editar(comando.titulo(), comando.descripcion(), reloj.ahora());
        repoProductos.actualizar(producto);
        List<Tarea> tareas = repoTareas.listarPorProducto(producto.altKey());
        return new DetalleProducto(producto, tareas);
    }

    @Override
    public DetalleProducto transicionar(TransicionProductoComando comando) {
        ContextoSesion ctx = comando.contexto();
        Producto producto = cargarProductoVisible(comando.altKey(), ctx);
        AccionProducto accion = comando.accion();

        if (accion.exigeCoordinacion() && !esCoordinacion(ctx)) {
            throw new ErrorAutorizacion(
                    "La accion " + accion + " requiere rol de Coordinacion");
        }

        EstadoProducto destino = maquinaProducto.siguiente(producto.estado(), accion);

        Instant ahora = reloj.ahora();
        NumeroProducto numero = null;
        if (accion == AccionProducto.EMITIR) {
            if (producto.idSedeAlt() == null || producto.idSedeAlt().isBlank()) {
                throw new ReglaNegocio(
                        "El producto no tiene sede asociada; no puede emitirse");
            }
            int anio = ahora.atZone(ZoneOffset.UTC).getYear();
            numero = generadorNumero.generar(producto.idSedeAlt(), anio);
        }

        String altKeyAsiento = generadorIds.nuevo();
        producto.aplicar(accion, destino, numero, comando.observacion(), ctx.sub(), altKeyAsiento, ahora);
        repoProductos.actualizar(producto);

        switch (accion) {
            case EMITIR -> publicador.publicar(EventoDominio.de("producto.emitido", producto.altKey(),
                    ahora, "numeroProducto", producto.numeroComoCadena()));
            case ANULAR -> publicador.publicar(EventoDominio.de("producto.anulado", producto.altKey(), ahora));
            case ENVIAR_REVISION -> { /* sin evento asociado */ }
        }

        List<Tarea> tareas = repoTareas.listarPorProducto(producto.altKey());
        return new DetalleProducto(producto, tareas);
    }

    // ===================== GestionTareas =====================

    @Override
    public Tarea crearBajoProducto(CrearTareaComando comando) {
        ContextoSesion ctx = comando.contexto();
        exigirTexto(comando.idProductoAlt(), "idProducto");
        exigirTexto(comando.titulo(), "titulo");
        exigirObjeto(comando.prioridad(), "prioridad");

        Producto producto = cargarProductoVisible(comando.idProductoAlt(), ctx);
        if (!producto.estado().esMutable()) {
            throw new ConflictoEstado(
                    "No se pueden agregar tareas a un producto en estado " + producto.estado());
        }

        String responsableNombre = resolverNombre(comando.responsableAlt());
        Instant ahora = reloj.ahora();
        String altKey = generadorIds.nuevo();
        String altKeyAsiento = generadorIds.nuevo();

        Tarea tarea = Tarea.crear(altKey, producto.idCasoAlt(), producto.altKey(), comando.titulo(),
                comando.descripcion(), comando.responsableAlt(), responsableNombre, comando.prioridad(),
                comando.fechaVencimiento(), producto.idSedeAlt(), producto.idUnidadAlt(), null,
                ctx.sub(), altKeyAsiento, ahora);
        repoTareas.crear(tarea);

        publicador.publicar(EventoDominio.de("tarea.creada", altKey, ahora,
                "idCasoAlt", producto.idCasoAlt(), "idProductoAlt", producto.altKey(),
                "prioridad", comando.prioridad().name()));

        return tarea;
    }

    @Override
    public Tarea crearSuelta(CrearTareaComando comando) {
        ContextoSesion ctx = comando.contexto();
        exigirTexto(comando.idCasoAlt(), "idCaso");
        exigirTexto(comando.titulo(), "titulo");
        exigirObjeto(comando.prioridad(), "prioridad");

        String[] territorio = resolverTerritorio(comando.idCasoAlt(), ctx);
        String idSedeAlt = territorio[0];
        String idUnidadAlt = territorio[1];

        String responsableNombre = resolverNombre(comando.responsableAlt());
        Instant ahora = reloj.ahora();
        String altKey = generadorIds.nuevo();
        String altKeyAsiento = generadorIds.nuevo();

        Tarea tarea = Tarea.crear(altKey, comando.idCasoAlt(), null, comando.titulo(),
                comando.descripcion(), comando.responsableAlt(), responsableNombre, comando.prioridad(),
                comando.fechaVencimiento(), idSedeAlt, idUnidadAlt, null, ctx.sub(), altKeyAsiento, ahora);
        repoTareas.crear(tarea);

        publicador.publicar(EventoDominio.de("tarea.creada", altKey, ahora,
                "idCasoAlt", comando.idCasoAlt(), "prioridad", comando.prioridad().name()));

        return tarea;
    }

    @Override
    public Tarea detalleTarea(ContextoSesion contexto, String altKey) {
        return cargarTareaVisible(altKey, contexto);
    }

    @Override
    public PaginaDominio<Tarea> listar(FiltroTareas filtro) {
        return repoTareas.listar(filtro);
    }

    @Override
    public Tarea transicionar(TransicionTareaComando comando) {
        ContextoSesion ctx = comando.contexto();
        Tarea tarea = cargarTareaVisible(comando.altKey(), ctx);
        AccionTarea accion = comando.accion();

        if (!puedeOperarTarea(ctx, tarea)) {
            throw new ErrorAutorizacion(
                    "Solo Coordinacion o el responsable actual pueden operar la tarea");
        }

        Instant ahora = reloj.ahora();
        String altKeyAsiento = generadorIds.nuevo();

        if (accion == AccionTarea.REASIGNAR) {
            exigirReglaTexto(comando.responsableAlt(), "REASIGNAR requiere responsableAlt");
            String nombre = resolverNombre(comando.responsableAlt());
            tarea.reasignar(comando.responsableAlt(), nombre, comando.observacion(), ctx.sub(),
                    altKeyAsiento, ahora);
            repoTareas.actualizar(tarea);
            publicador.publicar(EventoDominio.de("tarea.asignada", tarea.altKey(), ahora,
                    "responsableAlt", comando.responsableAlt()));
            return tarea;
        }

        EstadoTarea destino = maquinaTarea.siguiente(tarea.estado(), accion);
        tarea.aplicarAvance(accion, destino, comando.observacion(), ctx.sub(), altKeyAsiento, ahora);
        repoTareas.actualizar(tarea);

        if (accion == AccionTarea.COMPLETAR) {
            publicador.publicar(EventoDominio.de("tarea.completada", tarea.altKey(), ahora));
        }
        return tarea;
    }

    // ===================== Apoyos privados =====================

    /** Carga un Producto y exige que sea visible para el contexto; de lo contrario, 404. */
    private Producto cargarProductoVisible(String altKey, ContextoSesion ctx) {
        Producto producto = repoProductos.buscarPorAltKey(altKey)
                .orElseThrow(() -> new RecursoNoEncontrado("Producto no encontrado: " + altKey));
        if (!evaluadorAlcance.puedeVer(producto.idSedeAlt(), producto.idUnidadAlt(), ctx)) {
            throw new RecursoNoEncontrado("Producto no encontrado: " + altKey);
        }
        return producto;
    }

    /** Carga una Tarea y exige que sea visible para el contexto; de lo contrario, 404. */
    private Tarea cargarTareaVisible(String altKey, ContextoSesion ctx) {
        Tarea tarea = repoTareas.buscarPorAltKey(altKey)
                .orElseThrow(() -> new RecursoNoEncontrado("Tarea no encontrada: " + altKey));
        if (!evaluadorAlcance.puedeVer(tarea.idSedeAlt(), tarea.idUnidadAlt(), ctx)) {
            throw new RecursoNoEncontrado("Tarea no encontrada: " + altKey);
        }
        return tarea;
    }

    /**
     * Resuelve la sede y la unidad a heredar. Con enriquecimiento activo consulta el Caso (y
     * rechaza con 422 si es inaccesible/inexistente); en su defecto, hereda del contexto.
     *
     * @return arreglo {@code [idSedeAlt, idUnidadAlt]}.
     */
    private String[] resolverTerritorio(String idCasoAlt, ContextoSesion ctx) {
        if (enriquecimientoCasosActivo) {
            Optional<ResumenCaso> resumen = clienteCasos.obtener(idCasoAlt);
            ResumenCaso caso = resumen.orElseThrow(() -> new ReglaNegocio(
                    "El caso indicado no existe o no es accesible: " + idCasoAlt));
            return new String[]{caso.idSedeAlt(), caso.idUnidadAlt()};
        }
        return new String[]{ctx.idSede(), ctx.idUnidad()};
    }

    /** Resuelve el nombre legible (snapshot no sensible) del responsable, o {@code null}. */
    private String resolverNombre(String responsableAlt) {
        if (responsableAlt == null || responsableAlt.isBlank()) {
            return null;
        }
        return clientePersonas.nombreLegible(responsableAlt).orElse(null);
    }

    /** {@code true} si el contexto porta algun rol de Coordinacion. */
    private boolean esCoordinacion(ContextoSesion ctx) {
        return ctx.tieneAlgunRol(rolesCoordinacion);
    }

    /** Regla unificada de permiso de Tarea: Coordinacion o responsable actual. */
    private boolean puedeOperarTarea(ContextoSesion ctx, Tarea tarea) {
        if (esCoordinacion(ctx)) {
            return true;
        }
        return tarea.responsableAlt() != null && tarea.responsableAlt().equals(ctx.sub());
    }

    private void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ErrorValidacion("El campo '" + campo + "' es obligatorio");
        }
    }

    private void exigirObjeto(Object valor, String campo) {
        if (valor == null) {
            throw new ErrorValidacion("El campo '" + campo + "' es obligatorio");
        }
    }

    private void exigirReglaTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocio(mensaje);
        }
    }
}
