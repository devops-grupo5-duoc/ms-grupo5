
# MS Pedido

Microservicio desarrollado con Java 21, Spring Boot y Maven para la gestión de pedidos. El repositorio utiliza GitFlow y GitHub Actions para automatizar pruebas, análisis de seguridad, construcción de imágenes Docker y despliegue.

## Integrantes

- Antonella Castillo
- Pedro Lopez
- Eduardo Fernandez

## Convenciones de commits

Se utiliza una convención basada en **Conventional Commits**:

```text
tipo(alcance): descripción breve
```

Tipos utilizados:

- `feat`: incorpora una funcionalidad.
- `fix`: corrige un error.
- `refactor`: reorganiza código sin cambiar su comportamiento.
- `test`: incorpora o modifica pruebas.
- `docs`: actualiza documentación.
- `chore`: mantenimiento y configuración.
- `ci`: cambios en los pipelines.
- `release`: preparación de versiones.

El alcance entre paréntesis puede omitirse en los títulos de Pull Requests dirigidos a `main`, porque el validador actual exige estrictamente `tipo: descripción`.

Se procura escribir asuntos breves, descriptivos, en minúsculas y relacionados con un único cambio. No deben incluirse contraseñas ni secretos en los commits.

Ejemplos:

```text
feat(pedidos): agregar actualización del estado
fix(pedidos): validar identificador inexistente
test(pedidos): agregar pruebas unitarias del servicio
docs: documentar pipelines de la entrega 2
```

## GitFlow y nombres de ramas

El proyecto utiliza `main` como rama estable y `develop` para integrar los cambios en desarrollo. Las ramas `feature/*` se crean para cambios funcionales; `hotfix/*` se utiliza para correcciones urgentes sobre la versión estable.

También se usan ramas de trabajo descriptivas, por ejemplo `docs/*`, `test/*`, `refactor/*` o `chore/*`. Esta es una adaptación de GitFlow al tamaño y necesidades del equipo.

Convención:

```text
tipo/descripcion-corta
```

Ejemplos:

```text
feature/crear-pedido
hotfix/validacion-sucursal
docs/actualizar-readme-ep2
```

### Flujo de colaboración

1. Crear una rama de trabajo a partir de `develop`, o de `main` cuando se trate de un `hotfix`.
2. Realizar los cambios y registrarlos con commits descriptivos.
3. Ejecutar pruebas y revisar los archivos modificados.
4. Subir la rama y abrir un Pull Request.
5. Revisar comentarios y resultados de GitHub Actions antes de fusionar.
6. Integrar las ramas de trabajo a `develop`; los cambios para la versión estable llegan a `main` desde `develop` o `hotfix/*`.

El workflow `control-pr-develop.yml` comprueba que el origen de los Pull Requests a `develop` tenga un nombre permitido. El workflow `control-pr-main.yml` exige que el origen sea `develop` o `hotfix/*`, que el título tenga un prefijo válido seguido de dos puntos y que la descripción no esté vacía.

La revisión de cambios considera funcionalidad, manejo de errores, pruebas, claridad del código, ausencia de credenciales y compatibilidad con el resto del microservicio.

## Pipelines implementados

Los archivos están en `.github/workflows/`. Se describen aquí sus eventos y pasos reales, diferenciando la configuración de los resultados observados.

| Archivo | Activación | Función |
| --- | --- | --- |
| `ci-develop.yml` | `push` a `develop`; Pull Requests a `develop` y `main` | Ejecutar Maven y validar el sistema con Docker Compose. |
| `security-pipeline.yml` | `push` y Pull Requests a `develop`/`main`; llamado desde otro workflow | Ejecutar Snyk y SonarCloud. |
| `cd-main.yml` | `push` a `main` o ejecución manual | Verificar, construir y publicar imagen, desplegar en EC2 y ejecutar pruebas de humo. |
| `control-pr-develop.yml` | Pull Requests hacia `develop` | Validar el nombre de la rama de origen. |
| `control-pr-main.yml` | Pull Requests hacia `main` | Validar origen, título y descripción. |
| `notify-on-failure.yml` | Finalización de ciertos workflows | Intentar registrar un Issue cuando concluyen con error. |

### Integración continua: `ci-develop.yml`

El trabajo **Compilar y probar** descarga el código, configura Eclipse Temurin 21, habilita Maven Wrapper y ejecuta:

```bash
./mvnw -B verify
```

Los reportes de pruebas (`target/surefire-reports`) se publican como artefactos, incluso cuando las pruebas fallan.

Si el trabajo anterior es exitoso, **Integración con Docker Compose** crea un `.env` temporal exclusivo del entorno de CI, valida `docker-compose.yml`, construye y levanta los servicios, espera que `/actuator/health` responda con estado `UP` y consulta `/api/v1/pedidos`. Al terminar, ejecuta la limpieza de los contenedores temporales.

### Pruebas automatizadas

