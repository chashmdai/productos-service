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
 * Entidad JPA de la tabla {@code producto_transicion}: asiento append-only del historial de un
 * Producto. Referencia al padre por su llave primaria interna escalar ({@code producto_id}), no
 * como relacion JPA, para mantener el agregado desacoplado y las inserciones simples.
 */
@Entity
@Table(name = "producto_transicion")
@Getter
@Setter
@NoArgsConstructor
public class ProductoTransicionEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "alt_key", nullable = false, length = 36, unique = true)
    private String altKey;

    @Column(name = "estado_origen", length = 20)
    private String estadoOrigen;

    @Column(name = "estado_destino", nullable = false, length = 20)
    private String estadoDestino;

    @Column(name = "accion", nullable = false, length = 20)
    private String accion;

    @Column(name = "observacion", length = 1000)
    private String observacion;

    @Column(name = "actor", nullable = false, length = 36)
    private String actor;

    @Column(name = "ocurrido_en", nullable = false)
    private LocalDateTime ocurridoEn;
}
