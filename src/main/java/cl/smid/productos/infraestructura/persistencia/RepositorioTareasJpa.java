package cl.smid.productos.infraestructura.persistencia;

import cl.smid.productos.dominio.excepcion.RecursoNoEncontrado;
import cl.smid.productos.dominio.modelo.Tarea;
import cl.smid.productos.dominio.modelo.Transicion;
import cl.smid.productos.dominio.modelo.vista.PaginaDominio;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroTareas;
import cl.smid.productos.dominio.puerto.salida.RepositorioTareas;
import cl.smid.productos.infraestructura.persistencia.entidad.TareaEntidad;
import cl.smid.productos.infraestructura.persistencia.entidad.TareaTransicionEntidad;
import cl.smid.productos.infraestructura.persistencia.mapeo.MapeadorTarea;
import cl.smid.productos.infraestructura.persistencia.repositorio.TareaJpaRepository;
import cl.smid.productos.infraestructura.persistencia.repositorio.TareaTransicionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de persistencia del puerto {@link RepositorioTareas} sobre Spring Data JPA. Analogo a
 * {@link RepositorioProductosJpa}: no abre transacciones propias y persiste agregado mas asientos.
 */
@Repository
public class RepositorioTareasJpa implements RepositorioTareas {

    private final TareaJpaRepository tareaJpa;
    private final TareaTransicionJpaRepository transicionJpa;

    public RepositorioTareasJpa(TareaJpaRepository tareaJpa,
                                TareaTransicionJpaRepository transicionJpa) {
        this.tareaJpa = tareaJpa;
        this.transicionJpa = transicionJpa;
    }

    @Override
    public void crear(Tarea tarea) {
        TareaEntidad nueva = Objects.requireNonNull(MapeadorTarea.aEntidadNueva(tarea));
        TareaEntidad guardada = tareaJpa.save(nueva);
        insertarTransiciones(tarea.transicionesPendientes(), guardada.getId());
    }

    @Override
    public void actualizar(Tarea tarea) {
        TareaEntidad entidad = tareaJpa.findByAltKey(tarea.altKey())
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "Tarea no encontrada para actualizar: " + tarea.altKey()));
        MapeadorTarea.copiarADestino(tarea, entidad);
        tareaJpa.save(Objects.requireNonNull(entidad));
        insertarTransiciones(tarea.transicionesPendientes(), entidad.getId());
    }

    @Override
    public Optional<Tarea> buscarPorAltKey(String altKey) {
        return tareaJpa.findByAltKey(altKey).map(entidad -> {
            List<Transicion> historial = transicionJpa
                    .findByTareaIdOrderByOcurridoEnAscIdAsc(entidad.getId())
                    .stream()
                    .map(MapeadorTarea::transicionADominio)
                    .toList();
            return MapeadorTarea.aDominio(entidad, historial);
        });
    }

    @Override
    public PaginaDominio<Tarea> listar(FiltroTareas filtro) {
        Specification<TareaEntidad> spec = TareaSpecs.desdeFiltro(filtro);
        Pageable pageable = PageRequest.of(filtro.pagina(), filtro.tamano(),
                Sort.by(Sort.Direction.DESC, "creadoEn"));
        Page<TareaEntidad> pagina = tareaJpa.findAll(spec, pageable);
        List<Tarea> contenido = pagina.getContent().stream()
                .map(e -> MapeadorTarea.aDominio(e, List.of()))
                .toList();
        return new PaginaDominio<>(contenido, pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }

    @Override
    public List<Tarea> listarPorProducto(String idProductoAlt) {
        return tareaJpa.findByIdProductoAltOrderByCreadoEnAscIdAsc(idProductoAlt).stream()
                .map(e -> MapeadorTarea.aDominio(e, List.of()))
                .toList();
    }

    @Override
    public boolean existeSemilla(String idCasoAlt, String tipoSemilla) {
        return tareaJpa.existsByIdCasoAltAndTipoSemilla(idCasoAlt, tipoSemilla);
    }

    private void insertarTransiciones(List<Transicion> pendientes, Long tareaId) {
        if (pendientes.isEmpty()) {
            return;
        }
        List<TareaTransicionEntidad> entidades = pendientes.stream()
                .map(t -> MapeadorTarea.transicionAEntidad(t, tareaId))
                .toList();
        transicionJpa.saveAll(Objects.requireNonNull(entidades));
    }
}
