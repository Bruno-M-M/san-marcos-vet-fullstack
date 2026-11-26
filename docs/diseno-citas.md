# Diseño: reserva de citas

Estado: **propuesta para aprobar**. Alcance: reservar, cancelar y atender citas, sobre el catálogo de servicios que ya está en `citas-service`.

## Decisiones ya tomadas

| Tema | Decisión |
|---|---|
| Profesionales | Son los usuarios con rol `VENDEDOR` de `auth-service` (no se crea un rol nuevo). |
| Cancelación | El cliente puede cancelar hasta **24 horas antes**. El margen es configurable (`CITAS_MARGEN_CANCELACION_HORAS`, por defecto 24). |
| Agenda | Bloques de **30 minutos**, de **08:30 a 18:30**. |
| Servicios | Un servicio ocupa `ceil(duracionMin / 30)` bloques seguidos. Ejemplos: 5 a 30 min = 1 bloque, 40 a 60 min = 2, 90 min = 3, 120 min = 4. |
| No agendable | Los servicios con `agendable = false` (hospitalización) no se pueden reservar. |

## Supuestos que se pueden cambiar (valores por defecto)

- Días de atención: **lunes a viernes** (`CITAS_DIAS_ATENCION`).
- Anticipación mínima para reservar: **60 minutos** (`CITAS_ANTICIPACION_MIN`).
- Reserva hasta **30 días** hacia adelante (`CITAS_DIAS_MAXIMOS`).
- Máximo **3 citas futuras activas** por cliente (`CITAS_MAX_ACTIVAS_CLIENTE`).
- Zona horaria del negocio: **America/Santiago** (`CITAS_ZONA`).
- Una cita se reserva con **un profesional elegido** por el cliente (sin asignación automática en esta versión).
- Datos de la mascota: solo un campo de texto opcional (`mascota`) y otro para notas. No hay ficha de mascotas todavía.

## Datos (tablas nuevas en `citas-service`)

**`cita`**: `id`, `cliente_id`, `profesional_id`, `profesional_nombre` (copia al reservar), `servicio_id` (FK a `servicio`), `inicio`, `fin`, `bloques`, `estado`, `mascota`, `notas`, `precio` (copia al reservar), `creada_en`.

Estados y transiciones: una cita nace `RESERVADA` y solo puede pasar a `CANCELADA`, `ATENDIDA` o `NO_ASISTIO`. Esos tres son finales: no se reabre ni se cambia una cita cerrada.

**`cita_bloque`**: `id`, `cita_id` (FK), `profesional_id`, `inicio_bloque`. Con **índice único (`profesional_id`, `inicio_bloque`)**.

Ese índice es lo que evita el doble agendamiento: si dos clientes reservan el mismo horario a la vez, la base rechaza al segundo y la API responde **409** ("ese horario ya fue tomado"). La cita y sus bloques se guardan en **una sola transacción**, así que nunca queda una cita a medias. Al cancelar una cita se borran sus bloques y el horario queda libre.

`cliente_id` y `profesional_id` son los ids de `auth-service` (sin clave foránea, porque es otra base lógica). El nombre del profesional se copia en la cita para no consultar `auth-service` cada vez que se listan citas.

## Reglas

- **Reservar** (cliente con sesión):
  - El servicio existe, está activo y es agendable. El profesional existe y está activo.
  - La hora de inicio cae **exactamente en un bloque** (08:30, 09:00, 09:30…) y todos los bloques terminan a más tardar a las 18:30.
  - El día es de atención, el inicio respeta la anticipación mínima y no pasa del máximo de días.
  - Los bloques del profesional están libres.
  - El cliente **no tiene otra cita que se cruce** a esa hora y no supera el máximo de citas activas.
- **Cancelar**: el cliente solo cancela sus citas, y solo si faltan al menos 24 horas. Si no, responde 409 con el motivo. El administrador puede cancelar cualquiera sin margen.
- **Asistencia**: el profesional marca `ATENDIDA` o `NO_ASISTIO` en sus propias citas, solo después de la hora de inicio. El administrador puede hacerlo en cualquiera.
- **Aviso al reservar con menos de 24 horas**: esa cita ya no se puede cancelar desde la web. La pantalla de confirmación lo dice antes de reservar.

## Hora y zona horaria

Los contenedores usan UTC, y hoy los comentarios guardan la hora en UTC (se ven 3 horas adelantados). Para la agenda eso no sirve.

- Las horas de las citas se guardan como **hora local de Chile** (`LocalDateTime`), que es lo que ven el cliente y el profesional. Chile cambia de horario en el año, y con hora local la agenda no se corre.
- `citas-service` tiene un **`Clock` configurado con `CITAS_ZONA`**, y todo "ahora" sale de ahí. No se cambia la zona del contenedor, así que los otros servicios no se ven afectados.
- Con ese `Clock`, las reglas de tiempo (24 horas, anticipación, "solo después del inicio") se prueban fijando la hora en las pruebas.
- Se corrige también la hora de los comentarios con el mismo `Clock`.

