# Sonido Vivo — Backend de microservicios

Backend académico para Sonido Vivo. El proyecto utiliza Java 21, Spring Boot 4.1.2, Spring Cloud 2025.1.4, MySQL, Spring Data JPA, Hibernate, Flyway, Lombok, Spring Security y JWT.

El backend conserva los dominios que tienen una responsabilidad propia y una base de datos independiente. Los servicios se registran en Eureka y el frontend consume las API a través de Spring Cloud Gateway.

## 1. Estructura

| Módulo | Puerto | Responsabilidad | Base de datos |
|---|---:|---|---|
| `auth` | 8080 | Inicio de sesión, registro de clientes y administración de usuarios | `sonido_vivo_auth` |
| `catalogo` | 8081 | Categorías, productos y descuentos idempotentes de stock | `sonido_vivo_catalogo` |
| `mensajeria` | 8082 | Contacto, preguntas frecuentes y revisión de mensajes | `sonido_vivo_mensajeria` |
| `carrito` | 8083 | Carrito persistente por cliente | `sonido_vivo_carrito` |
| `pedidos` | 8085 | Checkout, pedidos, estados, cálculo de totales y confirmación de compra pagada | `sonido_vivo_pedidos` |
| `pagos` | 8086 | Procesamiento académico del pago iniciado por el cliente | `sonido_vivo_pagos` |
| `ventas` | 8087 | Registro de ventas pagadas y resúmenes diarios/mensuales | `sonido_vivo_ventas` |
| `eureka-server` | 8761 | Descubrimiento de servicios | No utiliza base de datos |
| `api-gateway` | 8090 | Punto de entrada HTTP para el frontend | No utiliza base de datos |
| `common-security` | — | Componentes compartidos de JWT y autenticación interna | Biblioteca compartida |

Cada microservicio persistente tiene su propia base de datos y sus propias migraciones Flyway. MySQL puede ejecutarse desde Laragon o desde Docker Compose.

## 2. Reglas de negocio aplicadas

### Autenticación y usuarios

- Los roles del sistema son `ADMIN`, `VENDEDOR` y `CLIENTE`.
- El registro público crea clientes.
- La administración de usuarios está restringida al administrador.
- Las contraseñas se almacenan con BCrypt.
- Los usuarios de prueba de Sonido Vivo se conservan con sus datos originales.
- Las cuentas de prueba usan `1234` como contraseña académica. Esta configuración permite 4 a 10 caracteres.
- ADMIN crea cuentas internas ADMIN o VENDEDOR y asigna su contraseña inicial. También puede asignar una contraseña a cuentas internas heredadas que aún no tengan una.
- El registro público crea CLIENTE directamente; no requiere correo ni activación. Los endpoints heredados de activación permanecen disponibles por compatibilidad, pero no forman parte del flujo de integración.
- Se valida el dígito verificador del RUN tanto en el frontend como en el backend.

### Catálogo

- Se conservan los campos de `Producto` del frontend: código, nombre, categoría, marca, modelo, precio, stock, imagen y descripción.
- La relación de datos es `Categoria 1:N Producto`.
- El DTO de respuesta de producto incluye la categoría.
- La base de datos conserva la ruta de la imagen; los archivos de imagen permanecen en `public/img` del frontend.
- ADMIN y VENDEDOR pueden crear, editar, eliminar productos y actualizar stock.
- La creación del pedido no descuenta stock. Catálogo descuenta las unidades únicamente cuando Pedidos confirma el pago; el descuento se registra por código de pedido para impedir duplicados.

### Carrito

- Los clientes autenticados tienen un carrito persistido en `sonido_vivo_carrito`.
- Un visitante puede preparar un carrito temporal en el estado de React. Al iniciar sesión, los productos temporales se incorporan al carrito persistente.
- Al volver a iniciar sesión, el frontend consulta el carrito real.
- El checkout no envía precios ni productos confiables desde el navegador: Pedidos consulta el carrito del cliente y obtiene los datos y precios actuales desde Catálogo.
- El pedido permanece sin pagar y el carrito se conserva hasta que el pago se complete.

### Pedidos

