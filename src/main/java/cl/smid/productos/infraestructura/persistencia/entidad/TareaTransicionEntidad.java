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

import java.time.LocalDateTime;

/**
 * Entidad JPA de la tabla {@code tarea_transicion}: asiento append-only del historial de una
 * Tarea. Referencia al padre por llave primaria interna escalar ({@code tarea_id}).
 */
@Entity
@Table(name = "tarea_transicion")
@Getter
@Setter
@NoArgsConstructor
public class TareaTransicionEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tarea_id", nullable = false)
    private Long tareaId;

    @Column(name = "alt_key", nullable = false, length = 36, unique = true)
    private String altKey;

    @Column(name = "estado_origen", length = 12)
    private String estadoOrigen;

    @Column(name = "estado_destino", nullable = false, length = 12)
    private String estadoDestino;

    @Column(name = "accion", nullable = false, length = 12)
    private String accion;

    @Column(name = "observacion", length = 1000)
    private String observacion;

    @Column(name = "actor", nullable = false, length = 36)
    private String actor;

    @Column(name = "ocurrido_en", nullable = false)
    private LocalDateTime ocurridoEn;
}
