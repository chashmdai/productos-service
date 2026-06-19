package cl.smid.productos.dominio.dobles;

import cl.smid.productos.dominio.modelo.EventoDominio;
import cl.smid.productos.dominio.puerto.salida.PublicadorEventos;

import java.util.ArrayList;
import java.util.List;

/** Publicador de prueba que captura los eventos emitidos para su verificacion. */
public class PublicadorCapturador implements PublicadorEventos {

    private final List<EventoDominio> eventos = new ArrayList<>();

    @Override
    public void publicar(EventoDominio evento) {
        eventos.add(evento);
    }

    public List<EventoDominio> eventos() {
        return eventos;
    }

    public List<String> tipos() {
        return eventos.stream().map(EventoDominio::tipo).toList();
    }

    public EventoDominio ultimo() {
        return eventos.get(eventos.size() - 1);
    }
}