- El backend calcula subtotal, IVA, despacho y total.
- El despacho tiene costo de $8.990 y el retiro en tienda no agrega costo de envío.
- Para despacho se exige dirección y número de vivienda. El código postal es opcional. Para retiro no se exige dirección.
- Los estados corresponden a los valores que ya utiliza Sonido Vivo: `Confirmado`, `En preparación`, `Despachado` y `Entregado`.
- Los cambios de estado no permiten retroceder.
- Los pedidos guardan una copia de los datos de productos y cliente usados al momento de la compra.
- El cliente solo consulta sus propios pedidos. Administrador y vendedor pueden consultar y gestionar pedidos.
- Los estados operacionales solo pueden avanzar cuando el pedido ya está pagado y siempre una etapa a la vez.

Los pedidos ficticios del arreglo original del frontend no se insertan en la base de datos: algunos correos no corresponden a las cuentas de prueba y sus líneas no representan una relación verificable con el catálogo actual. Los pedidos nuevos se crean desde el carrito persistente.

### Pagos y ventas

- CLIENTE inicia el pago durante el checkout mediante `POST /api/pagos/{codigoPedido}/procesar`.
- No se solicitan, almacenan ni transmiten números de tarjeta, CVV ni otros datos bancarios.
- No existe una conexión real con Transbank, Klap ni otro proveedor de pago. La operación es una simulación académica que registra el método seleccionado.
- Pagos valida la titularidad del pedido y solicita a Pedidos marcarlo como pagado. Pedidos primero solicita a Catálogo el descuento atómico del stock; solo después actualiza `pagado=true`.
- Una vez confirmado el pedido, Pedidos notifica automáticamente a Ventas. Si Ventas no está disponible, la compra sigue completándose sin esperar a Ventas y queda pendiente un reintento de baja frecuencia; el registro de ventas es idempotente.
- El panel administrativo permite consultar resúmenes de ventas por día y mes. Solo ADMIN puede acceder a esas estadísticas.

### Mensajería

- Se conserva el patrón del ejemplo de integración del formulario del profesor: `POST /api/contactos` con nombre, correo y mensaje.
- Se agregan `tipoMensaje`, `tipoRemitente`, `revisado` e identificador.
- `tipoRemitente` lo determina el backend a partir de la sesión. No se acepta como dato confiable del navegador.
- El sitio público puede enviar mensajes. Administrador y vendedor pueden consultarlos y marcarlos como revisados.
- Se mantienen las migraciones anteriores de Mensajería y se agregan migraciones posteriores para los nuevos campos.

