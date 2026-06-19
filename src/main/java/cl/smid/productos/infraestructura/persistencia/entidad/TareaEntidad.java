package cl.smid.productos.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA de la tabla {@code tarea}. Espejo persistente del agregado {@code Tarea}.
 * El vinculo con el Producto se modela como identificador opaco escalar
 * ({@code id_producto_alt}, anulable), no como relacion JPA, en coherencia con la politica de
 * solo-alt_key. {@code fecha_vencimiento} es fecha pura (DATE); el resto de marcas, UTC.
 */
@Entity
@Table(name = "tarea")
@Getter
@Setter
@NoArgsConstructor
public class TareaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "alt_key", nullable = false, length = 36, unique = true)
    private String altKey;

    @Column(name = "id_caso_alt", nullable = false, length = 36)
    private String idCasoAlt;

    @Column(name = "id_producto_alt", length = 36)
    private String idProductoAlt;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Column(name = "responsable_alt", length = 36)
    private String responsableAlt;

    @Column(name = "responsable_nombre", length = 200)
    private String responsableNombre;

    @Column(name = "prioridad", nullable = false, length = 10)
    private String prioridad;

    @Column(name = "estado", nullable = false, length = 12)
    private String estado;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "id_sede_alt", length = 36)
    private String idSedeAlt;

    @Column(name = "id_unidad_alt", length = 36)
    private String idUnidadAlt;

    @Column(name = "tipo_semilla", length = 40)
    private String tipoSemilla;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @Column(name = "completado_en")
    private LocalDateTime completadoEn;
}
