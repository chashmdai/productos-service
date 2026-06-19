package cl.smid.productos.dominio.modelo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Agregado raiz <strong>Tarea</strong>: un item de trabajo asignable, ligado siempre a un Caso
 * y opcionalmente a un Producto (cuando nace bajo un entregable concreto).
 *
 * <p>POJO puro (sin Spring/JPA/Lombok). Se identifica por su {@code altKey} opaco; la clave
 * primaria interna no vive en el dominio. La sede y la unidad se heredan al crear (del Producto
 * padre, del Caso o del contexto del usuario, segun el modo de enriquecimiento) y rigen el
 * filtrado territorial (override 6: fuera de alcance ⇒ 404).</p>
 *
 * <p>El responsable es opcional: una Tarea puede crearse sin asignar y tomarse luego. El
 * {@code responsableNombre} es un <em>snapshot</em> no sensible (nombre legible) que evita
 * resolver Personas en cada lectura.</p>
 *
 * <p><b>Historial append-only.</b> Igual que {@link Producto}, distingue {@code historialPrevio}
 * (ya persistido) de {@code pendientes} (generado en la operacion en curso). La accion
 * {@code REASIGNAR} no altera el estado pero deja asiento.</p>
 */
public final class Tarea {

    private final String altKey;
    private final String idCasoAlt;
    private final String idProductoAlt;               // null si es tarea suelta del Caso
    private String titulo;
    private String descripcion;
    private String responsableAlt;                    // null mientras no este asignada
    private String responsableNombre;                 // snapshot no sensible (puede ser null)
    private PrioridadTarea prioridad;
    private EstadoTarea estado;
    private final LocalDate fechaVencimiento;          // opcional
    private final String idSedeAlt;
    private final String idUnidadAlt;
    private final String tipoSemilla;                  // null salvo tareas sembradas por evento
    private final Instant creadoEn;
    private Instant actualizadoEn;
    private Instant completadoEn;                       // null salvo COMPLETADA

    private final List<Transicion> historialPrevio;
    private final List<Transicion> pendientes = new ArrayList<>();

    private Tarea(String altKey, String idCasoAlt, String idProductoAlt, String titulo,
                  String descripcion, String responsableAlt, String responsableNombre,
                  PrioridadTarea prioridad, EstadoTarea estado, LocalDate fechaVencimiento,
                  String idSedeAlt, String idUnidadAlt, String tipoSemilla,
                  Instant creadoEn, Instant actualizadoEn, Instant completadoEn,
                  List<Transicion> historialPrevio) {
        this.altKey = altKey;
        this.idCasoAlt = idCasoAlt;
        this.idProductoAlt = idProductoAlt;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.responsableAlt = responsableAlt;
        this.responsableNombre = responsableNombre;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaVencimiento = fechaVencimiento;
        this.idSedeAlt = idSedeAlt;
        this.idUnidadAlt = idUnidadAlt;
        this.tipoSemilla = tipoSemilla;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
        this.completadoEn = completadoEn;
        this.historialPrevio = historialPrevio == null ? new ArrayList<>() : new ArrayList<>(historialPrevio);
    }

    /**
     * Crea una Tarea nueva en estado {@code PENDIENTE} y registra el asiento de creacion.
     *
     * @param altKey            identificador opaco recien generado.
     * @param idCasoAlt         Caso al que pertenece (obligatorio).
     * @param idProductoAlt     Producto padre (opcional; {@code null} si es tarea suelta).
     * @param titulo            titulo de la tarea.
     * @param descripcion       descripcion (puede ser nula).
     * @param responsableAlt    alt_key del responsable inicial (opcional).
     * @param responsableNombre snapshot de nombre legible del responsable (opcional).
     * @param prioridad         prioridad de la tarea.
     * @param fechaVencimiento  fecha limite (opcional).
     * @param idSedeAlt         sede heredada.
     * @param idUnidadAlt       unidad heredada.
     * @param tipoSemilla       marca de sembrado idempotente ({@code null} en alta manual).
     * @param actorCreacion     alt_key del actor del asiento de creacion (usuario o sistema).
     * @param altKeyAsiento     identificador opaco del asiento de creacion.
     * @param ahora             instante UTC de creacion.
     */
    public static Tarea crear(String altKey, String idCasoAlt, String idProductoAlt, String titulo,
                              String descripcion, String responsableAlt, String responsableNombre,
                              PrioridadTarea prioridad, LocalDate fechaVencimiento, String idSedeAlt,
                              String idUnidadAlt, String tipoSemilla, String actorCreacion,
                              String altKeyAsiento, Instant ahora) {
        Objects.requireNonNull(altKey, "altKey es obligatorio");
        Objects.requireNonNull(idCasoAlt, "idCasoAlt es obligatorio");
        Objects.requireNonNull(titulo, "titulo es obligatorio");
        Objects.requireNonNull(prioridad, "prioridad es obligatoria");
        Objects.requireNonNull(idSedeAlt, "idSedeAlt es obligatorio");
        Objects.requireNonNull(actorCreacion, "actorCreacion es obligatorio");
        Objects.requireNonNull(ahora, "ahora es obligatorio");
        Tarea t = new Tarea(altKey, idCasoAlt, idProductoAlt, titulo, descripcion, responsableAlt,
                responsableNombre, prioridad, EstadoTarea.PENDIENTE, fechaVencimiento, idSedeAlt,
                idUnidadAlt, tipoSemilla, ahora, ahora, null, new ArrayList<>());
        t.pendientes.add(new Transicion(altKeyAsiento, null, EstadoTarea.PENDIENTE.name(),
                Transicion.ACCION_CREACION, null, actorCreacion, ahora));
        return t;
    }

