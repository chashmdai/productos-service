package cl.smid.productos.dominio.dobles;

import cl.smid.productos.dominio.puerto.salida.ClientePersonas;

import java.util.Optional;

/**
 * Cliente de Personas de prueba. Devuelve el nombre configurado (o vacio si es nulo) para
 * cualquier identificador.
 */
public class ClientePersonasDoble implements ClientePersonas {

    private final String nombre;

    public ClientePersonasDoble() {
        this.nombre = null;
    }

    public ClientePersonasDoble(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public Optional<String> nombreLegible(String idPersonaAlt) {
        return Optional.ofNullable(nombre);
    }
}
