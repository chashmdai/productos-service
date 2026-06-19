package cl.smid.productos.infraestructura.seguridad;

import cl.smid.productos.dominio.modelo.ContextoSesion;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de borde que valida el JWT (cuando viene) y, si es valido, autentica la peticion y
 * deposita el {@link ContextoSesion} y el token bruto como atributos de la peticion.
 *
 * <p>Si el token falta o es invalido, no autentica: la cadena de seguridad derivara la peticion al
 * punto de entrada que responde 401 (AUTZ-003) para los recursos protegidos. No produce 401 por si
 * mismo, manteniendo una unica via de respuesta de no-autenticacion.</p>
 */
@Component
public class FiltroAutenticacion extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(FiltroAutenticacion.class);
    private static final String ENCABEZADO = "Authorization";
    private static final String PREFIJO = "Bearer ";

    private final ValidadorJwt validadorJwt;

    public FiltroAutenticacion(ValidadorJwt validadorJwt) {
        this.validadorJwt = validadorJwt;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest peticion,
                                    @NonNull HttpServletResponse respuesta,
                                    @NonNull FilterChain cadena) throws ServletException, IOException {
        String encabezado = peticion.getHeader(ENCABEZADO);
        if (encabezado != null && encabezado.startsWith(PREFIJO)) {
            String token = encabezado.substring(PREFIJO.length()).trim();
            try {
                ContextoSesion contexto = validadorJwt.validar(token);
                autenticar(peticion, contexto, token);
            } catch (JwtException e) {
                LOG.debug("Token rechazado: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        cadena.doFilter(peticion, respuesta);
    }

    private void autenticar(HttpServletRequest peticion, ContextoSesion contexto, String token) {
        List<SimpleGrantedAuthority> autoridades = contexto.roles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken autenticacion =
                new UsernamePasswordAuthenticationToken(contexto.sub(), null, autoridades);
        SecurityContextHolder.getContext().setAuthentication(autenticacion);

        peticion.setAttribute(ProveedorContexto.ATRIBUTO_CONTEXTO, contexto);
        peticion.setAttribute(ProveedorContexto.ATRIBUTO_TOKEN, token);
    }
}
