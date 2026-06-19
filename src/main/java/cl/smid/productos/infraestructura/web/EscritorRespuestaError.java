package cl.smid.productos.infraestructura.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Escribe un {@link SobreError} como cuerpo JSON en una {@link HttpServletResponse}. Lo usan los
 * manejadores de seguridad (entrada no autenticada y acceso denegado), que operan fuera del ambito
 * del {@code @RestControllerAdvice} y por tanto serializan la respuesta directamente.
 */
public final class EscritorRespuestaError {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private EscritorRespuestaError() {
    }

    public static void escribir(HttpServletResponse respuesta, SobreError sobre) throws IOException {
        respuesta.setStatus(sobre.status());
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        MAPPER.writeValue(respuesta.getWriter(), sobre);
    }
}