La rama `develop` incluye `src/test/java/com/example/ms_pedido/service/PedidoServiceTest.java`, con ocho pruebas unitarias del servicio de pedidos. Comprueban consultas, creación, actualización parcial, eliminación y comportamiento ante registros inexistentes. También existe la prueba de contexto de Spring Boot.

La ejecución del workflow **CI #42**, correspondiente al commit `0ed9043` en `develop`, terminó en estado **Success**: `Compilar y probar` e `Integración con Docker Compose` fueron exitosos. En ese ciclo se registraron nueve pruebas exitosas, sin fallos ni errores.

**Estado de ramas al preparar esta entrega:** las ocho pruebas adicionales permanecen en `develop`. Su incorporación posterior a `main` mediante el PR #40 fue revertida mediante un `hotfix`; por tanto, no debe afirmarse que el archivo `PedidoServiceTest.java` existe actualmente en `main`.

### Calidad y seguridad: `security-pipeline.yml`

El trabajo **Snyk Security Scan** configura Java 21, instala Snyk y examina las dependencias declaradas en `pom.xml`:

```bash
snyk test --file=pom.xml --severity-threshold=high --json-file-output=snyk-report.json
```

Un hallazgo no exceptuado de severidad alta o crítica puede provocar que este paso falle. El reporte JSON se intenta publicar como artefacto aunque el análisis falle. El repositorio también contiene un archivo `.snyk` con una excepción temporal para una vulnerabilidad transitiva; esa excepción debe revisarse periódicamente y no equivale a que no existan riesgos.

El trabajo **SonarCloud Analysis** depende del resultado de Snyk. Ejecuta el análisis mediante Maven y espera la evaluación del *Quality Gate* (`sonar.qualitygate.wait=true`). Que el análisis termine correctamente no significa, por sí solo, que exista cobertura de pruebas importada a SonarCloud.

Además, en la configuración de seguridad del repositorio se habilitaron **Dependabot alerts** y **Dependabot security updates** para detectar vulnerabilidades conocidas en dependencias y proponer actualizaciones cuando hay una corrección disponible. En la comprobación realizada no había alertas abiertas. Las actualizaciones periódicas de versiones mediante `dependabot.yml` no estaban configuradas.

### Construcción, publicación y despliegue: `cd-main.yml`

El workflow **CD - Main** se activa al hacer `push` a `main` o manualmente mediante `workflow_dispatch`. Sus trabajos son:

1. **Seguridad:** reutiliza `security-pipeline.yml`.
2. **Pruebas unitarias:** ejecuta `./mvnw -B verify` y publica los reportes.
3. **Publicar imagen Docker:** tras superar seguridad y pruebas, construye la imagen con Docker Buildx y la publica en GitHub Container Registry (`ghcr.io`). La etiqueta utiliza el SHA del commit y también `latest`.
4. **Desplegar en EC2:** abre una conexión SSH con las credenciales guardadas en GitHub Secrets, obtiene el estado de `main`, inicia sesión en GHCR y utiliza `docker compose pull ms-pedido` y `docker compose up -d --remove-orphans` para actualizar los contenedores.
5. **Prueba de humo:** espera que la API responda correctamente en `/actuator/health`, consulta `/api/v1/pedidos` y comprueba mediante `/api/v1/info` que se esté ejecutando el SHA esperado.

La configuración utiliza los secretos `EC2_HOST`, `EC2_USER` y `EC2_SSH_KEY` para la conexión; los valores secretos no deben incluirse en el repositorio ni en la documentación.

**Trazabilidad:** los Pull Requests identifican los cambios y revisiones; cada ejecución de GitHub Actions queda asociada a una rama y un commit; la imagen Docker se etiqueta con el SHA y el despliegue comprueba el commit publicado.

**Resultados y limitaciones conocidas:** existe evidencia de una ejecución anterior exitosa del despliegue. Sin embargo, un intento posterior relacionado con el PR #40 falló antes de conectarse al servidor EC2 por un tiempo de espera agotado en SSH (`puerto 22`). Este fallo de conectividad no demuestra un error del código, pero tampoco permite afirmar que ese último despliegue se completó. El workflow de notificación también registró anteriormente un fallo de permisos (`403`) al intentar crear un Issue; las ejecuciones posteriores omitidas (*skipped*) no acreditan que esa autorización se haya corregido.

## Contenerización con Docker

El `Dockerfile` utiliza una construcción en dos etapas:

1. **Build:** `eclipse-temurin:21-jdk`, Maven Wrapper y empaquetado del JAR sin repetir pruebas dentro de la imagen.
2. **Runtime:** `eclipse-temurin:21-jre-alpine`; copia el JAR, expone el puerto `8080` y ejecuta la aplicación con un usuario sin privilegios de administrador.

Esta separación permite usar una imagen de ejecución más liviana que la imagen de compilación. Las pruebas se realizan en los trabajos de CI/CD antes de publicar la imagen.

## Ejecución local y orquestación con Docker Compose

El proyecto define dos servicios en `docker-compose.yml`:

