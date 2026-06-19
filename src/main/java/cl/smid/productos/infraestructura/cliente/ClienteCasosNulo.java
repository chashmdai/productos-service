package cl.smid.productos.infraestructura.cliente;

import cl.smid.productos.dominio.modelo.ResumenCaso;
import cl.smid.productos.dominio.puerto.salida.ClienteCasos;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementacion nula del cliente de Casos: activa por defecto
 * ({@code smid.enriquecimiento.casos=false} o ausente). Siempre devuelve vacio, de modo que la
 * herencia territorial recae en el contexto del usuario.
 */
@Component
@ConditionalOnProperty(name = "smid.enriquecimiento.casos", havingValue = "false", matchIfMissing = true)
public class ClienteCasosNulo implements ClienteCasos {

    @Override
    public Optional<ResumenCaso> obtener(String idCasoAlt) {
        return Optional.empty();
    }
}
