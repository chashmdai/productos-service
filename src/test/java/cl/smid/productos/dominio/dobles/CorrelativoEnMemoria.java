package cl.smid.productos.dominio.dobles;

import cl.smid.productos.dominio.puerto.salida.CorrelativoProductoPort;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Correlativo en memoria que aisla series por (sede, anio). Apto para pruebas de un solo hilo. */
public class CorrelativoEnMemoria implements CorrelativoProductoPort {

    private final Map<String, AtomicLong> series = new ConcurrentHashMap<>();

    @Override
    public long siguiente(String idSedeAlt, int anio) {
        return series.computeIfAbsent(idSedeAlt + "|" + anio, k -> new AtomicLong(0))
                .incrementAndGet();
    }
}
