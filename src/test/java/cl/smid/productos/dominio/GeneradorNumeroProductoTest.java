package cl.smid.productos.dominio;

import cl.smid.productos.dominio.dobles.CorrelativoEnMemoria;
import cl.smid.productos.dominio.dobles.DirectorioSedesFijo;
import cl.smid.productos.dominio.modelo.NumeroProducto;
import cl.smid.productos.dominio.servicio.GeneradorNumeroProducto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Generador del numero oficial del Producto")
class GeneradorNumeroProductoTest {

    @Test
    @DisplayName("Formato PRD-{SEDE}-{N}/{ANIO} y correlativo creciente por sede/anio")
    void formatoYCorrelativo() {
        GeneradorNumeroProducto generador =
                new GeneradorNumeroProducto(new CorrelativoEnMemoria(), new DirectorioSedesFijo("RM"));

        NumeroProducto primero = generador.generar("sede-1", 2027);
        NumeroProducto segundo = generador.generar("sede-1", 2027);

        assertEquals("PRD-RM-1/2027", primero.valor());
        assertEquals("PRD-RM-2/2027", segundo.valor());
    }

    @Test
    @DisplayName("Series independientes por anio")
    void seriesPorAnio() {
        GeneradorNumeroProducto generador =
                new GeneradorNumeroProducto(new CorrelativoEnMemoria(), new DirectorioSedesFijo("RM"));

        assertEquals("PRD-RM-1/2027", generador.generar("sede-1", 2027).valor());
        assertEquals("PRD-RM-1/2028", generador.generar("sede-1", 2028).valor());
    }
}