- `mysql`: base de datos MySQL 8.4, con volumen persistente `mysql_data` y comprobación de salud.
- `ms-pedido`: aplicación Spring Boot, con puerto configurable, comprobación de salud, límites de memoria y CPU y dependencia de MySQL saludable.

Ambos servicios comparten la red `ms-pedido-network`. El servicio de la aplicación puede utilizar una imagen construida localmente o una imagen publicada en GHCR mediante la variable `APP_IMAGE`.

### Requisitos

- Docker Desktop o Docker Engine con Docker Compose.
- Repositorio descargado.
- Variables de entorno configuradas.

### Archivo `.env`

En PowerShell, desde la raíz del proyecto:

```powershell
Copy-Item .env.example .env
```

Editar `.env` con contraseñas propias. Las variables son:

| Variable | Descripción |
| --- | --- |
| `MYSQL_DATABASE` | Nombre de la base de datos. |
| `MYSQL_USER` | Usuario utilizado por la aplicación. |
| `MYSQL_PASSWORD` | Contraseña de ese usuario. |
| `MYSQL_ROOT_PASSWORD` | Contraseña del usuario administrador de MySQL. |
| `APP_PORT` | Puerto de acceso desde el equipo anfitrión (predeterminado: `8080`). |

`.env` no debe subirse al repositorio y está contemplado en `.gitignore`.

### Construcción e inicio

```powershell
docker compose up --build -d
```

El comando construye el microservicio con el Dockerfile y levanta los dos servicios en segundo plano.

### Comprobaciones

```powershell
docker compose ps
docker compose logs ms-pedido
docker compose logs mysql
```

En Postman o mediante una petición HTTP se puede consultar:

```text
http://localhost:8080/actuator/health
http://localhost:8080/api/v1/pedidos
```

Si `APP_PORT=8081`, las rutas locales pasan a utilizar el puerto `8081`; dentro del contenedor Spring Boot sigue atendiendo en `8080`.

### Persistencia y detención

El volumen `mysql_data` conserva los datos cuando se detienen o recrean los contenedores, mientras el volumen no se elimine.

```powershell
docker compose down
```

Para volver a iniciar:

```powershell
docker compose up -d
```

No utilizar `docker compose down -v` si se requiere conservar la información, ya que elimina los volúmenes asociados.

### Problemas frecuentes

- **Puerto ocupado:** cambiar `APP_PORT` y recrear el contenedor.
- **Docker detenido:** iniciar Docker Desktop o el servicio Docker.
- **Error de MySQL:** revisar el estado y los registros del contenedor.
- **Cambio de contraseñas:** modificar `.env` no actualiza automáticamente los usuarios ya creados en un volumen de MySQL existente.
- **Despliegue SSH con timeout:** comprobar el estado de EC2 y la conectividad antes de volver a ejecutar un despliegue.

## Evidencias para la Entrega 2

| Indicador | Evidencia disponible o por comprobar |
| --- | --- |
| IE1 — Contenedores | `Dockerfile`, `docker-compose.yml` y construcción automatizada en `cd-main.yml`. |
| IE2 — Pruebas | `PedidoServiceTest.java` en `develop`, reportes Maven y ejecución exitosa CI #42 del commit `0ed9043`. |
| IE3 — Seguridad | `security-pipeline.yml`, resultados Snyk y SonarCloud, alertas de Dependabot y configuración de bloqueo por severidad alta/crítica. |
| IE4 — Despliegue | `cd-main.yml`, historial de publicación en GHCR, despliegue anterior exitoso y resultado del smoke test; revisar el fallo SSH posterior. |
| IE5 — Orquestación | `docker-compose.yml`, servicios, healthchecks, límites de recursos y ejecución exitosa de integración con Docker Compose. |

Las capturas y resultados deben corresponder a ejecuciones reales. Una ejecución histórica exitosa no implica que la versión más reciente se haya desplegado correctamente.

## Responsabilidades y revisión

Los integrantes del equipo participan en el desarrollo, la revisión de Pull Requests y la verificación de los pipelines. Antes de fusionar cambios se revisan las pruebas, los controles de seguridad y la descripción del Pull Request. Los problemas detectados se registran y se corrigen mediante nuevas ramas o tareas de seguimiento.

## Uso de inteligencia artificial

Se utilizó ChatGPT como apoyo para comprender errores de Git, GitHub Actions y Docker, organizar verificaciones y mejorar la claridad de la documentación. Las configuraciones, resultados y decisiones que se presenten deben ser comprobados y validados por el equipo. Las justificaciones técnicas finales, conclusiones y reflexiones individuales deben ser escritas por sus integrantes sin asistencia de IA, conforme a la pauta de evaluación.

## Conclusiones individuales

Las reflexiones personales se encuentran en la carpeta `conclusiones/`. Cada integrante debe revisar y mantener allí un texto propio sobre sus contribuciones, aprendizajes y dificultades, sin redacción asistida por IA.
