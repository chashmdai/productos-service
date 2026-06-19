package cl.smid.productos.api.error;

import cl.smid.productos.dominio.excepcion.CodigoError;
import cl.smid.productos.dominio.excepcion.ErrorDominio;
import cl.smid.productos.infraestructura.web.SobreError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Traduce las excepciones de la capa web y del dominio al sobre de error unificado del proyecto
 * (campo {@code ruta}). Centraliza el mapeo {@code CodigoError -> estado HTTP} de modo que el
 * contrato de errores sea consistente en todos los endpoints.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    /** Errores de negocio del dominio: el tipo de la excepcion fija codigo y estado. */
    @ExceptionHandler(ErrorDominio.class)
    public ResponseEntity<SobreError> manejarDominio(ErrorDominio ex, HttpServletRequest req) {
        CodigoError codigo = ex.codigo();
        return construir(codigo, ex.getMessage(), null, req);
    }

    /** Validacion de DTOs (@Valid): desglosa los errores por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SobreError> manejarValidacion(MethodArgumentNotValidException ex,
                                                        HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return construir(CodigoError.VALIDACION,
                "La solicitud contiene errores de validacion", detalles, req);
    }

    /** Validacion de parametros (@Validated en query/path). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<SobreError> manejarRestricciones(ConstraintViolationException ex,
                                                           HttpServletRequest req) {
        List<String> detalles = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return construir(CodigoError.VALIDACION, "Parametros invalidos", detalles, req);
    }

    /** Cuerpo ilegible o con valores no mapeables (p. ej. enum o fecha invalida). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<SobreError> manejarCuerpoIlegible(HttpMessageNotReadableException ex,
                                                            HttpServletRequest req) {
        return construir(CodigoError.VALIDACION,
                "El cuerpo de la peticion es ilegible o contiene valores no validos", null, req);
    }

    /** Parametro de consulta requerido ausente. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<SobreError> manejarParametroFaltante(MissingServletRequestParameterException ex,
                                                               HttpServletRequest req) {
        return construir(CodigoError.VALIDACION,
                "Falta el parametro requerido: " + ex.getParameterName(), null, req);
    }

    /** Tipo de parametro incompatible (p. ej. texto donde se espera numero). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<SobreError> manejarTipoIncompatible(MethodArgumentTypeMismatchException ex,
                                                              HttpServletRequest req) {
        return construir(CodigoError.VALIDACION,
                "Valor invalido para el parametro: " + ex.getName(), null, req);
    }

    /** Red de seguridad: cualquier excepcion no contemplada se reporta como error interno. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SobreError> manejarGenerico(Exception ex, HttpServletRequest req) {
        LOG.error("Error no controlado en {}: {}", req.getRequestURI(), ex.toString(), ex);
        return construir(CodigoError.ERROR_INTERNO,
                "Ocurrio un error interno al procesar la solicitud", null, req);
    }

    private ResponseEntity<SobreError> construir(CodigoError codigo, String mensaje,
                                                 List<String> detalles, HttpServletRequest req) {
        SobreError sobre = SobreError.de(codigo.httpStatus(), codigo.titulo(), codigo.codigo(),
                mensaje, detalles, req.getRequestURI());
        return ResponseEntity.status(codigo.httpStatus()).body(sobre);
    }
}
