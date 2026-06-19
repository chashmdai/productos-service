package cl.smid.productos.infraestructura.eventos;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion RabbitMQ comun a la publicacion y al consumo. Se activa cuando cualquiera de los
 * dos lados usa RabbitMQ ({@code smid.eventos.transporte=rabbitmq} o
 * {@code smid.eventos.consumo=rabbitmq}); en el perfil local (log + none) no se declara nada y no
 * se intenta conexion con el broker.
 *
 * <p>Declara el exchange de dominio {@code smid.eventos} (topic) y el conversor JSON. El conversor
 * usa precedencia de tipo <em>inferida</em> para deserializar segun la firma del listener,
 * ignorando el encabezado {@code __TypeId__} que pudiera traer el productor de otro servicio.</p>
 */
@Configuration
@ConditionalOnExpression(
        "'${smid.eventos.transporte:log}' == 'rabbitmq' or '${smid.eventos.consumo:none}' == 'rabbitmq'")
public class ConfiguracionRabbitComun {

    /** Nombre del exchange de dominio compartido por el ecosistema SMID. */
    public static final String EXCHANGE_DOMINIO = "smid.eventos";

    @Bean
    public TopicExchange smidEventosExchange() {
        return new TopicExchange(EXCHANGE_DOMINIO, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter conversorEventosJson() {
        Jackson2JsonMessageConverter conversor = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper mapeador = new DefaultJackson2JavaTypeMapper();
        mapeador.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        mapeador.setTrustedPackages("*");
        conversor.setJavaTypeMapper(mapeador);
        return conversor;
    }
}
