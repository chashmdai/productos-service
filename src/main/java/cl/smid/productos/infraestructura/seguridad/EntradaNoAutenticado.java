package cl.smid.productos.infraestructura.seguridad;

import cl.smid.productos.dominio.excepcion.CodigoError;
import cl.smid.productos.infraestructura.web.EscritorRespuestaError;
import cl.smid.productos.infraestructura.web.SobreError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Punto de entrada de autenticacion: responde 401 con el sobre de error unificado (codigo
 * {@code AUTZ-003}) cuando se accede a un recurso protegido sin credencial valida.
 */
@Component
public class EntradaNoAutenticado implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta,
                         AuthenticationException excepcion) throws IOException {
        SobreError sobre = SobreError.de(
                CodigoError.NO_AUTENTICADO.httpStatus(),
                CodigoError.NO_AUTENTICADO.titulo(),
                CodigoError.NO_AUTENTICADO.codigo(),
                "Se requiere un token de autenticacion valido",
                null,
                peticion.getRequestURI());
        EscritorRespuestaError.escribir(respuesta, sobre);
    }
}
