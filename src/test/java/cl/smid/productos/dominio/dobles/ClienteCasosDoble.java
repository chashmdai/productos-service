package cl.smid.productos.dominio.dobles;

import cl.smid.productos.dominio.modelo.ResumenCaso;
import cl.smid.productos.dominio.puerto.salida.ClienteCasos;

import java.util.Optional;

/**
 * Cliente de Casos de prueba. Si se construye con un {@link ResumenCaso}, lo devuelve siempre;
 * si se construye vacio, simula un caso inaccesible (Optional vacio).
 */
public class ClienteCasosDoble implements ClienteCasos {

    private final ResumenCaso resumen;

    public ClienteCasosDoble() {
        this.resumen = null;
    }

    public ClienteCasosDoble(ResumenCaso resumen) {
        this.resumen = resumen;
    }

    @Override
    public Optional<ResumenCaso> obtener(String idCasoAlt) {
        return Optional.ofNullable(resumen);
    }
}
