package cl.smid.productos.infraestructura.eventos;

import cl.smid.productos.dominio.modelo.EventoDominio;
import cl.smid.productos.dominio.modelo.PrioridadTarea;
import cl.smid.productos.dominio.modelo.Tarea;
import cl.smid.productos.dominio.modelo.Transicion;
import cl.smid.productos.dominio.puerto.salida.GeneradorIdentificadores;
import cl.smid.productos.dominio.puerto.salida.PublicadorEventos;
import cl.smid.productos.dominio.puerto.salida.Reloj;
import cl.smid.productos.dominio.puerto.salida.RepositorioTareas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Listener opcional que, al recibir {@code caso.abierto}, siembra una tarea inicial
 * "Plan de trabajo" para el Caso. Se activa con {@code smid.eventos.consumo=rabbitmq}.
 *
 * <p><b>Reglas:</b> el actor es el de sistema (no hay usuario); <strong>nunca</strong> usa los
 * clientes de enriquecimiento (hereda sede/unidad de los metadatos del propio evento); es
 * idempotente mediante la unicidad {@code (id_caso_alt, tipo_semilla)}; y los mensajes malformados
 * se rechazan sin reencolar ({@link AmqpRejectAndDontRequeueException}) para que terminen en la
 * DLQ. La frontera transaccional del consumo es este metodo.</p>
 */
@Component
@ConditionalOnProperty(name = "smid.eventos.consumo", havingValue = "rabbitmq")
public class ListenerCasoAbierto {

    private static final Logger LOG = LoggerFactory.getLogger(ListenerCasoAbierto.class);

    /** Marca de semilla del plan de trabajo (clave de idempotencia junto al Caso). */
    public static final String SEMILLA_PLAN_TRABAJO = "PLAN_TRABAJO";

    private static final String TITULO_PLAN = "Plan de trabajo inicial";

    private final RepositorioTareas repositorioTareas;
    private final PublicadorEventos publicador;
    private final Reloj reloj;
    private final GeneradorIdentificadores generadorIds;

    public ListenerCasoAbierto(RepositorioTareas repositorioTareas,
                               PublicadorEventos publicador,
                               Reloj reloj,
                               GeneradorIdentificadores generadorIds) {
        this.repositorioTareas = repositorioTareas;
        this.publicador = publicador;
        this.reloj = reloj;
        this.generadorIds = generadorIds;
    }

    @RabbitListener(queues = ConfiguracionRabbitConsumo.COLA_CASO_ABIERTO)
    @Transactional
    public void alAbrirCaso(CasoAbiertoEvento evento) {
        if (evento == null || evento.altKey() == null || evento.altKey().isBlank()) {
            throw new AmqpRejectAndDontRequeueException(
                    "Evento caso.abierto malformado: falta altKey del Caso");
        }
        String idCasoAlt = evento.altKey();

        if (repositorioTareas.existeSemilla(idCasoAlt, SEMILLA_PLAN_TRABAJO)) {
            LOG.debug("Semilla ya existente para caso={}, se omite (idempotencia)", idCasoAlt);
            return;
        }

        String idSedeAlt = evento.metadato("idSede");
        String idUnidadAlt = evento.metadato("idUnidad");

        Instant ahora = reloj.ahora();
        String altKey = generadorIds.nuevo();
        String altKeyAsiento = generadorIds.nuevo();

        Tarea tarea = Tarea.crear(altKey, idCasoAlt, null, TITULO_PLAN,
                "Tarea generada automaticamente al abrir el caso.", null, null,
                PrioridadTarea.MEDIA, null, idSedeAlt, idUnidadAlt, SEMILLA_PLAN_TRABAJO,
                Transicion.ACTOR_SISTEMA, altKeyAsiento, ahora);
        repositorioTareas.crear(tarea);

        publicador.publicar(EventoDominio.de("tarea.creada", altKey, ahora,
                "idCasoAlt", idCasoAlt, "prioridad", PrioridadTarea.MEDIA.name()));

        LOG.info("Tarea semilla creada para caso={} tareaAltKey={}", idCasoAlt, altKey);
    }
}
