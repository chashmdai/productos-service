package cl.smid.productos.dominio.dobles;

import cl.smid.productos.dominio.puerto.salida.DirectorioSedes;

/** Directorio de sedes de prueba que devuelve un codigo fijo para cualquier sede. */
public class DirectorioSedesFijo implements DirectorioSedes {

    private final String codigo;

    public DirectorioSedesFijo(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigoDe(String idSedeAlt) {
        return codigo;
    }
}
