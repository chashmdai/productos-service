# productos-service (SMID 6.6) — Productos y Tareas

Microservicio de **planificación y entregables** de la plataforma SMID (Sistema de Misión y Datos)
de la **Defensoría de los Derechos de la Niñez** (Chile). Gestiona los **Productos** (informes,
gestiones, oficios, derivaciones, resoluciones) y las **Tareas** asociadas a un Caso, sobre la base
de los servicios ya vivos del ecosistema (Identidad, Catálogo, Personas, Casos).

Es el componente **6.6** del ecosistema. Consume eventos de Casos (6.4) y, opcionalmente, enriquece
desde Casos y Personas.

---

## 1. Stack

- **Java 21**, **Spring Boot 3.5.15**, **Maven** (sin wrapper).
- **MySQL 8** (InnoDB, `utf8mb4_0900_ai_ci`), **Flyway** (`ddl-auto=validate`).
- **JWT** con `jjwt 0.12.5` (HS256 con `kid`).
- **RabbitMQ** para eventos (transporte conmutable con publicación por log).
- **Testcontainers** para integración y concurrencia.

Coordenadas: `groupId` `cl.smid`, `artifactId` `productos-service`, paquete base
`cl.smid.productos`, versión `1.0.0`. Base de datos `db_productos`. Puerto **8092**.
Tras el `StripPrefix=1` del gateway, las rutas externas `/api/productos/**` llegan como
`/productos/**`.

---

## 2. Arquitectura (hexagonal estricta)

El dominio es **POJO puro** (sin dependencias de framework). Los adaptadores viven en la
infraestructura y la API; el cableado a Spring está aislado en `config/`.

```
cl.smid.productos
├── api                     Adaptador de entrada HTTP
│   ├── ControladorProductos, ControladorTareas
│   ├── dto                 Peticiones/respuestas (identificadores opacos)
│   ├── mapper              Dominio → respuesta
│   └── error               Sobre de error unificado (@RestControllerAdvice)
├── dominio                 Núcleo (sin framework)
│   ├── modelo              Agregados (Producto, Tarea), VOs, enums, vistas
│   ├── puerto.entrada      Casos de uso (GestionProductos, GestionTareas) + comandos
│   ├── puerto.salida       Contratos hacia el exterior (repos, eventos, clientes, reloj…)
│   ├── servicio            Máquinas de estado, generador de número, evaluador de alcance,
│   │                       y el orquestador ServicioProductos (implementa ambos casos de uso)
│   └── excepcion           Errores de dominio + catálogo de códigos
├── infraestructura
│   ├── persistencia        JPA (entidades, repos, mapeo, specs) + correlativo JDBC
│   ├── eventos             Publicador (log/RabbitMQ), listener opcional de caso.abierto
│   ├── seguridad           Validación JWT, filtro, contexto, manejadores 401/403
│   ├── cliente             Clientes REST de Casos/Personas (conmutables, con variante nula)
│   ├── soporte             Reloj, generador de UUID, directorio de sedes
│   └── web                 Sobre de error neutral + escritor JSON
└── config                  Propiedades tipadas y cableado del dominio
```

**Un único bean** (`ServicioProductos`) implementa los dos puertos de entrada; los controladores
dependen de las interfaces. La **frontera transaccional** es el controlador (escrituras) y el
listener; el dominio no abre transacciones.

---

## 3. Modelo de negocio

### Producto (agregado)

Campos públicos por `alt_key` (UUID). Hereda `idSede`/`idUnidad` al crearse.

Máquina de estados:

```
BORRADOR ──ENVIAR_REVISION──▶ EN_REVISION ──EMITIR──▶ EMITIDO (terminal)
BORRADOR / EN_REVISION ──ANULAR──▶ ANULADO (terminal)
```

- `EMITIR` y `ANULAR` **exigen rol de Coordinación** (de lo contrario, `AUTZ-004` / 403).
- `ENVIAR_REVISION` es operativa (solo alcance territorial).
- La edición solo aplica en `BORRADOR` y `EN_REVISION` (si no, `PRD-409`).

**Numeración oficial** (solo al `EMITIR`): `PRD-{CODIGO_SEDE}-{N}/{AÑO}`. El correlativo `N` es
**atómico por (sede, año)** y arranca en 1; **no hay serie beta** para Productos. El código de sede
proviene del directorio configurable (`smid.sedes.codigos`), con valor por defecto `STG`.