    /** Reconstituye el agregado desde persistencia (sin generar asientos nuevos). */
    public static Tarea reconstituir(String altKey, String idCasoAlt, String idProductoAlt, String titulo,
                                     String descripcion, String responsableAlt, String responsableNombre,
                                     PrioridadTarea prioridad, EstadoTarea estado, LocalDate fechaVencimiento,
                                     String idSedeAlt, String idUnidadAlt, String tipoSemilla,
                                     Instant creadoEn, Instant actualizadoEn, Instant completadoEn,
                                     List<Transicion> historialPrevio) {
        return new Tarea(altKey, idCasoAlt, idProductoAlt, titulo, descripcion, responsableAlt,
                responsableNombre, prioridad, estado, fechaVencimiento, idSedeAlt, idUnidadAlt,
                tipoSemilla, creadoEn, actualizadoEn, completadoEn, historialPrevio);
    }

    /**
     * Aplica un avance de ciclo de vida ya validado por el orquestador (alcance, permiso y
     * maquina de estados): {@code TOMAR}, {@code COMPLETAR} o {@code CANCELAR}.
     *
     * @param accion        accion aplicada (no {@code REASIGNAR}).
     * @param destino       estado resultante (calculado por la maquina de estados).
     * @param observacion   nota opcional del actor.
     * @param actor         alt_key del actor.
     * @param altKeyAsiento identificador opaco del asiento.
     * @param ahora         instante UTC.
     */
    public void aplicarAvance(AccionTarea accion, EstadoTarea destino, String observacion,
                              String actor, String altKeyAsiento, Instant ahora) {
        Objects.requireNonNull(accion, "accion es obligatoria");
        Objects.requireNonNull(destino, "destino es obligatorio");
        if (accion == AccionTarea.REASIGNAR) {
            throw new IllegalArgumentException("REASIGNAR se aplica con reasignar(), no con aplicarAvance()");
        }
        final String origen = this.estado.name();
        this.estado = destino;
        this.actualizadoEn = ahora;
        if (accion == AccionTarea.COMPLETAR) {
            this.completadoEn = ahora;
        }
        this.pendientes.add(new Transicion(altKeyAsiento, origen, destino.name(),
                accion.name(), observacion, actor, ahora));
    }

    /**
     * Reasigna la tarea a un nuevo responsable sin alterar su estado. Deja asiento con la
     * pseudo-transicion {@code REASIGNAR} (origen == destino).
     *
     * @param nuevoResponsableAlt    alt_key del nuevo responsable (obligatorio en esta operacion).
     * @param nuevoResponsableNombre snapshot de nombre legible (opcional).
     * @param observacion            nota opcional del actor.
     * @param actor                  alt_key del actor.
     * @param altKeyAsiento          identificador opaco del asiento.
     * @param ahora                  instante UTC.
     */
    public void reasignar(String nuevoResponsableAlt, String nuevoResponsableNombre, String observacion,
                          String actor, String altKeyAsiento, Instant ahora) {
        Objects.requireNonNull(nuevoResponsableAlt, "nuevoResponsableAlt es obligatorio");
        this.responsableAlt = nuevoResponsableAlt;
        this.responsableNombre = nuevoResponsableNombre;
        this.actualizadoEn = ahora;
        final String estadoActual = this.estado.name();
        this.pendientes.add(new Transicion(altKeyAsiento, estadoActual, estadoActual,
                AccionTarea.REASIGNAR.name(), observacion, actor, ahora));
    }

    // ---- Accesores (solo lectura del estado del agregado) ----

    public String altKey() { return altKey; }
    public String idCasoAlt() { return idCasoAlt; }
    public String idProductoAlt() { return idProductoAlt; }
    public String titulo() { return titulo; }
    public String descripcion() { return descripcion; }
    public String responsableAlt() { return responsableAlt; }
    public String responsableNombre() { return responsableNombre; }
    public PrioridadTarea prioridad() { return prioridad; }
    public EstadoTarea estado() { return estado; }
    public LocalDate fechaVencimiento() { return fechaVencimiento; }
    public String idSedeAlt() { return idSedeAlt; }
    public String idUnidadAlt() { return idUnidadAlt; }
    public String tipoSemilla() { return tipoSemilla; }
    public Instant creadoEn() { return creadoEn; }
    public Instant actualizadoEn() { return actualizadoEn; }
    public Instant completadoEn() { return completadoEn; }

    /** {@code true} si la tarea sigue abierta (no terminal). */
    public boolean estaAbierta() {
        return estado.esVigente();
    }

    /** Transiciones generadas en la operacion en curso (a insertar por el adaptador). */
    public List<Transicion> transicionesPendientes() {
        return Collections.unmodifiableList(pendientes);
    }

    /** Historial completo (previo + pendientes) para renderizar la respuesta. */
    public List<Transicion> historial() {
        List<Transicion> total = new ArrayList<>(historialPrevio.size() + pendientes.size());
        total.addAll(historialPrevio);
        total.addAll(pendientes);
        return Collections.unmodifiableList(total);
    }
}
