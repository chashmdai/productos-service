package cl.smid.productos.config;

import cl.smid.productos.dominio.puerto.salida.ClienteCasos;
import cl.smid.productos.dominio.puerto.salida.ClientePersonas;
import cl.smid.productos.dominio.puerto.salida.CorrelativoProductoPort;
import cl.smid.productos.dominio.puerto.salida.DirectorioSedes;
import cl.smid.productos.dominio.puerto.salida.GeneradorIdentificadores;
import cl.smid.productos.dominio.puerto.salida.PublicadorEventos;
import cl.smid.productos.dominio.puerto.salida.Reloj;
import cl.smid.productos.dominio.puerto.salida.RepositorioProductos;
import cl.smid.productos.dominio.puerto.salida.RepositorioTareas;
import cl.smid.productos.dominio.servicio.EvaluadorAlcance;
import cl.smid.productos.dominio.servicio.GeneradorNumeroProducto;
import cl.smid.productos.dominio.servicio.MaquinaEstadosProducto;
import cl.smid.productos.dominio.servicio.MaquinaEstadosTarea;
import cl.smid.productos.dominio.servicio.ServicioProductos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Cableado del nucleo hexagonal: convierte los servicios de dominio (POJOs puros) en beans de
 * Spring inyectandoles sus puertos de salida (cuyas implementaciones son los adaptadores).
 *
 * <p>El orquestador {@link ServicioProductos} implementa los dos puertos de entrada
 * ({@code GestionProductos} y {@code GestionTareas}); se expone como un unico bean que satisface
 * ambas dependencias en los controladores. Los parametros de politica (roles de Coordinacion y
 * bandera de enriquecimiento de Casos) provienen de las propiedades tipadas.</p>
 */
@Configuration
public class CableadoDominio {

    @Bean
    public MaquinaEstadosProducto maquinaEstadosProducto() {
        return new MaquinaEstadosProducto();
    }

    @Bean
    public MaquinaEstadosTarea maquinaEstadosTarea() {
        return new MaquinaEstadosTarea();
    }

    @Bean
    public EvaluadorAlcance evaluadorAlcance() {
        return new EvaluadorAlcance();
    }

    @Bean
    public GeneradorNumeroProducto generadorNumeroProducto(CorrelativoProductoPort correlativo,
                                                           DirectorioSedes directorioSedes) {
        return new GeneradorNumeroProducto(correlativo, directorioSedes);
    }

    @Bean
    public ServicioProductos servicioProductos(RepositorioProductos repositorioProductos,
                                               RepositorioTareas repositorioTareas,
                                               MaquinaEstadosProducto maquinaProducto,
                                               MaquinaEstadosTarea maquinaTarea,
                                               GeneradorNumeroProducto generadorNumero,
                                               EvaluadorAlcance evaluadorAlcance,
                                               PublicadorEventos publicador,
                                               Reloj reloj,
                                               GeneradorIdentificadores generadorIds,
                                               ClienteCasos clienteCasos,
                                               ClientePersonas clientePersonas,
                                               PropiedadesSeguridad propiedadesSeguridad,
                                               PropiedadesEnriquecimiento propiedadesEnriquecimiento) {
        return new ServicioProductos(
                repositorioProductos,
                repositorioTareas,
                maquinaProducto,
                maquinaTarea,
                generadorNumero,
                evaluadorAlcance,
                publicador,
                reloj,
                generadorIds,
                clienteCasos,
                clientePersonas,
                Set.copyOf(propiedadesSeguridad.rolesCoordinacion()),
                propiedadesEnriquecimiento.casos());
    }
}
