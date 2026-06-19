package cl.smid.productos.infraestructura.eventos;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion del consumo de eventos {@code caso.abierto}. Se activa con
 * {@code smid.eventos.consumo=rabbitmq}. Declara la cola de consumo con su exchange de mensajes
 * muertos (DLX) y la cola muerta (DLQ), de modo que un mensaje rechazado y no reencolado termine
 * en la DLQ para inspeccion en lugar de perderse o reentrar en bucle.
 */
@Configuration
@ConditionalOnProperty(name = "smid.eventos.consumo", havingValue = "rabbitmq")
public class ConfiguracionRabbitConsumo {

    public static final String COLA_CASO_ABIERTO = "productos.caso-abierto";
    public static final String EXCHANGE_DLX = "smid.eventos.dlx";
    public static final String COLA_DLQ = "productos.caso-abierto.dlq";
    public static final String RK_CASO_ABIERTO = "caso.abierto";

    @Bean
    public TopicExchange smidEventosDlx() {
        return new TopicExchange(EXCHANGE_DLX, true, false);
    }

    @Bean
    public Queue colaCasoAbierto() {
        return QueueBuilder.durable(COLA_CASO_ABIERTO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", RK_CASO_ABIERTO)
                .build();
    }

    @Bean
    public Queue colaCasoAbiertoDlq() {
        return QueueBuilder.durable(COLA_DLQ).build();
    }

    @Bean
    public Binding bindingCasoAbierto(Queue colaCasoAbierto, TopicExchange smidEventosExchange) {
        return BindingBuilder.bind(colaCasoAbierto).to(smidEventosExchange).with(RK_CASO_ABIERTO);
    }

    @Bean
    public Binding bindingCasoAbiertoDlq(Queue colaCasoAbiertoDlq, TopicExchange smidEventosDlx) {
        return BindingBuilder.bind(colaCasoAbiertoDlq).to(smidEventosDlx).with(RK_CASO_ABIERTO);
    }
}
