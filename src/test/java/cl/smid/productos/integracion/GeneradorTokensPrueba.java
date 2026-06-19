package cl.smid.productos.integracion;

import io.jsonwebtoken.Jwts;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Generador de JWT HS256 para las pruebas de integracion. Emite tokens equivalentes a los del
 * servicio de autenticacion del ecosistema (mismo esquema de cabecera con {@code kid} y mismos
 * claims), firmados con el secreto de prueba.
 */
final class GeneradorTokensPrueba {

    private GeneradorTokensPrueba() {
    }

    static String token(String secreto, String kid, String sub, List<String> roles,
                        String idSede, String idUnidad, String alcance, String nombre) {
        SecretKeySpec clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return Jwts.builder()
                .header().keyId(kid).and()
                .subject(sub)
                .issuer("smid-auth")
                .audience().add("smid-servicios").and()
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .claim("roles", roles)
                .claim("idSede", idSede)
                .claim("idUnidad", idUnidad)
                .claim("alcance", alcance)
                .claim("nombre", nombre)
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }
}
