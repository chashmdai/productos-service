package cl.smid.productos.dominio;

import cl.smid.productos.dominio.modelo.Alcance;
import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.servicio.EvaluadorAlcance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Evaluador de alcance territorial")
class EvaluadorAlcanceTest {

    private final EvaluadorAlcance evaluador = new EvaluadorAlcance();

    private ContextoSesion contexto(Alcance alcance, String sede, String unidad) {
        return new ContextoSesion("user-1", List.of("OPERADOR"), sede, unidad, alcance, "Usuario");
    }

    @Test
    @DisplayName("NACIONAL ve cualquier registro")
    void nacionalVeTodo() {
        ContextoSesion ctx = contexto(Alcance.NACIONAL, null, null);
        assertTrue(evaluador.puedeVer("sede-9", "unidad-9", ctx));
    }

    @Test
    @DisplayName("SEDE solo ve registros de su sede")
    void sedeFiltraPorSede() {
        ContextoSesion ctx = contexto(Alcance.SEDE, "sede-1", "unidad-1");
        assertTrue(evaluador.puedeVer("sede-1", "unidad-7", ctx));
        assertFalse(evaluador.puedeVer("sede-2", "unidad-1", ctx));
    }

    @Test
    @DisplayName("UNIDAD solo ve registros de su unidad")
    void unidadFiltraPorUnidad() {
        ContextoSesion ctx = contexto(Alcance.UNIDAD, "sede-1", "unidad-1");
        assertTrue(evaluador.puedeVer("sede-1", "unidad-1", ctx));
        assertFalse(evaluador.puedeVer("sede-1", "unidad-2", ctx));
    }
}