### Tarea (hija de un Producto o suelta ligada a un Caso)

Máquina de estados:

```
PENDIENTE ──TOMAR──▶ EN_CURSO ──COMPLETAR──▶ COMPLETADA (terminal)
PENDIENTE / EN_CURSO ──CANCELAR──▶ CANCELADA (terminal)
REASIGNAR cambia el responsable sin alterar el estado.
```

- **Permiso unificado**: puede operar la tarea **Coordinación o el responsable actual** (si no,
  `AUTZ-004` / 403). Una tarea no asignada solo la opera Coordinación; el flujo típico es
  *Coordinación REASIGNA → el responsable TOMA → COMPLETA*.
- `REASIGNAR` requiere `responsableAlt` (si falta, `PRD-422`).

### Historial

Cada agregado mantiene un historial **append-only** (asiento de creación incluido). El actor es el
`alt_key` del usuario para la API o el actor de **sistema**
(`00000000-0000-0000-0000-000000000000`) para el listener.

### Territorio (alcance)

El alcance filtra **registro a registro** por la sede/unidad heredadas. `NACIONAL` ve todo; `SEDE`
filtra por sede; `UNIDAD` por unidad. **Un registro fuera de alcance se trata como inexistente
(404, no 403).**

### Integración con Casos al crear

- Con enriquecimiento **desactivado** (por defecto): el Producto / la Tarea suelta heredan
  sede/unidad del **contexto del usuario** (token).
- Con enriquecimiento **activo**: se consulta el Caso (si es inaccesible/inexistente, `PRD-422`).
- Una Tarea bajo un Producto hereda siempre del **Producto**.

---

## 4. Eventos (solo metadatos no sensibles)

Transporte conmutable: `log` (por defecto) o `rabbitmq`. Exchange de dominio `smid.eventos` (topic);
la *routing key* es el tipo del evento. La publicación es **tolerante a fallos**.

Eventos emitidos: `producto.creado` `{idCasoAlt, tipo}`, `producto.emitido` `{numeroProducto}`,
`producto.anulado` `{}`, `tarea.creada` `{idCasoAlt, idProductoAlt?, prioridad}`,
`tarea.asignada` `{responsableAlt}`, `tarea.completada` `{}`. El `altKey` viaja en el sobre, no en
los metadatos.

**Consumo opcional** (`smid.eventos.consumo=rabbitmq`): consume `caso.abierto` (emitido por Casos)
y siembra una Tarea inicial "Plan de trabajo" (`tipoSemilla=PLAN_TRABAJO`, prioridad `MEDIA`, actor
de sistema). Es **idempotente** por `(id_caso_alt, tipo_semilla)`. Los mensajes malformados se
rechazan sin reencolar y terminan en la DLQ (`productos.caso-abierto.dlq`). El listener **nunca**
usa los clientes de enriquecimiento: hereda sede/unidad de los metadatos del propio evento.

---

## 5. API REST

Todas las rutas exigen `Authorization: Bearer <jwt>` (salvo `/actuator/health`). Rutas relativas al
servicio (tras el `StripPrefix`):

| Método      | Ruta                                          | Descripción                              |
|-------------|-----------------------------------------------|------------------------------------------|
| POST        | `/productos/productos`                         | Crear producto (201)                     |
| GET         | `/productos/productos/{altKey}`               | Detalle (con tareas e historial)         |
| GET         | `/productos/productos`                         | Listado paginado                         |
| PUT / PATCH | `/productos/productos/{altKey}`               | Editar (solo BORRADOR/EN_REVISION)       |
| POST        | `/productos/productos/{altKey}/transiciones`  | ENVIAR_REVISION \| EMITIR \| ANULAR      |
| POST        | `/productos/productos/{altKey}/tareas`        | Crear tarea bajo el producto (201)       |
| POST        | `/productos/tareas`                            | Crear tarea suelta (201)                 |
| GET         | `/productos/tareas/{altKey}`                  | Detalle de tarea (con historial)         |
| GET         | `/productos/tareas`                            | Listado paginado                         |
| POST        | `/productos/tareas/{altKey}/transiciones`     | TOMAR \| COMPLETAR \| CANCELAR \| REASIGNAR |

