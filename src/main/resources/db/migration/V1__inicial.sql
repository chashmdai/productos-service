-- =====================================================================================
-- productos-service (SMID 6.6) - Esquema inicial
-- Plataforma SMID - Defensoria de los Derechos de la Ninez (Chile)
--
-- DECISIONES ARQUITECTONICAS APLICADAS (OVERRIDES):
--  1. Arranque limpio: sin migracion ni ETL del sistema legado; la costura SIGER es futura.
--  2. Identificadores publicos: solo alt_key (UUID, VARCHAR(36)). La PK BIGINT interna nunca
--     cruza la frontera del servicio ni se expone en la API.
--  3. Enumeraciones: VARCHAR(N) + CHECK (nunca ENUM nativo de MySQL), para evolucion controlada.
--  4. Marcas de tiempo: DATETIME(6) en UTC (hibernate.jdbc.time_zone=UTC); fechas puras DATE.
--  5. Booleanos: TINYINT(1) (JDBC con tinyInt1isBit=true).
--  6. Denegacion territorial: se modela como 404 en la API (no 403); a nivel de datos, el alcance
--     filtra por id_sede_alt / id_unidad_alt heredados.
--  7. Transaccionalidad: la frontera es el controlador/listener (no hay logica transaccional aqui).
--  8. Eventos: solo metadatos no sensibles; transporte conmutable (log/RabbitMQ).
--  9. Sobre de error unificado con campo "ruta".
-- 10. JWT con par de claves fijo (kidActivo/secretoActivo + kidPrevio/secretoPrevio opcional).
-- 11. Hexagonal estricto: dominio POJO; este esquema es el detalle de persistencia.
--
-- NUMERACION OFICIAL DEL PRODUCTO (al EMITIR): PRD-{CODIGO_SEDE}-{N}/{ANIO}
--   Correlativo atomico por (sede, anio) en 'correlativo_producto' (sin entidad JPA; JDBC puro).
--   Sin serie beta para Productos.
-- =====================================================================================

