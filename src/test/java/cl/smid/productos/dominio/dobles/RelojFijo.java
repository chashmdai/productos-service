package cl.smid.productos.dominio.dobles;

import cl.smid.productos.dominio.puerto.salida.Reloj;

import java.time.Instant;

/** Reloj de prueba que devuelve siempre un instante fijo. */
public class RelojFijo implements Reloj {

    private final Instant instante;

    public RelojFijo(Instant instante) {
        this.instante = instante;
    }

    @Override
    public Instant ahora() {
        return instante;
    }
}
