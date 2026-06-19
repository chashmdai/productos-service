package cl.smid.productos.concurrencia;

import cl.smid.productos.infraestructura.persistencia.CorrelativoProductoJdbc;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que el correlativo de Productos sea seguro ante concurrencia: N hilos reservando sobre
 * la misma serie (sede, anio) deben obtener exactamente los valores 1..N, sin huecos ni duplicados.
 *
 * <p>Requiere Docker (Testcontainers MySQL). Se omite automaticamente si Docker no esta disponible.
 * Levanta solo la infraestructura JDBC necesaria (DataSource + TransactionTemplate), sin el
 * contexto completo de Spring Boot.</p>
 */
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Concurrencia del correlativo de Productos")
class ConcurrenciaCorrelativoTest {

    @SuppressWarnings("resource")
    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("db_productos")
            .withUsername("smid")
            .withPassword("smid")
            .withUrlParam("allowPublicKeyRetrieval", "true")
            .withUrlParam("useSSL", "false");

    private static HikariDataSource dataSource;
    private static CorrelativoProductoJdbc correlativo;
    private static TransactionTemplate tx;

    @BeforeAll
    static void preparar() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(MYSQL.getJdbcUrl());
        config.setUsername(MYSQL.getUsername());
        config.setPassword(MYSQL.getPassword());
        config.setMaximumPoolSize(16);
        HikariDataSource nuevoDataSource = new HikariDataSource(config);
        dataSource = nuevoDataSource;

        JdbcTemplate jdbc = new JdbcTemplate(Objects.requireNonNull(nuevoDataSource));
        jdbc.execute("CREATE TABLE correlativo_producto ("
                + "id_sede_alt VARCHAR(36) NOT NULL, "
                + "anio INT NOT NULL, "
                + "ultimo BIGINT NOT NULL, "
                + "CONSTRAINT pk_correlativo_producto PRIMARY KEY (id_sede_alt, anio)) ENGINE=InnoDB");

        correlativo = new CorrelativoProductoJdbc(jdbc);
        tx = new TransactionTemplate(new DataSourceTransactionManager(
                Objects.requireNonNull(nuevoDataSource)));
    }

    @AfterAll
    static void cerrar() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    @DisplayName("N hilos sobre la misma serie obtienen valores 1..N unicos y contiguos")
    void reservaConcurrenteEsUnicaYContigua() throws InterruptedException {
        int hilos = 50;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch listos = new CountDownLatch(hilos);
        CountDownLatch arranque = new CountDownLatch(1);
        ConcurrentLinkedQueue<Long> resultados = new ConcurrentLinkedQueue<>();
        AtomicInteger errores = new AtomicInteger(0);

        for (int i = 0; i < hilos; i++) {
            pool.submit(() -> {
                listos.countDown();
                try {
                    arranque.await();
                    Long valor = tx.execute(status -> correlativo.siguiente("sede-1", 2027));
                    resultados.add(Objects.requireNonNull(valor,
                            "La reserva de correlativo no debe ser nula"));
                } catch (Exception e) {
                    errores.incrementAndGet();
                }
            });
        }

        listos.await();
        arranque.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS), "Los hilos no terminaron a tiempo");

        assertEquals(0, errores.get(), "No deben producirse errores de reserva");
        assertEquals(hilos, resultados.size(), "Deben emitirse N valores");

        Set<Long> unicos = resultados.stream().collect(Collectors.toSet());
        assertEquals(hilos, unicos.size(), "Todos los valores deben ser unicos");

        List<Long> esperados = IntStream.rangeClosed(1, hilos)
                .mapToObj(Long::valueOf).collect(Collectors.toList());
        List<Long> obtenidos = new ArrayList<>(unicos);
        obtenidos.sort(Long::compareTo);
        assertEquals(esperados, obtenidos, "Los valores deben ser contiguos 1..N");
    }
}