-- -------------------------------------------------------------------------------------
-- Tabla: producto (agregado principal)
-- -------------------------------------------------------------------------------------
CREATE TABLE producto (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    alt_key           VARCHAR(36)   NOT NULL,
    id_caso_alt       VARCHAR(36)   NOT NULL,
    tipo              VARCHAR(20)   NOT NULL,
    titulo            VARCHAR(200)  NOT NULL,
    descripcion       VARCHAR(2000) NULL,
    estado            VARCHAR(20)   NOT NULL,
    numero_producto   VARCHAR(40)   NULL,
    id_sede_alt       VARCHAR(36)   NULL,
    id_unidad_alt     VARCHAR(36)   NULL,
    autor_alt         VARCHAR(36)   NOT NULL,
    vigente           TINYINT(1)    NOT NULL,
    creado_en         DATETIME(6)   NOT NULL,
    actualizado_en    DATETIME(6)   NOT NULL,
    emitido_en        DATETIME(6)   NULL,
    anulado_en        DATETIME(6)   NULL,
    CONSTRAINT pk_producto PRIMARY KEY (id),
    CONSTRAINT uk_producto_alt_key UNIQUE (alt_key),
    CONSTRAINT uk_producto_numero UNIQUE (numero_producto),
    CONSTRAINT ck_producto_tipo CHECK (tipo IN
        ('INFORME','GESTION','OFICIO','DERIVACION','RESOLUCION','OTRO')),
    CONSTRAINT ck_producto_estado CHECK (estado IN
        ('BORRADOR','EN_REVISION','EMITIDO','ANULADO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX ix_producto_caso   ON producto (id_caso_alt);
CREATE INDEX ix_producto_sede   ON producto (id_sede_alt);
CREATE INDEX ix_producto_unidad ON producto (id_unidad_alt);
CREATE INDEX ix_producto_estado ON producto (estado);

-- -------------------------------------------------------------------------------------
-- Tabla: tarea (hija de un Producto o suelta ligada a un Caso)
-- id_producto_alt es NULL para tareas sueltas; id_caso_alt siempre presente.
-- uk_tarea_semilla garantiza idempotencia de las tareas sembradas por evento
-- (una por (caso, tipo_semilla); las tareas no sembradas tienen tipo_semilla NULL).
-- -------------------------------------------------------------------------------------
CREATE TABLE tarea (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    alt_key             VARCHAR(36)   NOT NULL,
    id_caso_alt         VARCHAR(36)   NOT NULL,
    id_producto_alt     VARCHAR(36)   NULL,
    titulo              VARCHAR(200)  NOT NULL,
    descripcion         VARCHAR(2000) NULL,
    responsable_alt     VARCHAR(36)   NULL,
    responsable_nombre  VARCHAR(200)  NULL,
    prioridad           VARCHAR(10)   NOT NULL,
    estado              VARCHAR(12)   NOT NULL,
    fecha_vencimiento   DATE          NULL,
    id_sede_alt         VARCHAR(36)   NULL,
    id_unidad_alt       VARCHAR(36)   NULL,
    tipo_semilla        VARCHAR(40)   NULL,
    creado_en           DATETIME(6)   NOT NULL,
    actualizado_en      DATETIME(6)   NOT NULL,
    completado_en       DATETIME(6)   NULL,
    CONSTRAINT pk_tarea PRIMARY KEY (id),
    CONSTRAINT uk_tarea_alt_key UNIQUE (alt_key),
    CONSTRAINT uk_tarea_semilla UNIQUE (id_caso_alt, tipo_semilla),
    CONSTRAINT ck_tarea_prioridad CHECK (prioridad IN ('BAJA','MEDIA','ALTA')),
    CONSTRAINT ck_tarea_estado CHECK (estado IN
        ('PENDIENTE','EN_CURSO','COMPLETADA','CANCELADA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX ix_tarea_caso        ON tarea (id_caso_alt);
CREATE INDEX ix_tarea_producto    ON tarea (id_producto_alt);
CREATE INDEX ix_tarea_sede        ON tarea (id_sede_alt);
CREATE INDEX ix_tarea_unidad      ON tarea (id_unidad_alt);
CREATE INDEX ix_tarea_responsable ON tarea (responsable_alt);
CREATE INDEX ix_tarea_estado      ON tarea (estado);

-- -------------------------------------------------------------------------------------
-- Tabla: producto_transicion (historial append-only del Producto)
-- Referencia al padre por su PK interna escalar (producto_id), sin relacion JPA.
-- estado_origen es NULL en el asiento de creacion (accion = 'CREACION').
-- -------------------------------------------------------------------------------------
CREATE TABLE producto_transicion (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    producto_id     BIGINT        NOT NULL,
    alt_key         VARCHAR(36)   NOT NULL,
    estado_origen   VARCHAR(20)   NULL,
    estado_destino  VARCHAR(20)   NOT NULL,
    accion          VARCHAR(20)   NOT NULL,
    observacion     VARCHAR(1000) NULL,
    actor           VARCHAR(36)   NOT NULL,
    ocurrido_en     DATETIME(6)   NOT NULL,
    CONSTRAINT pk_producto_transicion PRIMARY KEY (id),
    CONSTRAINT uk_producto_transicion_alt_key UNIQUE (alt_key),
    CONSTRAINT fk_producto_transicion_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id),
    CONSTRAINT ck_producto_transicion_accion CHECK (accion IN
        ('CREACION','ENVIAR_REVISION','EMITIR','ANULAR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX ix_producto_transicion_producto ON producto_transicion (producto_id);

-- -------------------------------------------------------------------------------------
-- Tabla: tarea_transicion (historial append-only de la Tarea)
-- -------------------------------------------------------------------------------------
CREATE TABLE tarea_transicion (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    tarea_id        BIGINT        NOT NULL,
    alt_key         VARCHAR(36)   NOT NULL,
    estado_origen   VARCHAR(12)   NULL,
    estado_destino  VARCHAR(12)   NOT NULL,
    accion          VARCHAR(12)   NOT NULL,
    observacion     VARCHAR(1000) NULL,
    actor           VARCHAR(36)   NOT NULL,
    ocurrido_en     DATETIME(6)   NOT NULL,
    CONSTRAINT pk_tarea_transicion PRIMARY KEY (id),
    CONSTRAINT uk_tarea_transicion_alt_key UNIQUE (alt_key),
    CONSTRAINT fk_tarea_transicion_tarea FOREIGN KEY (tarea_id)
        REFERENCES tarea (id),
    CONSTRAINT ck_tarea_transicion_accion CHECK (accion IN
        ('CREACION','TOMAR','COMPLETAR','CANCELAR','REASIGNAR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX ix_tarea_transicion_tarea ON tarea_transicion (tarea_id);

-- -------------------------------------------------------------------------------------
-- Tabla: correlativo_producto (serie del numero oficial por sede y anio)
-- PK compuesta (id_sede_alt, anio) que aisla cada serie. Operada por JDBC puro mediante
-- UPSERT atomico (INSERT ... ON DUPLICATE KEY UPDATE ultimo = LAST_INSERT_ID(ultimo + 1)).
-- Sin entidad JPA.
-- -------------------------------------------------------------------------------------
CREATE TABLE correlativo_producto (
    id_sede_alt   VARCHAR(36) NOT NULL,
    anio          INT         NOT NULL,
    ultimo        BIGINT      NOT NULL,
    CONSTRAINT pk_correlativo_producto PRIMARY KEY (id_sede_alt, anio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
