package cl.smid.productos.infraestructura.soporte;

import cl.smid.productos.dominio.puerto.salida.GeneradorIdentificadores;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementacion de produccion del puerto {@link GeneradorIdentificadores}: UUID version 4 en
 * formato canonico de 36 caracteres.
 */
@Component
public class GeneradorUuid implements GeneradorIdentificadores {

    @Override
    public String nuevo() {
        return UUID.randomUUID().toString();
    }
}
