package cl.smid.productos.dominio.modelo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Agregado raiz <strong>Producto</strong>: un entregable de un Caso (informe, gestion,
 * oficio, derivacion, resolucion u otro).
 *
 * <p>POJO puro (sin Spring/JPA/Lombok). La clave primaria interna no vive aqui: el agregado
 * se identifica por su {@code altKey} opaco. La sede y la unidad se <em>heredan del Caso</em>
 * al crear y rigen el filtrado territorial (override 6: fuera de alcance ⇒ 404).</p>
 *
 * <p><b>Historial append-only.</b> El agregado distingue las transiciones ya persistidas
 * ({@code historialPrevio}) de las generadas en la operacion en curso ({@code pendientes}).
 * El adaptador de persistencia inserta solo {@link #transicionesPendientes()}.</p>
 */
public final class Producto {

    private final String altKey;
    private final String idCasoAlt;
    private final TipoProducto tipo;
    private String titulo;
    private String descripcion;
    private EstadoProducto estado;
    private NumeroProducto numeroProducto;          // null hasta EMITIR
    private final String idSedeAlt;
    private final String idUnidadAlt;
    private final String autorAlt;
    private boolean vigente;                          // false tras ANULAR
    private final Instant creadoEn;
    private Instant actualizadoEn;
    private Instant emitidoEn;                        // null salvo EMITIDO
    private Instant anuladoEn;                        // null salvo ANULADO

    private final List<Transicion> historialPrevio;
    private final List<Transicion> pendientes = new ArrayList<>();

    private Producto(String altKey, String idCasoAlt, TipoProducto tipo, String titulo,
                     String descripcion, EstadoProducto estado, NumeroProducto numeroProducto,
                     String idSedeAlt, String idUnidadAlt, String autorAlt, boolean vigente,
                     Instant creadoEn, Instant actualizadoEn, Instant emitidoEn, Instant anuladoEn,
                     List<Transicion> historialPrevio) {
        this.altKey = altKey;
        this.idCasoAlt = idCasoAlt;
        this.tipo = tipo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.estado = estado;
        this.numeroProducto = numeroProducto;
        this.idSedeAlt = idSedeAlt;
        this.idUnidadAlt = idUnidadAlt;
        this.autorAlt = autorAlt;
        this.vigente = vigente;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
        this.emitidoEn = emitidoEn;
        this.anuladoEn = anuladoEn;
        this.historialPrevio = historialPrevio == null ? new ArrayList<>() : new ArrayList<>(historialPrevio);
    }

    /**
     * Crea un Producto nuevo en estado {@code BORRADOR} y registra el asiento de creacion.
     *
     * @param altKey        identificador opaco recien generado.
     * @param idCasoAlt     Caso al que pertenece (obligatorio).
     * @param tipo          tipo de entregable.
     * @param titulo        titulo del producto.
     * @param descripcion   descripcion (puede ser nula).
     * @param idSedeAlt     sede heredada del Caso.
     * @param idUnidadAlt   unidad heredada del Caso.
     * @param autorAlt      alt_key del usuario autor (actor del asiento de creacion).
     * @param altKeyAsiento identificador opaco del asiento de creacion.
     * @param ahora         instante UTC de creacion.
     */
    public static Producto crear(String altKey, String idCasoAlt, TipoProducto tipo, String titulo,
                                 String descripcion, String idSedeAlt, String idUnidadAlt,
                                 String autorAlt, String altKeyAsiento, Instant ahora) {
        Objects.requireNonNull(altKey, "altKey es obligatorio");
        Objects.requireNonNull(idCasoAlt, "idCasoAlt es obligatorio");
        Objects.requireNonNull(tipo, "tipo es obligatorio");
        Objects.requireNonNull(titulo, "titulo es obligatorio");
        Objects.requireNonNull(idSedeAlt, "idSedeAlt es obligatorio");
        Objects.requireNonNull(autorAlt, "autorAlt es obligatorio");
        Objects.requireNonNull(ahora, "ahora es obligatorio");
        Producto p = new Producto(altKey, idCasoAlt, tipo, titulo, descripcion,
                EstadoProducto.BORRADOR, null, idSedeAlt, idUnidadAlt, autorAlt, true,
                ahora, ahora, null, null, new ArrayList<>());
        p.pendientes.add(new Transicion(altKeyAsiento, null, EstadoProducto.BORRADOR.name(),
                Transicion.ACCION_CREACION, null, autorAlt, ahora));
        return p;
    }

    /** Reconstituye el agregado desde persistencia (sin generar asientos nuevos). */
    public static Producto reconstituir(String altKey, String idCasoAlt, TipoProducto tipo, String titulo,
                                        String descripcion, EstadoProducto estado, NumeroProducto numeroProducto,
                                        String idSedeAlt, String idUnidadAlt, String autorAlt, boolean vigente,
                                        Instant creadoEn, Instant actualizadoEn, Instant emitidoEn, Instant anuladoEn,
                                        List<Transicion> historialPrevio) {
        return new Producto(altKey, idCasoAlt, tipo, titulo, descripcion, estado, numeroProducto,
                idSedeAlt, idUnidadAlt, autorAlt, vigente, creadoEn, actualizadoEn, emitidoEn, anuladoEn,
                historialPrevio);
    }

    /**
     * Aplica una edicion parcial (merge): solo se modifican los campos no nulos.
     * La validacion de mutabilidad (estado BORRADOR/EN_REVISION) la realiza el orquestador.
     */
    public void editar(String nuevoTitulo, String nuevaDescripcion, Instant ahora) {
        if (nuevoTitulo != null && !nuevoTitulo.isBlank()) {
            this.titulo = nuevoTitulo;
        }
        if (nuevaDescripcion != null) {
            this.descripcion = nuevaDescripcion;
        }
        this.actualizadoEn = ahora;
    }

    /**
     * Aplica una transicion ya validada por el orquestador (alcance, rol y maquina de estados).
     * Centraliza la mutacion de estado, las marcas temporales y el asiento del historial.
     *
     * @param accion        accion aplicada.
     * @param destino       estado resultante (calculado por la maquina de estados).
     * @param numero        numero oficial; obligatorio solo en {@code EMITIR}, ignorado en el resto.
     * @param observacion   nota opcional del actor.
     * @param actor         alt_key del actor.
     * @param altKeyAsiento identificador opaco del asiento.
     * @param ahora         instante UTC.
     */
    public void aplicar(AccionProducto accion, EstadoProducto destino, NumeroProducto numero,
                        String observacion, String actor, String altKeyAsiento, Instant ahora) {
        Objects.requireNonNull(accion, "accion es obligatoria");
        Objects.requireNonNull(destino, "destino es obligatorio");
        final String origen = this.estado.name();
        this.estado = destino;
        this.actualizadoEn = ahora;
        switch (accion) {
            case EMITIR -> {
                this.numeroProducto = Objects.requireNonNull(numero, "EMITIR requiere un numero asignado");
                this.emitidoEn = ahora;
            }
            case ANULAR -> {
                this.anuladoEn = ahora;
                this.vigente = false;
            }
            case ENVIAR_REVISION -> { /* sin efectos colaterales adicionales */ }
        }
        this.pendientes.add(new Transicion(altKeyAsiento, origen, destino.name(),
                accion.name(), observacion, actor, ahora));
    }

    // ---- Accesores (solo lectura del estado del agregado) ----

    public String altKey() { return altKey; }
    public String idCasoAlt() { return idCasoAlt; }
    public TipoProducto tipo() { return tipo; }
    public String titulo() { return titulo; }
    public String descripcion() { return descripcion; }
    public EstadoProducto estado() { return estado; }
    public NumeroProducto numeroProducto() { return numeroProducto; }
    public String idSedeAlt() { return idSedeAlt; }
    public String idUnidadAlt() { return idUnidadAlt; }
    public String autorAlt() { return autorAlt; }
    public boolean vigente() { return vigente; }
    public Instant creadoEn() { return creadoEn; }
    public Instant actualizadoEn() { return actualizadoEn; }
    public Instant emitidoEn() { return emitidoEn; }
    public Instant anuladoEn() { return anuladoEn; }

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

    /** Numero como cadena, o {@code null} si aun no se ha emitido (para mapeos). */
    public String numeroComoCadena() {
        return numeroProducto == null ? null : numeroProducto.valor();
    }

    /** Fecha de vencimiento no aplica al Producto; helper reservado para coherencia de mapeos. */
    public LocalDate sinVencimiento() {
        return null;
    }
}
