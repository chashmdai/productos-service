package cl.smid.productos.dominio.puerto.entrada;

import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.modelo.Tarea;
import cl.smid.productos.dominio.modelo.vista.PaginaDominio;
import cl.smid.productos.dominio.puerto.entrada.comando.CrearTareaComando;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroTareas;
import cl.smid.productos.dominio.puerto.entrada.comando.TransicionTareaComando;

/**
 * Puerto de entrada para la gestion de Tareas (caso de uso). Lo implementa el mismo orquestador
 * de dominio que {@link GestionProductos}, de modo que un unico bean concentra ambos casos de uso.
 */
public interface GestionTareas {

    /**
     * Crea una Tarea bajo un Producto existente y mutable, heredando Caso/sede/unidad del Producto.
     * Publica {@code tarea.creada}.
     */
    Tarea crearBajoProducto(CrearTareaComando comando);

    /**
     * Crea una Tarea suelta ligada directamente a un Caso, heredando sede/unidad del Caso o del
     * contexto. Publica {@code tarea.creada}.
     */
    Tarea crearSuelta(CrearTareaComando comando);

    /** Detalle de una Tarea sujeto a alcance territorial. */
    Tarea detalleTarea(ContextoSesion contexto, String altKey);

    /** Listado paginado de Tareas segun filtros y alcance territorial. */
    PaginaDominio<Tarea> listar(FiltroTareas filtro);

    /** Aplica una transicion de Tarea (TOMAR, COMPLETAR, CANCELAR, REASIGNAR) con sus permisos. */
    Tarea transicionar(TransicionTareaComando comando);
}