## 3. Rutas principales detrás de API Gateway

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/login` | Público |
| POST | `/api/auth/registro` | Público |
| POST | `/api/auth/logout` | Público |
| GET | `/api/auth/me` | Usuario autenticado |
| GET | `/api/auth/usuarios` | Administrador |
| GET/POST/PUT/PATCH | `/api/catalogo/**` | Según operación y rol |
| GET/POST/PUT/DELETE | `/api/carrito/**` | Cliente |
| GET/POST/PATCH | `/api/pedidos/**` | Según operación y rol |
| GET/POST | `/api/pagos/**` | CLIENTE procesa su pago; ADMIN consulta pagos |
| POST | `/api/contactos` | Público |
| GET/PATCH | `/api/contactos/**` | Administrador o vendedor |
| GET | `/api/ventas/resumen/**` | Administrador |

Las rutas internas entre servicios requieren `X-Internal-Token` y no deben utilizarse desde el navegador.

## 4. Seguridad de sesión

El JWT se entrega en una cookie `SV_ACCESS` con `HttpOnly`, `SameSite=Strict` y ruta `/`. En producción debe habilitarse `Secure` mediante `AUTH_COOKIE_SECURE=true`.

El frontend conserva únicamente el perfil de usuario en `sessionStorage`; no guarda el JWT en `localStorage` ni en `sessionStorage`. Las solicitudes del frontend incluyen `credentials: "include"` y `X-Requested-With`. El backend verifica el JWT en cada servicio.

En desarrollo, Vite reenvía `/api` al Gateway en `http://localhost:8090`. Se recomienda mantener el frontend y el Gateway bajo el mismo host para que la cookie funcione correctamente.

## 5. Preparar MySQL con Laragon

1. Inicia MySQL desde Laragon.
2. Comprueba que el puerto sea `3306`.
3. Abre una terminal en la raíz del backend.
4. Ejecuta el archivo de inicialización para crear las bases vacías:

```powershell
Get-Content .\\mysql\\init.sql | mysql -u root -p
```

Si Laragon utiliza una contraseña vacía, omite `-p`.

El archivo crea las bases de datos, pero no las tablas. Las tablas se crean exclusivamente con Flyway cuando inicia cada microservicio.

La configuración local utiliza `localhost:3306`, usuario `root` y contraseña vacía por defecto. Si Laragon utiliza otra contraseña, configura `DB_PASSWORD` en `.env`.

## 6. Variables de entorno

Copia `.env.example` a `.env` y reemplaza los valores de ejemplo. Las configuraciones de VS Code aplican límites de memoria por proceso desde `.vscode/launch.json`; Docker Compose aplica límites equivalentes en cada contenedor. Para iniciar un JAR manualmente desde PowerShell, define `JAVA_TOOL_OPTIONS` en esa sesión antes de ejecutar `java -jar` (no lo apliques a una compilación Maven del reactor completo).

Genera un secreto JWT en PowerShell:

```powershell
$bytes = New-Object byte[] 48
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

Genera un token interno:

```powershell
[guid]::NewGuid().ToString("N") + [guid]::NewGuid().ToString("N")
```

Para esta entrega académica puedes usar `1234` en `ADMIN_BOOTSTRAP_PASSWORD`. Las cuentas de prueba también quedan configuradas con `1234`. No uses estas claves reales ni subas `.env` a GitHub.

Para cargar las variables en la terminal actual:

```powershell
.\scripts\cargar-env.ps1
```

El script carga las variables de `.env` en el proceso actual de PowerShell. Debes ejecutarlo en cada terminal donde iniciarás un microservicio.

## 7. Ejecutar los microservicios localmente

Inicia cada comando en una terminal independiente. Primero carga las variables de entorno en cada terminal con `.\scripts\cargar-env.ps1`.

Orden recomendado:

1. MySQL (si no se utiliza Docker).
2. Eureka.
3. Los siete microservicios de negocio pueden iniciarse independientemente; para una prueba ordenada, inicia Auth, Catálogo, Carrito, Pedidos, Pagos, Ventas y Mensajería.
4. API Gateway.

Pedidos no necesita que Pagos ni Ventas estén disponibles para arrancar. La notificación de venta se produce después de confirmar el pago y los fallos de Ventas no bloquean la compra.

Comandos desde la raíz del proyecto:

```powershell
.\mvnw.cmd -pl eureka-server spring-boot:run
.\mvnw.cmd -pl auth spring-boot:run
.\mvnw.cmd -pl catalogo spring-boot:run
.\mvnw.cmd -pl carrito spring-boot:run
.\mvnw.cmd -pl pagos spring-boot:run
.\mvnw.cmd -pl ventas spring-boot:run
.\mvnw.cmd -pl pedidos spring-boot:run
.\mvnw.cmd -pl mensajeria spring-boot:run
.\mvnw.cmd -pl api-gateway spring-boot:run
```

Eureka queda disponible en `http://localhost:8761` y Gateway en `http://localhost:8090`.

Para compilar todos los módulos:

```powershell
.\mvnw.cmd clean package
```

## 8. Ejecutar con Docker Compose

Docker Compose utiliza un MySQL propio. Por defecto publica MySQL en el puerto `3307` para no interferir con el MySQL de Laragon. Las imágenes de los nueve procesos Java utilizan el `Dockerfile` de la raíz: Maven utiliza una etapa de compilación común y cacheable; cada imagen final copia únicamente su JAR. Con BuildKit, Compose puede reutilizar esa etapa y evitar compilaciones Maven repetidas.

1. Copia `.env.example` a `.env`.
2. Define `MYSQL_ROOT_PASSWORD`, los secretos y la contraseña inicial de administrador.
3. Mantén `MYSQL_HOST_PORT=3307` si el puerto `3306` está ocupado por Laragon.
4. Ejecuta:

```powershell
docker compose up --build -d
```

Para revisar los servicios:

```powershell
docker compose ps
docker compose logs -f
```

Para detenerlos:

```powershell
docker compose down
```

No utilices `docker compose down -v` salvo que quieras eliminar también los datos persistidos.

## 9. Integración con el frontend

El frontend utiliza `VITE_API_URL=/api`. Su configuración de Vite reenvía las peticiones al Gateway `http://localhost:8090`.

El detalle de los archivos que se deben reemplazar, agregar y eliminar está en el paquete independiente `sonido-vivo-frontend-integracion.zip`.

## 10. Consideraciones de entrega

- No subir `.env`, contraseñas, tokens ni secretos a GitHub.
- Subir migraciones Flyway junto con cada cambio de entidad.
- No editar una migración Flyway que ya haya sido ejecutada en otra máquina; crear una migración posterior.
- Mantener las bases de datos separadas por microservicio.
- No insertar datos de prueba directamente desde controladores.
- Mantener el frontend sin arreglos de productos, usuarios, pedidos o categorías como fuente de persistencia.
- El directorio `coverage` y las pruebas existentes del frontend deben conservarse.

La compilación Maven y las pruebas npm deben ejecutarse en el equipo del proyecto. En el entorno donde se preparó este paquete no estaban disponibles las dependencias Maven/npm necesarias para completar esas ejecuciones.


## 11. Documentación Swagger / OpenAPI

La documentación interactiva de las API REST está disponible desde Swagger UI. El API Gateway está configurado para reunir las especificaciones de los siete microservicios de negocio en una sola interfaz cuando Eureka y las rutas están operativas; si el agregador presenta un error, las interfaces Swagger individuales son suficientes para probar cada API:

- **Swagger UI centralizado:** `http://localhost:8090/swagger-ui.html`
- **OpenAPI de Auth:** `http://localhost:8090/auth/v3/api-docs`
- **OpenAPI de Catálogo:** `http://localhost:8090/catalogo/v3/api-docs`
- **OpenAPI de Mensajería:** `http://localhost:8090/mensajeria/v3/api-docs`
- **OpenAPI de Carrito:** `http://localhost:8090/carrito/v3/api-docs`
- **OpenAPI de Pedidos:** `http://localhost:8090/pedidos/v3/api-docs`
- **OpenAPI de Pagos:** `http://localhost:8090/pagos/v3/api-docs`
- **OpenAPI de Ventas:** `http://localhost:8090/ventas/v3/api-docs`

También es posible abrir Swagger UI directamente en cada microservicio:

| Microservicio | Swagger UI |
|---|---|
| Auth | `http://localhost:8080/swagger-ui.html` |
| Catálogo | `http://localhost:8081/swagger-ui.html` |
| Mensajería | `http://localhost:8082/swagger-ui.html` |
| Carrito | `http://localhost:8083/swagger-ui.html` |
| Pedidos | `http://localhost:8085/swagger-ui.html` |
| Pagos | `http://localhost:8086/swagger-ui.html` |
| Ventas | `http://localhost:8087/swagger-ui.html` |

Las operaciones protegidas con JWT están documentadas con el esquema `bearerAuth`. En Swagger UI, utiliza **Authorize** e ingresa un token JWT válido con el formato Bearer. Las operaciones internas entre microservicios documentan el encabezado `X-Internal-Token`; ese secreto debe mantenerse fuera del frontend y de repositorios públicos.

Para reducir el consumo al ejecutar los servicios desde VS Code se fijaron límites de heap por proceso en `.vscode/launch.json`. Docker Compose aplica límites equivalentes mediante `JAVA_TOOL_OPTIONS`. Además, los pools Hikari se limitaron a 3 conexiones por servicio y los hilos Tomcat a 40. Son límites operativos, no requisitos académicos adicionales; si el entorno muestra presión de memoria, deben revisarse con mediciones antes de aumentarlos.

Los recursos de documentación (`/v3/api-docs` y `/swagger-ui/**`) están permitidos por Spring Security para facilitar las pruebas. Esta excepción no modifica los permisos de los endpoints de negocio: sus reglas de autenticación y autorización permanecen vigentes.
