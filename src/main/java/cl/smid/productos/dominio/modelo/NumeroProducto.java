package cl.smid.productos.dominio.modelo;

import java.util.Objects;

/**
 * Numero oficial de un Producto, asignado <strong>solo al EMITIR</strong>.
 *
 * <p>Formato: {@code PRD-{CODIGO_SEDE}-{N}/{ANIO}} — p. ej. {@code PRD-RM-1/2027}.
 * El correlativo {@code N} es atomico y unico por {@code (sede, anio)} (sin serie beta,
 * salvo que la documentacion lo exija). Es un objeto de valor inmutable.</p>
 */
public record NumeroProducto(String valor) {

    public NumeroProducto {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El numero de producto no puede ser vacio");
        }
        valor = valor.trim();
    }

    /**
     * Construye el numero a partir de sus componentes.
     *
     * @param codigoSede  codigo corto de la sede (p. ej. {@code RM}).
     * @param correlativo correlativo atomico (>= 1).
     * @param anio        anio de emision.
     */
    public static NumeroProducto de(String codigoSede, long correlativo, int anio) {
        Objects.requireNonNull(codigoSede, "codigoSede es obligatorio");
        return new NumeroProducto("PRD-" + codigoSede + "-" + correlativo + "/" + anio);
    }

    @Override
    public String toString() {
        return valor;
    }
}
