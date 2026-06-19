package cl.smid.productos.dominio.puerto.salida;

import cl.smid.productos.dominio.modelo.EventoDominio;

/**
 * Puerto de publicacion de eventos de dominio hacia el bus corporativo.
 *
 * <p>La publicacion es <strong>tolerante a fallos</strong>: un error de transporte no debe abortar
 * la operacion de negocio ya confirmada (la implementacion captura y registra). El transporte es
 * conmutable (log o RabbitMQ) sin que el dominio lo perciba. Solo se publican metadatos no
 * sensibles (override 8).</p>
 */
public interface PublicadorEventos {

    /** Publica el evento; nunca propaga excepciones de transporte. */
    void publicar(EventoDominio evento);
}
