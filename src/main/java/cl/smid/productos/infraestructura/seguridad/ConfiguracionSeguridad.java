package cl.smid.productos.infraestructura.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracion de la cadena de seguridad: API sin estado, autenticacion por JWT en el borde y
 * respuestas de error unificadas. Solo {@code /actuator/health} queda abierto; el resto exige
 * autenticacion. La autorizacion fina (rol de Coordinacion, permiso por responsable) se resuelve
 * en el dominio, no con expresiones declarativas.
 */
@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private final FiltroAutenticacion filtroAutenticacion;
    private final EntradaNoAutenticado entradaNoAutenticado;
    private final ManejadorAccesoDenegado manejadorAccesoDenegado;

    public ConfiguracionSeguridad(FiltroAutenticacion filtroAutenticacion,
                                  EntradaNoAutenticado entradaNoAutenticado,
                                  ManejadorAccesoDenegado manejadorAccesoDenegado) {
        this.filtroAutenticacion = filtroAutenticacion;
        this.entradaNoAutenticado = entradaNoAutenticado;
        this.manejadorAccesoDenegado = manejadorAccesoDenegado;
    }

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entradaNoAutenticado)
                        .accessDeniedHandler(manejadorAccesoDenegado))
                .addFilterBefore(filtroAutenticacion, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
