package cl.smid.productos.infraestructura.eventos;

import cl.smid.productos.dominio.modelo.EventoDominio;
import cl.smid.productos.dominio.puerto.salida.PublicadorEventos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos sobre RabbitMQ. Se activa con {@code smid.eventos.transporte=rabbitmq}.
 * Envia el {@link SobreEvento} al exchange de dominio usando el tipo del evento como routing key.
 *
 * <p><b>Tolerante a fallos (override 8).</b> Un error de transporte se registra pero no se propaga:
 * la operacion de negocio ya confirmada no debe abortarse por la indisponibilidad del bus.</p>
 */
@Component
@ConditionalOnProperty(name = "smid.eventos.transporte", havingValue = "rabbitmq")
public class PublicadorEventosRabbit implements PublicadorEventos {

    private static final Logger LOG = LoggerFactory.getLogger(PublicadorEventosRabbit.class);

    private final RabbitTemplate rabbitTemplate;

    public PublicadorEventosRabbit(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(EventoDominio evento) {
        try {
            rabbitTemplate.convertAndSend(
                    ConfiguracionRabbitComun.EXCHANGE_DOMINIO,
                    evento.tipo(),
                    SobreEvento.desde(evento));
        } catch (AmqpException e) {
            LOG.error("No se pudo publicar el evento tipo={} altKey={}: {}",
                    evento.tipo(), evento.altKey(), e.getMessage());
        }
    }
}
