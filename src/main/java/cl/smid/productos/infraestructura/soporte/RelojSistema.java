package cl.smid.productos.infraestructura.soporte;

import cl.smid.productos.dominio.puerto.salida.Reloj;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Implementacion de produccion del puerto {@link Reloj}: el reloj del sistema en UTC. */
@Component
public class RelojSistema implements Reloj {

    @Override
    public Instant ahora() {
        return Instant.now();
    }
}
