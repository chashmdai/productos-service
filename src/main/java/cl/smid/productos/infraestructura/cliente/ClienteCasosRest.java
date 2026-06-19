package cl.smid.productos.infraestructura.cliente;

import cl.smid.productos.config.PropiedadesEnriquecimiento;
import cl.smid.productos.dominio.modelo.ResumenCaso;
import cl.smid.productos.dominio.puerto.salida.ClienteCasos;
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
 * Cliente REST del servicio de Casos (6.4). Activo con {@code smid.enriquecimiento.casos=true}.
 * Propaga el bearer del solicitante (obtenido del contexto de la peticion) y degrada a vacio ante
 * cualquier indisponibilidad o respuesta no exitosa, para no bloquear la operacion de negocio.
 */
@Component
@ConditionalOnProperty(name = "smid.enriquecimiento.casos", havingValue = "true")
public class ClienteCasosRest implements ClienteCasos {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteCasosRest.class);

    private final RestClient restClient;
    private final ProveedorContexto proveedorContexto;

    public ClienteCasosRest(PropiedadesEnriquecimiento propiedades, ProveedorContexto proveedorContexto) {
        String casosUrlBase = propiedades.casosUrlBase();
        if (casosUrlBase == null || casosUrlBase.isBlank()) {
            throw new IllegalStateException(
                    "smid.enriquecimiento.casos esta activo pero falta smid.enriquecimiento.casos-url-base");
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restClient = RestClient.builder()
                .baseUrl(Objects.requireNonNull(casosUrlBase))
                .requestFactory(factory)
                .build();
        this.proveedorContexto = proveedorContexto;
    }

    @Override
    public Optional<ResumenCaso> obtener(String idCasoAlt) {
        try {
            RespuestaCaso cuerpo = restClient.get()
                    .uri("/{id}", idCasoAlt)
                    .headers(h -> {
                        String token = proveedorContexto.tokenActual();
                        if (token != null) {
                            h.setBearerAuth(token);
                        }
                    })
                    .retrieve()
                    .body(RespuestaCaso.class);
            if (cuerpo == null) {
                return Optional.empty();
            }
            return Optional.of(new ResumenCaso(cuerpo.altKey(), cuerpo.numeroExpediente(),
                    cuerpo.estado(), cuerpo.idSede(), cuerpo.idUnidad(), cuerpo.complejidad()));
        } catch (RuntimeException e) {
            LOG.debug("No se pudo obtener el caso {}: {}", idCasoAlt, e.getMessage());
            return Optional.empty();
        }
    }

    /** Proyeccion de la respuesta del servicio de Casos (campos no usados se ignoran). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RespuestaCaso(
            String altKey,
            String numeroExpediente,
            String estado,
            String idSede,
            String idUnidad,
            String complejidad
    ) {
    }
}
