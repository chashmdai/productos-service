package cl.smid.productos.integracion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion del servicio sobre MySQL real (Testcontainers), ejercitando la pila
 * completa: seguridad JWT, controladores, dominio, persistencia (Flyway + JPA) y manejo de errores.
 *
 * <p>Requiere Docker; se omite automaticamente si no esta disponible. Publicacion por log y sin
 * consumo de eventos (no requiere RabbitMQ); enriquecimiento desactivado (no requiere Casos ni
 * Personas).</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Integracion: API de Productos y Tareas")
class IntegracionProductosTest {

    private static final String SECRETO = "secreto-de-integracion-pruebas-0123456789-XYZ";
    private static final String KID = "smid-2026-06";

    @SuppressWarnings("resource")
    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("db_productos")
            .withUsername("smid")
            .withPassword("smid")
            .withUrlParam("allowPublicKeyRetrieval", "true")
            .withUrlParam("useSSL", "false")
            .withUrlParam("tinyInt1isBit", "true")
            .withUrlParam("serverTimezone", "UTC");

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registro.add("spring.datasource.username", MYSQL::getUsername);
        registro.add("spring.datasource.password", MYSQL::getPassword);
        registro.add("smid.seguridad.kid-activo", () -> KID);
        registro.add("smid.seguridad.secreto-activo", () -> SECRETO);
        registro.add("smid.seguridad.issuer", () -> "smid-auth");
        registro.add("smid.seguridad.audiencia", () -> "smid-servicios");
        registro.add("smid.seguridad.roles-coordinacion", () -> "COORDINACION");
        registro.add("smid.eventos.transporte", () -> "log");
        registro.add("smid.eventos.consumo", () -> "none");
        registro.add("smid.enriquecimiento.casos", () -> "false");
        registro.add("smid.enriquecimiento.personas", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenOperador() {
        return GeneradorTokensPrueba.token(SECRETO, KID, "user-op", List.of("OPERADOR"),
                "sede-1", "unidad-1", "SEDE", "Operador");
    }

    private String tokenCoordinador() {
        return GeneradorTokensPrueba.token(SECRETO, KID, "user-coord", List.of("COORDINACION"),
                "sede-1", "unidad-1", "SEDE", "Coordinador");
    }

    private String tokenOtraSede() {
        return GeneradorTokensPrueba.token(SECRETO, KID, "user-2", List.of("OPERADOR"),
                "sede-2", "unidad-9", "SEDE", "Operador 2");
    }

    @NonNull
    private static MediaType json() {
        return Objects.requireNonNull(MediaType.APPLICATION_JSON);
    }

    private String crearProducto(String token) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/productos/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(json())
                        .content("{\"idCaso\":\"caso-1\",\"tipo\":\"INFORME\","
                                + "\"titulo\":\"Informe inicial\",\"descripcion\":\"detalle\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.idSede").value("sede-1"))
                .andReturn();
        JsonNode cuerpo = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return cuerpo.get("altKey").asText();
    }

    private void enviarARevision(String altKey, String token) throws Exception {
        mockMvc.perform(post("/productos/productos/" + altKey + "/transiciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(json())
                        .content("{\"accion\":\"ENVIAR_REVISION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"));
    }

    @Test
    @DisplayName("POST crea un producto (201) con token valido")
    void creaProducto() throws Exception {
        crearProducto(tokenOperador());
    }

    @Test
    @DisplayName("Sin token responde 401 con codigo AUTZ-003")
    void sinTokenNoAutenticado() throws Exception {
        mockMvc.perform(post("/productos/productos")
                        .contentType(json())
                        .content("{\"idCaso\":\"caso-1\",\"tipo\":\"INFORME\",\"titulo\":\"X\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("AUTZ-003"))
                .andExpect(jsonPath("$.ruta").value("/productos/productos"));
    }

    @Test
    @DisplayName("Un producto fuera de alcance territorial responde 404")
    void fueraDeAlcance404() throws Exception {
        String altKey = crearProducto(tokenOperador());
        mockMvc.perform(get("/productos/productos/" + altKey)
                        .header("Authorization", "Bearer " + tokenOtraSede()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PRD-404"));
    }

    @Test
    @DisplayName("Emitir sin rol de Coordinacion responde 403 (AUTZ-004)")
    void emitirSinCoordinacion403() throws Exception {
        String altKey = crearProducto(tokenOperador());
        enviarARevision(altKey, tokenOperador());
        mockMvc.perform(post("/productos/productos/" + altKey + "/transiciones")
                        .header("Authorization", "Bearer " + tokenOperador())
                        .contentType(json())
                        .content("{\"accion\":\"EMITIR\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("AUTZ-004"));
    }

    @Test
    @DisplayName("Emitir con Coordinacion responde 200 y asigna numero PRD-...")
    void emitirConCoordinacion200() throws Exception {
        String altKey = crearProducto(tokenOperador());
        enviarARevision(altKey, tokenOperador());
        mockMvc.perform(post("/productos/productos/" + altKey + "/transiciones")
                        .header("Authorization", "Bearer " + tokenCoordinador())
                        .contentType(json())
                        .content("{\"accion\":\"EMITIR\",\"observacion\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EMITIDO"))
                .andExpect(jsonPath("$.numeroProducto").exists());
    }

    @Test
    @DisplayName("Crea tarea bajo producto (201) y el responsable/Coordinacion la avanza")
    void crearTareaYAvanzar() throws Exception {
        String altKey = crearProducto(tokenOperador());

        MvcResult creada = mockMvc.perform(post("/productos/productos/" + altKey + "/tareas")
                        .header("Authorization", "Bearer " + tokenOperador())
                        .contentType(json())
                        .content("{\"titulo\":\"Revisar\",\"prioridad\":\"ALTA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn();
        String tareaAlt = objectMapper.readTree(creada.getResponse().getContentAsString())
                .get("altKey").asText();

        // Coordinacion puede operar una tarea sin responsable asignado.
        mockMvc.perform(post("/productos/tareas/" + tareaAlt + "/transiciones")
                        .header("Authorization", "Bearer " + tokenCoordinador())
                        .contentType(json())
                        .content("{\"accion\":\"TOMAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));

        mockMvc.perform(post("/productos/tareas/" + tareaAlt + "/transiciones")
                        .header("Authorization", "Bearer " + tokenCoordinador())
                        .contentType(json())
                        .content("{\"accion\":\"COMPLETAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }
}
