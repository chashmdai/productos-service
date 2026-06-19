package cl.smid.productos.infraestructura.persistencia;

import cl.smid.productos.dominio.puerto.salida.CorrelativoProductoPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Implementacion del correlativo oficial de Productos mediante un UPSERT atomico de MySQL.
 *
 * <p><b>Patron seguro ante concurrencia.</b> En una unica sentencia se inserta la fila de la serie
 * ({@code id_sede_alt}, {@code anio}) con valor inicial 1 o, si ya existe (colision de clave
 * primaria), se incrementa {@code ultimo}. La funcion {@code LAST_INSERT_ID(expr)} fija en la
 * sesion el valor reservado en <em>ambas</em> ramas (insercion e incremento), de modo que el
 * {@code SELECT LAST_INSERT_ID()} posterior devuelve exactamente ese valor. Envolver el valor
 * inicial en {@code LAST_INSERT_ID(1)} es imprescindible: sin ello, en la primera insercion la
 * funcion devolveria un valor de sesion obsoleto.</p>
 *
 * <p>Ambas sentencias deben ejecutarse sobre la <strong>misma conexion</strong>. Al participar en
 * la transaccion de la frontera (controlador {@code @Transactional}), {@link JdbcTemplate} obtiene
 * la conexion ligada a la transaccion para las dos llamadas, garantizando la consistencia del
 * {@code LAST_INSERT_ID()} por conexion. El bloqueo de fila de InnoDB serializa las series en
 * conflicto, produciendo valores unicos y contiguos.</p>
 */
@Repository
public class CorrelativoProductoJdbc implements CorrelativoProductoPort {

    private static final String UPSERT =
            "INSERT INTO correlativo_producto (id_sede_alt, anio, ultimo) "
                    + "VALUES (?, ?, LAST_INSERT_ID(1)) "
                    + "ON DUPLICATE KEY UPDATE ultimo = LAST_INSERT_ID(ultimo + 1)";

    private static final String RECUPERAR = "SELECT LAST_INSERT_ID()";

    private final JdbcTemplate jdbc;

    public CorrelativoProductoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long siguiente(String idSedeAlt, int anio) {
        jdbc.update(UPSERT, idSedeAlt, anio);
        Long valor = jdbc.queryForObject(RECUPERAR, Long.class);
        if (valor == null) {
            throw new IllegalStateException(
                    "No se pudo recuperar el correlativo reservado para la sede " + idSedeAlt);
        }
        return valor;
    }
}
