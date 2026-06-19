package cl.smid.productos.infraestructura.eventos;

import cl.smid.productos.dominio.modelo.EventoDominio;
import cl.smid.productos.dominio.puerto.salida.PublicadorEventos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos basado en registro (log). Es el transporte por defecto
 * ({@code smid.eventos.transporte=log} o ausente): no requiere broker y resulta ideal para
 * desarrollo local y pruebas. La publicacion es tolerante a fallos por construccion (solo escribe
 * una linea de log).
 */
@Component
@ConditionalOnProperty(name = "smid.eventos.transporte", havingValue = "log", matchIfMissing = true)
public class PublicadorEventosLog implements PublicadorEventos {

    private static final Logger LOG = LoggerFactory.getLogger(PublicadorEventosLog.class);

    @Override
    public void publicar(EventoDominio evento) {
        LOG.info("[EVENTO] tipo={} altKey={} ocurridoEn={} metadatos={}",
                evento.tipo(), evento.altKey(), evento.ocurridoEn(), evento.metadatos());
    }
}