Parámetros de listado de productos: `idCaso`, `estado`, `tipo`, `pagina` (base 0), `tamano`
(1..200, por defecto 20). De tareas: `idCaso`, `idProducto`, `responsable`, `estado`, `pagina`,
`tamano`.

### Ejemplos

Crear producto:

```json
POST /productos/productos
{
  "idCaso": "8f3a1c2e-...",
  "tipo": "INFORME",
  "titulo": "Informe de derivación",
  "descripcion": "Antecedentes recabados en la primera gestión"
}
```

Respuesta (201):

```json
{
  "altKey": "b2d9...",
  "idCaso": "8f3a1c2e-...",
  "tipo": "INFORME",
  "titulo": "Informe de derivación",
  "descripcion": "Antecedentes recabados en la primera gestión",
  "estado": "BORRADOR",
  "idSede": "rm-01-...",
  "idUnidad": "u-07-...",
  "autor": "user-...",
  "vigente": true,
  "creadoEn": "2027-03-15T12:00:00Z",
  "actualizadoEn": "2027-03-15T12:00:00Z"
}
```

Emitir (requiere Coordinación):

```json
POST /productos/productos/{altKey}/transiciones
{ "accion": "EMITIR", "observacion": "Visado por la jefatura" }
```

Respuesta (200): el producto pasa a `EMITIDO` con `numeroProducto` `PRD-RM-1/2027`.

Crear tarea suelta:

```json
POST /productos/tareas
{
  "idCaso": "8f3a1c2e-...",
  "titulo": "Solicitar informe a la escuela",
  "prioridad": "ALTA",
  "responsableAlt": "user-...",
  "fechaVencimiento": "2027-03-30"
}
```

Reasignar tarea:

```json
POST /productos/tareas/{altKey}/transiciones
{ "accion": "REASIGNAR", "responsableAlt": "user-...", "observacion": "Carga de trabajo" }
```

### Sobre de error unificado

El campo de ruta se llama **`ruta`** (no `path`). `detalles` solo aparece en errores de validación.

```json
{
  "status": 409,
  "error": "Conflicto de estado",
  "codigo": "PRD-409",
  "mensaje": "No se puede editar un producto en estado EMITIDO",
  "ruta": "/productos/productos/b2d9...",
  "timestamp": "2027-03-15T12:00:00Z"
}
```

| Código    | HTTP | Significado                                            |
|-----------|------|--------------------------------------------------------|
| PRD-001   | 400  | Validación de entrada                                  |
| PRD-404   | 404  | Recurso no encontrado o fuera de alcance territorial   |
| PRD-409   | 409  | Conflicto con la máquina de estados                    |
| PRD-422   | 422  | Regla de negocio incumplida                            |
| PRD-500   | 500  | Error interno                                          |
| AUTZ-003  | 401  | No autenticado                                         |
| AUTZ-004  | 403  | No autorizado (falta rol o permiso)                    |

---

## 6. Seguridad (JWT)

JWT HS256 con `kid` compartido por el ecosistema. La cabecera porta `kid`; el *payload* incluye
`sub` (alt_key del usuario), `iss=smid-auth`, `aud` (debe contener `smid-servicios`), `exp`,
`roles[]`, `idSede`, `idUnidad`, `alcance` (`UNIDAD|SEDE|NACIONAL`) y `nombre`.

Se admite **rotación** de clave: par activo (`kidActivo`/`secretoActivo`) y par previo opcional
(`kidPrevio`/`secretoPrevio`). El secreto debe tener al menos **32 bytes** (UTF-8).

---

## 7. Configuración (variables de entorno)

