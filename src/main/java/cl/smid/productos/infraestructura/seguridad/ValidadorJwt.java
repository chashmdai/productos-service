package cl.smid.productos.infraestructura.seguridad;

import cl.smid.productos.config.PropiedadesSeguridad;
import cl.smid.productos.dominio.modelo.Alcance;
import cl.smid.productos.dominio.modelo.ContextoSesion;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import io.jsonwebtoken.ProtectedHeader;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.Set;

/**
 * Valida y traduce el JWT corporativo a {@link ContextoSesion}. Comparte el esquema HS256 con kid
 * del ecosistema SMID.
 *
 * <p>El {@link Locator} inspecciona el {@code kid} del encabezado y devuelve la clave activa o la
 * previa (rotacion). {@code parseSignedClaims} valida la firma y la expiracion; el emisor y la
 * audiencia se comprueban manualmente (la audiencia debe contener el valor esperado).</p>
 */
@Component
public class ValidadorJwt {

    private final SecretKey claveActiva;
    private final String kidActivo;
    private final SecretKey clavePrevia;   // puede ser null si no hay rotacion configurada
    private final String kidPrevio;        // puede ser null
    private final String issuerEsperado;
    private final String audienciaRequerida;
    private final JwtParser parser;

    public ValidadorJwt(PropiedadesSeguridad propiedades) {
        this.kidActivo = propiedades.kidActivo();
        this.claveActiva = aClave(propiedades.secretoActivo());
        this.kidPrevio = propiedades.kidPrevio();
        this.clavePrevia = (propiedades.secretoPrevio() == null || propiedades.secretoPrevio().isBlank())
                ? null
                : aClave(propiedades.secretoPrevio());
        this.issuerEsperado = propiedades.issuer();
        this.audienciaRequerida = propiedades.audiencia();
        this.parser = Jwts.parser().keyLocator(localizadorClaves()).build();
    }

    /**
     * Valida el token y construye el contexto de sesion.
     *
     * @throws JwtException si la firma, la estructura, la expiracion, el emisor o la audiencia no
     *                      son validos (el filtro lo traduce a 401).
     */
    public ContextoSesion validar(String token) {
        Jws<Claims> jws = parser.parseSignedClaims(token);
        Claims claims = jws.getPayload();

        if (!issuerEsperado.equals(claims.getIssuer())) {
            throw new JwtException("Emisor del token no valido");
        }
        Set<String> audiencia = claims.getAudience();
        if (audiencia == null || !audiencia.contains(audienciaRequerida)) {
            throw new JwtException("Audiencia del token no valida");
        }

        String sub = claims.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new JwtException("El token no contiene 'sub'");
        }

        List<String> roles = extraerRoles(claims);
        String idSede = claims.get("idSede", String.class);
        String idUnidad = claims.get("idUnidad", String.class);
        Alcance alcance = aAlcance(claims.get("alcance", String.class));
        String nombre = claims.get("nombre", String.class);

        return new ContextoSesion(sub, roles, idSede, idUnidad, alcance, nombre);
    }

    private Locator<Key> localizadorClaves() {
        return new Locator<>() {
            @Override
            public Key locate(io.jsonwebtoken.Header header) {
                if (header instanceof ProtectedHeader protegido) {
                    String kid = protegido.getKeyId();
                    if (kid != null) {
                        if (kid.equals(kidActivo)) {
                            return claveActiva;
                        }
                        if (clavePrevia != null && kid.equals(kidPrevio)) {
                            return clavePrevia;
                        }
                    }
                }
                throw new JwtException("Identificador de clave (kid) desconocido");
            }
        };
    }

    private List<String> extraerRoles(Claims claims) {
        Object valor = claims.get("roles");
        if (valor instanceof List<?> lista) {
            return lista.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private Alcance aAlcance(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new JwtException("El token no contiene 'alcance'");
        }
        try {
            return Alcance.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Alcance del token no reconocido: " + valor);
        }
    }

    private SecretKey aClave(String secreto) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "El secreto de firma debe tener al menos 32 bytes (256 bits) para HS256");
        }
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
