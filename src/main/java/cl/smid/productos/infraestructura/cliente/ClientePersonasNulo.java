package cl.smid.productos.infraestructura.cliente;

import cl.smid.productos.dominio.puerto.salida.ClientePersonas;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementacion nula del cliente de Personas: activa por defecto
 * ({@code smid.enriquecimiento.personas=false} o ausente). Devuelve vacio, de modo que las tareas
 * no guardan snapshot de nombre del responsable.
 */
@Component
@ConditionalOnProperty(name = "smid.enriquecimiento.personas", havingValue = "false", matchIfMissing = true)
public class ClientePersonasNulo implements ClientePersonas {

    @Override
    public Optional<String> nombreLegible(String idPersonaAlt) {
        return Optional.empty();
    }
}
