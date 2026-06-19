package cl.smid.productos.infraestructura.soporte;

import cl.smid.productos.config.PropiedadesSedes;
import cl.smid.productos.dominio.puerto.salida.DirectorioSedes;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implementacion del puerto {@link DirectorioSedes} respaldada por propiedades. Resuelve el codigo
 * corto de la sede desde el mapa configurado y, en su ausencia, devuelve el codigo por defecto.
 */
@Component
public class DirectorioSedesConfig implements DirectorioSedes {

    private final Map<String, String> codigos;
    private final String defecto;

    public DirectorioSedesConfig(PropiedadesSedes propiedades) {
        this.codigos = propiedades.codigos();
        this.defecto = propiedades.defecto();
    }

    @Override
    public String codigoDe(String idSedeAlt) {
        if (idSedeAlt == null || idSedeAlt.isBlank()) {
            return defecto;
        }
        return codigos.getOrDefault(idSedeAlt, defecto);
    }
}
