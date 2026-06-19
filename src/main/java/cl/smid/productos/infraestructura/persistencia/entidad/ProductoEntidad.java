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
 * Entidad JPA de la tabla {@code producto}. Espejo persistente del agregado de dominio
 * {@code Producto}. La llave primaria interna {@code id} nunca cruza la frontera de la API;
 * el identificador publico es {@code altKey}. Marcas temporales en UTC ({@link LocalDateTime}).
 */
@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
public class ProductoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "alt_key", nullable = false, length = 36, unique = true)
    private String altKey;

    @Column(name = "id_caso_alt", nullable = false, length = 36)
    private String idCasoAlt;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "numero_producto", length = 40, unique = true)
    private String numeroProducto;

    @Column(name = "id_sede_alt", length = 36)
    private String idSedeAlt;

    @Column(name = "id_unidad_alt", length = 36)
    private String idUnidadAlt;

    @Column(name = "autor_alt", nullable = false, length = 36)
    private String autorAlt;

    @Column(name = "vigente", nullable = false)
    private boolean vigente;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @Column(name = "emitido_en")
    private LocalDateTime emitidoEn;

    @Column(name = "anulado_en")
    private LocalDateTime anuladoEn;
}
