package cl.smid.productos.dominio.dobles;

import cl.smid.productos.dominio.puerto.salida.GeneradorIdentificadores;

import java.util.concurrent.atomic.AtomicLong;

/** Generador determinista de identificadores: {@code id-1}, {@code id-2}, ... */
public class GeneradorSecuencial implements GeneradorIdentificadores {

    private final AtomicLong contador = new AtomicLong(0);

    @Override
    public String nuevo() {
        return "id-" + contador.incrementAndGet();
    }
}
