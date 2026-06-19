package cl.smid.productos.infraestructura.seguridad;

import cl.smid.productos.dominio.excepcion.CodigoError;
import cl.smid.productos.infraestructura.web.EscritorRespuestaError;
import cl.smid.productos.infraestructura.web.SobreError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Manejador de acceso denegado a nivel de la cadena de seguridad: responde 403 con el sobre de
 * error unificado (codigo {@code AUTZ-004}). Los 403 de regla de negocio (falta de rol sobre un
 * recurso visible) los emite el dominio y los traduce el {@code @RestControllerAdvice}.
 */
@Component
public class ManejadorAccesoDenegado implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta,
                       AccessDeniedException excepcion) throws IOException {
        SobreError sobre = SobreError.de(
                CodigoError.NO_AUTORIZADO.httpStatus(),
                CodigoError.NO_AUTORIZADO.titulo(),
                CodigoError.NO_AUTORIZADO.codigo(),
                "No tiene permisos para acceder a este recurso",
                null,
                peticion.getRequestURI());
        EscritorRespuestaError.escribir(respuesta, sobre);
    }
}