| Variable                  | Por defecto       | Descripción                                       |
|---------------------------|-------------------|---------------------------------------------------|
| `SERVER_PORT`             | `8092`            | Puerto HTTP                                       |
| `DB_URL`                  | (local MySQL)     | URL JDBC (incluye `tinyInt1isBit=true`)           |
| `DB_USERNAME`             | `smid`            | Usuario de BD                                     |
| `DB_PASSWORD`             | `smid`            | Contraseña de BD                                  |
| `JWT_KID_ACTIVO`          | `smid-2026-06`    | `kid` de la clave activa                          |
| `JWT_SECRET_ACTIVO`       | (requerido)       | Secreto HMAC activo (≥ 32 bytes)                  |
| `JWT_KID_PREVIO`          | (vacío)           | `kid` de la clave previa (rotación)               |
| `JWT_SECRET_PREVIO`       | (vacío)           | Secreto previo (rotación)                         |
| `JWT_ISSUER`              | `smid-auth`       | Emisor esperado                                   |
| `JWT_AUDIENCIA`           | `smid-servicios`  | Audiencia requerida                               |
| `ROLES_COORDINACION`      | `COORDINACION,COORDINADOR` | Roles con capacidad de Coordinación      |
| `EVENTOS_TRANSPORTE`      | `log`             | `log` \| `rabbitmq`                               |
| `EVENTOS_CONSUMO`         | `none`            | `none` \| `rabbitmq`                              |
| `ENRIQUECIMIENTO_CASOS`   | `false`           | Activa el cliente REST de Casos                   |
| `ENRIQUECIMIENTO_PERSONAS`| `false`           | Activa el cliente REST de Personas                |
| `CASOS_URL_BASE`          | (vacío)           | URL base del recurso de Casos                     |
| `PERSONAS_URL_BASE`       | (vacío)           | URL base del recurso de Personas                  |
| `SEDE_CODIGO_DEFECTO`     | `STG`             | Código de sede por defecto                        |
| `RABBITMQ_HOST/PORT/...`  | `localhost/5672`  | Conexión a RabbitMQ (si se usa)                   |

El mapa de códigos de sede se define como `smid.sedes.codigos[<alt_key>]: <código>` (notación de
corchetes en YAML para las claves UUID).

---

## 8. Compilar, probar y ejecutar

> **Notas de honestidad técnica:**
> - Las **pruebas unitarias** de dominio corren **sin Docker**.
> - Las pruebas de **integración** y de **concurrencia** usan **Testcontainers** y **requieren
>   Docker** (se omiten automáticamente si no está disponible).
> - La **resolución de dependencias Maven requiere acceso a la red** (Maven Central).

```bash
# Compilar y empaquetar (omitiendo pruebas)
mvn -DskipTests package

# Solo pruebas unitarias de dominio (sin Docker)
mvn -Dtest='*Test' -DfailIfNoTests=false test

# Suite completa (requiere Docker para integración y concurrencia)
mvn verify

# Ejecutar en local (perfil 'local': publicación por log, sin RabbitMQ ni enriquecimiento)
# Requiere MySQL con la base db_productos creada.
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

El perfil `local` (`application-local.yml`) trae un secreto JWT de **demostración** que debe
reemplazarse en cualquier entorno real.

---

## 9. Decisiones de arquitectura (overrides)

1. **Arranque limpio**: sin migración ni ETL del sistema legado; la costura SIGER es futura.
2. **Identificadores públicos**: solo `alt_key` (UUID `VARCHAR(36)`). La PK `BIGINT` interna nunca
   cruza la frontera ni se expone.
3. **Enumeraciones**: `VARCHAR(N) + CHECK` (nunca `ENUM` nativo).
4. **Marcas de tiempo**: `DATETIME(6)` en UTC (`hibernate.jdbc.time_zone=UTC`); fechas puras `DATE`.
5. **Booleanos**: `TINYINT(1)` (`tinyInt1isBit=true`).
6. **Denegación territorial = 404** (no 403).
7. **Transaccionalidad** solo en la frontera (controlador/listener).
8. **Eventos** solo con metadatos no sensibles; transporte conmutable; publicación tolerante a
   fallos.
9. **Sobre de error** con campo `ruta`.
10. **JWT** con par de claves fijo (`kidActivo`/`secretoActivo` + `kidPrevio`/`secretoPrevio`
    opcional), evitando mapas YAML con *placeholders* de entorno que no resuelven.
11. **Hexagonal estricto**: dominio POJO; cableado en `config/CableadoDominio`; un solo bean
    implementa todos los puertos de entrada.

### Bloque OVERRIDE de diseño 6.6

La documentación del Núcleo Fundacional no incluye una sección funcional para 6.6. Este servicio
adopta el diseño descrito en este README. **Si una versión posterior de la documentación
introdujera definiciones que contradigan lo aquí especificado** (estados, numeración, permisos o
contratos de eventos), prevalece este documento hasta que se acuerde y publique una corrección
formal; en tal caso se emitirá el parche correspondiente con objetivos explícitos de
búsqueda-y-reemplazo.
