package cl.smid.productos.dominio;

import cl.smid.productos.dominio.excepcion.ConflictoEstado;
import cl.smid.productos.dominio.modelo.AccionTarea;
import cl.smid.productos.dominio.modelo.EstadoTarea;
import cl.smid.productos.dominio.servicio.MaquinaEstadosTarea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Maquina de estados de la Tarea")
class MaquinaEstadosTareaTest {

    private final MaquinaEstadosTarea maquina = new MaquinaEstadosTarea();

    @Test
    @DisplayName("PENDIENTE --TOMAR--> EN_CURSO")
    void tomarDesdePendiente() {
        assertEquals(EstadoTarea.EN_CURSO,
                maquina.siguiente(EstadoTarea.PENDIENTE, AccionTarea.TOMAR));
    }

    @Test
    @DisplayName("EN_CURSO --COMPLETAR--> COMPLETADA")
    void completarDesdeEnCurso() {
        assertEquals(EstadoTarea.COMPLETADA,
                maquina.siguiente(EstadoTarea.EN_CURSO, AccionTarea.COMPLETAR));
    }

    @Test
    @DisplayName("PENDIENTE y EN_CURSO --CANCELAR--> CANCELADA")
    void cancelarDesdeEstadosVivos() {
        assertEquals(EstadoTarea.CANCELADA,
                maquina.siguiente(EstadoTarea.PENDIENTE, AccionTarea.CANCELAR));
        assertEquals(EstadoTarea.CANCELADA,
                maquina.siguiente(EstadoTarea.EN_CURSO, AccionTarea.CANCELAR));
    }

    @Test
    @DisplayName("COMPLETAR desde PENDIENTE es invalido")
    void completarDesdePendienteEsInvalido() {
        assertThrows(ConflictoEstado.class,
                () -> maquina.siguiente(EstadoTarea.PENDIENTE, AccionTarea.COMPLETAR));
    }

    @Test
    @DisplayName("REASIGNAR no es transicion de estado: IllegalArgumentException")
    void reasignarNoEsTransicion() {
        assertThrows(IllegalArgumentException.class,
                () -> maquina.siguiente(EstadoTarea.PENDIENTE, AccionTarea.REASIGNAR));
    }
}
