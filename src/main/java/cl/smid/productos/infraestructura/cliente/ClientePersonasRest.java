package cl.smid.productos.infraestructura.cliente;

import cl.smid.productos.config.PropiedadesEnriquecimiento;
import cl.smid.productos.dominio.puerto.salida.ClientePersonas;
import cl.smid.productos.infraestructura.seguridad.ProveedorContexto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.Optional;

/**
 * Cliente REST del servicio de Personas (6.2). Activo con
 * {@code smid.enriquecimiento.personas=true}. Resuelve el nombre legible (no sensible) del
 * responsable, propagando el bearer y degradando a vacio ante indisponibilidad.
 */
@Component
@ConditionalOnProperty(name = "smid.enriquecimiento.personas", havingValue = "true")
public class ClientePersonasRest implements ClientePersonas {

    private static final Logger LOG = LoggerFactory.getLogger(ClientePersonasRest.class);

    private final RestClient restClient;
    private final ProveedorContexto proveedorContexto;

    public ClientePersonasRest(PropiedadesEnriquecimiento propiedades, ProveedorContexto proveedorContexto) {
        String personasUrlBase = propiedades.personasUrlBase();
        if (personasUrlBase == null || personasUrlBase.isBlank()) {
            throw new IllegalStateException(
                    "smid.enriquecimiento.personas esta activo pero falta smid.enriquecimiento.personas-url-base");
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restClient = RestClient.builder()
                .baseUrl(Objects.requireNonNull(personasUrlBase))
                .requestFactory(factory)
                .build();
        this.proveedorContexto = proveedorContexto;
    }

    @Override
    public Optional<String> nombreLegible(String idPersonaAlt) {
        try {
            RespuestaPersona cuerpo = restClient.get()
                    .uri("/{id}", idPersonaAlt)
                    .headers(h -> {
                        String token = proveedorContexto.tokenActual();
                        if (token != null) {
                            h.setBearerAuth(token);
                        }
                    })
                    .retrieve()
                    .body(RespuestaPersona.class);
            if (cuerpo == null || cuerpo.nombreLegible() == null || cuerpo.nombreLegible().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(cuerpo.nombreLegible());
        } catch (RuntimeException e) {
            LOG.debug("No se pudo obtener la persona {}: {}", idPersonaAlt, e.getMessage());
            return Optional.empty();
        }
    }

    /** Proyeccion de la respuesta del servicio de Personas (campos no usados se ignoran). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RespuestaPersona(String altKey, String nombreLegible) {
    }
}
