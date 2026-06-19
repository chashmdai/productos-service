package cl.smid.productos.infraestructura.persistencia;

import cl.smid.productos.dominio.excepcion.RecursoNoEncontrado;
import cl.smid.productos.dominio.modelo.Producto;
import cl.smid.productos.dominio.modelo.Transicion;
import cl.smid.productos.dominio.modelo.vista.PaginaDominio;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroProductos;
import cl.smid.productos.dominio.puerto.salida.RepositorioProductos;
import cl.smid.productos.infraestructura.persistencia.entidad.ProductoEntidad;
import cl.smid.productos.infraestructura.persistencia.entidad.ProductoTransicionEntidad;
import cl.smid.productos.infraestructura.persistencia.mapeo.MapeadorProducto;
import cl.smid.productos.infraestructura.persistencia.repositorio.ProductoJpaRepository;
import cl.smid.productos.infraestructura.persistencia.repositorio.ProductoTransicionJpaRepository;
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
 * Adaptador de persistencia del puerto {@link RepositorioProductos} sobre Spring Data JPA.
 *
 * <p>No abre transacciones propias: participa en la transaccion de la frontera (controlador o
 * listener). Las escrituras persisten el agregado y, a continuacion, sus transiciones pendientes
 * ligadas por la PK interna recien obtenida.</p>
 */
@Repository
public class RepositorioProductosJpa implements RepositorioProductos {

    private final ProductoJpaRepository productoJpa;
    private final ProductoTransicionJpaRepository transicionJpa;

    public RepositorioProductosJpa(ProductoJpaRepository productoJpa,
                                   ProductoTransicionJpaRepository transicionJpa) {
        this.productoJpa = productoJpa;
        this.transicionJpa = transicionJpa;
    }

    @Override
    public void crear(Producto producto) {
        ProductoEntidad nueva = Objects.requireNonNull(MapeadorProducto.aEntidadNueva(producto));
        ProductoEntidad guardada = productoJpa.save(nueva);
        insertarTransiciones(producto.transicionesPendientes(), guardada.getId());
    }

    @Override
    public void actualizar(Producto producto) {
        ProductoEntidad entidad = productoJpa.findByAltKey(producto.altKey())
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "Producto no encontrado para actualizar: " + producto.altKey()));
        MapeadorProducto.copiarADestino(producto, entidad);
        productoJpa.save(Objects.requireNonNull(entidad));
        insertarTransiciones(producto.transicionesPendientes(), entidad.getId());
    }

    @Override
    public Optional<Producto> buscarPorAltKey(String altKey) {
        return productoJpa.findByAltKey(altKey).map(entidad -> {
            List<Transicion> historial = transicionJpa
                    .findByProductoIdOrderByOcurridoEnAscIdAsc(entidad.getId())
                    .stream()
                    .map(MapeadorProducto::transicionADominio)
                    .toList();
            return MapeadorProducto.aDominio(entidad, historial);
        });
    }

    @Override
    public PaginaDominio<Producto> listar(FiltroProductos filtro) {
        Specification<ProductoEntidad> spec = ProductoSpecs.desdeFiltro(filtro);
        Pageable pageable = PageRequest.of(filtro.pagina(), filtro.tamano(),
                Sort.by(Sort.Direction.DESC, "creadoEn"));
        Page<ProductoEntidad> pagina = productoJpa.findAll(spec, pageable);
        List<Producto> contenido = pagina.getContent().stream()
                .map(e -> MapeadorProducto.aDominio(e, List.of()))
                .toList();
        return new PaginaDominio<>(contenido, pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }

    private void insertarTransiciones(List<Transicion> pendientes, Long productoId) {
        if (pendientes.isEmpty()) {
            return;
        }
        List<ProductoTransicionEntidad> entidades = pendientes.stream()
                .map(t -> MapeadorProducto.transicionAEntidad(t, productoId))
                .toList();
        transicionJpa.saveAll(Objects.requireNonNull(entidades));
    }
}
