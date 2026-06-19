package cl.smid.productos.dominio;

import cl.smid.productos.dominio.excepcion.ConflictoEstado;
import cl.smid.productos.dominio.modelo.AccionProducto;
import cl.smid.productos.dominio.modelo.EstadoProducto;
import cl.smid.productos.dominio.servicio.MaquinaEstadosProducto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Maquina de estados del Producto")
class MaquinaEstadosProductoTest {

    private final MaquinaEstadosProducto maquina = new MaquinaEstadosProducto();

    @Test
    @DisplayName("BORRADOR --ENVIAR_REVISION--> EN_REVISION")
    void enviarRevisionDesdeBorrador() {
        assertEquals(EstadoProducto.EN_REVISION,
                maquina.siguiente(EstadoProducto.BORRADOR, AccionProducto.ENVIAR_REVISION));
    }

    @Test
    @DisplayName("EN_REVISION --EMITIR--> EMITIDO")
    void emitirDesdeEnRevision() {
        assertEquals(EstadoProducto.EMITIDO,
                maquina.siguiente(EstadoProducto.EN_REVISION, AccionProducto.EMITIR));
    }

    @Test
    @DisplayName("BORRADOR y EN_REVISION --ANULAR--> ANULADO")
    void anularDesdeEstadosVivos() {
        assertEquals(EstadoProducto.ANULADO,
                maquina.siguiente(EstadoProducto.BORRADOR, AccionProducto.ANULAR));
        assertEquals(EstadoProducto.ANULADO,
                maquina.siguiente(EstadoProducto.EN_REVISION, AccionProducto.ANULAR));
    }

    @Test
    @DisplayName("EMITIR desde BORRADOR es invalido (salta la revision)")
    void emitirDesdeBorradorEsInvalido() {
        assertThrows(ConflictoEstado.class,
                () -> maquina.siguiente(EstadoProducto.BORRADOR, AccionProducto.EMITIR));
    }

    @Test
    @DisplayName("Cualquier accion sobre un estado terminal es invalida")
    void accionSobreTerminalEsInvalida() {
        assertThrows(ConflictoEstado.class,
                () -> maquina.siguiente(EstadoProducto.EMITIDO, AccionProducto.ANULAR));
        assertThrows(ConflictoEstado.class,
                () -> maquina.siguiente(EstadoProducto.ANULADO, AccionProducto.ENVIAR_REVISION));
    }
}