## Cómo se obtienen los profesionales

`GET /api/v1/usuarios` es solo para administradores, así que no sirve. Se agrega en `auth-service`:

- `GET /api/v1/profesionales`: **público**, como el staff de cualquier clínica. Devuelve `id`, `nombres` y `apellidos` de los `VENDEDOR` activos. No devuelve correo, RUN ni teléfono.
- `citas-service` lo consulta con un cliente HTTP, igual que `ventas-service` consulta a `productos-service` (`AUTH_URL` en el compose), solo al reservar: valida que el profesional exista y copia su nombre en la cita. Como el endpoint es público, no hace falta reenviar el token entre servicios.
- Si `auth-service` no responde, la reserva falla con **503** y un mensaje claro, en vez de un error genérico.
- Nginx suma la ruta `/api/v1/profesionales` hacia `auth-service`.

## Endpoints de `citas-service`

Todo cuelga de `/api/v1/citas`, así que basta una ruta nueva en nginx.

| Método | Ruta | Quién | Qué hace |
|---|---|---|---|
| `GET` | `/api/v1/citas/disponibilidad?servicioId=&profesionalId=&fecha=` | **Público** | Horas de inicio libres ese día |
| `POST` | `/api/v1/citas` | Cliente | Reserva (`servicioId`, `profesionalId`, `inicio`, `mascota?`, `notas?`) |
| `GET` | `/api/v1/citas/mias` | Cliente | Sus citas |
| `PATCH` | `/api/v1/citas/{id}/cancelar` | Cliente o administrador | Cancela (con la regla de 24 horas para el cliente) |
| `GET` | `/api/v1/citas/agenda?fecha=` | Vendedor (profesional) | Su agenda del día |
| `PATCH` | `/api/v1/citas/{id}/asistencia` | Profesional o administrador | Marca `ATENDIDA` o `NO_ASISTIO` |
| `GET` | `/api/v1/citas?desde=&hasta=&profesionalId=&estado=` | Administrador | Citas de un rango de fechas (rango obligatorio, máximo 31 días) |

La disponibilidad es pública para que el visitante vea horarios **antes** de iniciar sesión. Solo se pide sesión al confirmar. La respuesta solo trae horas libres: no revela quién reservó qué.

Errores con el mismo formato del resto: `fecha`, `estado`, `error`, `mensaje`, `ruta`, `errores`.

## Frontend

- **Servicios** (`/servicios`, pública): catálogo agrupado por categoría y botón "Agendar". Solo los agendables muestran ese botón.
- **Reserva**: servicio, profesional, fecha, hora libre, confirmar. El visitante elige todo sin sesión. Al confirmar, si no ha iniciado sesión pasa por el login y vuelve con su elección guardada.
- **Mis citas**: lista con botón de cancelar (deshabilitado con el motivo si faltan menos de 24 horas).
- **Agenda del profesional** (panel del vendedor): citas del día y botones de asistencia.

## Entrega por etapas (un PR cada una)

1. Frontend: página de **Servicios** con el catálogo que ya existe (hoy `/servicios` es una página pendiente).
2. `auth-service`: endpoint de profesionales, pruebas y ruta en nginx.
3. `citas-service`: tablas, `Clock` y zona horaria (con la corrección de comentarios), cliente de profesionales y disponibilidad.
4. `citas-service`: reservar, cancelar y mis citas.
5. `citas-service`: agenda del profesional, asistencia y listado de administrador.
6. Frontend: flujo de reserva y Mis citas.
7. Frontend: agenda del profesional.
8. Postman, prueba de humo, evidencia y README.

Cada etapa se despliega y se prueba en el servidor antes de pasar a la siguiente.

## Fuera de alcance (limitaciones conocidas)

- **Todos los `VENDEDOR` aparecen como profesionales**, porque se reutiliza el rol. Si algún vendedor solo atiende la tienda, igual se puede reservar con él. Se resuelve más adelante con una marca en el usuario o un rol propio.
- **Sin pausas ni ausencias**: no hay colación, vacaciones ni bloqueos manuales de horario. Cada profesional atiende todo el horario, todos los días de atención.
- **Sin feriados**: un feriado en día hábil aparece disponible.
- **Sin avisos por correo** de reserva ni de cancelación.
- **Profesional desactivado con citas futuras**: las citas siguen ahí. El administrador debe cancelarlas a mano.

## Riesgos

- **Memoria del servidor**: quedan unos 300 MB libres. Si los servicios empiezan a reiniciarse, hay que pasar a una `t3.medium`.
- **Usuarios de prueba**: para probar la agenda hace falta al menos un usuario `VENDEDOR` activo. Hay que crearlo desde el panel de administrador.
