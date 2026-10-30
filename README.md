# MS Pedido

Microservicio para la gestión de pedidos.

## Integrantes

- Antonella Castillo
- Pedro Lopez
- Eduardo Fernandez

## Convenciones de commits

Se utiliza una convención basada en **Conventional Commits**. Cada commit debe tener el formato:

```text
tipo(alcance): descripción breve
```

Tipos permitidos:

- `feat`: incorpora una funcionalidad.
- `fix`: corrige un error.
- `refactor`: reorganiza el código sin cambiar su comportamiento.
- `test`: agrega o modifica pruebas.
- `docs`: actualiza la documentación.
- `chore`: realiza tareas de mantenimiento o configuración.

Reglas:

- Escribir la descripción en modo imperativo y en minúsculas.
- Mantener el asunto breve, preferentemente de hasta 72 caracteres.
- Realizar commits pequeños, relacionados con un único cambio.
- No incluir credenciales, archivos generados ni cambios sin relación.

Ejemplos:

```text
feat(pedidos): agregar actualización del estado
fix(pedidos): validar identificador inexistente
test(service): cubrir creación de pedidos
docs(readme): documentar flujo de contribución
```

## Gitflow y naming de ramas

El proyecto utiliza **Gitflow** como estrategia de trabajo. Gitflow separa el desarrollo continuo de las versiones estables mediante una rama `develop` y una rama `main`. Esto permite organizar entregas, preparar releases y corregir problemas urgentes sin mezclar cambios incompletos con producción.

Se elige Gitflow sobre **Trunk-Based Development** porque el equipo necesita trabajar con versiones y entregas controladas, mantener una rama estable y agrupar cambios antes de publicarlos. Trunk-Based Development favorece integrar cambios pequeños directamente en una única rama principal y requiere una integración continua muy frecuente, feature flags y una disciplina de despliegue más madura. Para este proyecto, Gitflow ofrece una separación más clara entre desarrollo, preparación de release y producción.

Las ramas deben utilizar el formato:

```text
tipo/descripcion-corta
```

Tipos de rama:

- `main`: contiene únicamente versiones estables listas para producción.
- `develop`: integra el trabajo terminado que formará parte de la próxima versión.
- `feature/`: nueva funcionalidad, creada desde `develop` y fusionada hacia `develop`.
- `fix/`: corrección de errores.
- `release/`: preparación de una versión, creada desde `develop` y fusionada hacia `main` y `develop`.
- `hotfix/`: corrección urgente sobre producción, creada desde `main` y fusionada hacia `main` y `develop`.
- `refactor/`: mejora interna sin cambio funcional, creada desde `develop`.
- `test/`: incorporación o ajuste de pruebas.
- `docs/`: cambios de documentación.
- `chore/`: mantenimiento y configuración.

Ejemplos:

```text
feature/crear-pedido
fix/validar-estado-pedido
release/1.0.0
hotfix/corregir-calculo-total
test/pedido-service
docs/actualizar-readme
```

Se recomienda usar nombres descriptivos, en minúsculas, sin espacios ni acentos. Las ramas de trabajo deben mantenerse actualizadas con su rama base antes de solicitar el merge.

## Flujo de merge

1. Crear una rama `feature`, `fix`, `refactor`, `test`, `docs` o `chore` desde `develop`.
2. Implementar el cambio mediante commits pequeños y descriptivos.
3. Ejecutar las pruebas y verificar que el proyecto compile correctamente.
4. Actualizar la rama con la última versión de `develop` y resolver conflictos localmente.
5. Abrir un Pull Request hacia `develop` con una descripción del cambio, su motivación y las pruebas realizadas.
6. Solicitar al menos una revisión de otro integrante del equipo.
7. Resolver las observaciones y actualizar el Pull Request.
8. Hacer merge únicamente cuando las revisiones y verificaciones hayan sido aprobadas.
9. Eliminar la rama de trabajo después del merge.

Para publicar una versión, crear una rama `release/x.y.z` desde `develop`, completar allí los ajustes finales y abrir Pull Requests hacia `main` y `develop`. Después del merge a `main`, crear una etiqueta con la versión, por ejemplo `v1.0.0`. Para una corrección urgente, crear `hotfix/...` desde `main`, validar el cambio y fusionarlo tanto en `main` como en `develop`.

Todos los merges deben realizarse mediante Pull Request. Para ramas de trabajo se recomienda **Squash and merge**; para `release` y `hotfix` se debe conservar la trazabilidad de la versión mediante un merge commit cuando sea necesario.

## Pipelines y sus funciones

Los pipelines automatizan las validaciones y entregas del microservicio. Deben ejecutarse en cada Pull Request y en las ramas protegidas (`develop` y `main`).

### Pipeline de Integración Continua (CI)

Se ejecuta al crear o actualizar un Pull Request y al hacer push a `develop` o `main`.

- Descargar el código y configurar la versión de Java.
- Restaurar dependencias de Maven.
- Compilar el proyecto con Maven.
- Ejecutar las pruebas unitarias y de integración.
- Publicar los reportes de pruebas.
- Informar el resultado para impedir el merge si falla una validación.

### Pipeline de Calidad y Seguridad

Se ejecuta junto con el pipeline de CI y antes de aceptar un Pull Request.

- Revisar formato, convenciones y calidad del código.
- Analizar cobertura de pruebas.
- Detectar vulnerabilidades en dependencias.
- Buscar secretos o credenciales expuestas.
- Revisar la calidad de la imagen, si se construye un contenedor.
- Generar un reporte para corregir hallazgos antes del despliegue.

### Pipeline de Construcción y Publicación

Se ejecuta después de que CI y calidad hayan finalizado correctamente.

- Construir el artefacto ejecutable del proyecto.
- Generar la imagen Docker del microservicio, si corresponde.
- Etiquetar la imagen con el commit y la versión del release.
- Publicar el artefacto o la imagen en el registro configurado.
- Conservar los artefactos para auditoría y despliegues posteriores.

### Pipeline de Despliegue Continuo (CD)

Se ejecuta cuando se fusiona un Pull Request de release o hotfix hacia `main`.

- Desplegar primero en un ambiente de pruebas o staging.
- Ejecutar pruebas de humo y verificaciones de salud.
- Solicitar aprobación manual antes de producción cuando aplique.
- Desplegar la versión aprobada en producción.
- Verificar disponibilidad del servicio y sus endpoints principales.
- Notificar el resultado y permitir rollback a la última versión estable.

La promoción entre ambientes debe utilizar el mismo artefacto validado en CI. Ningún pipeline debe incluir secretos directamente en el repositorio; deben configurarse como variables protegidas o secretos del proveedor de CI/CD.

## Estrategia de revisión

Cada Pull Request debe ser revisado por al menos un integrante distinto de quien realizó el cambio. La revisión debe comprobar:

- Correctitud funcional y cumplimiento del objetivo.
- Claridad, legibilidad y coherencia con la estructura del proyecto.
- Manejo de errores y validación de entradas.
- Cobertura y calidad de las pruebas.
- Ausencia de credenciales o información sensible.
- Compatibilidad con las funcionalidades existentes.

Los comentarios deben ser concretos, respetuosos y orientados a una mejora. Las observaciones bloqueantes deben resolverse antes del merge; las sugerencias menores pueden registrarse como tareas posteriores cuando el equipo lo acuerde.

## Responsabilidades del equipo

Todos los integrantes pueden crear ramas, implementar cambios y revisar Pull Requests. Quien desarrolla un cambio es responsable de:

- Mantener actualizada la rama de trabajo.
- Ejecutar y documentar las validaciones realizadas.
- Responder las observaciones de la revisión.
- Confirmar que el Pull Request esté listo antes de solicitar el merge.

## Uso de Inteligencia Artificial

Durante el desarrollo del proyecto se utilizó ChatGPT como herramienta de apoyo. **OpenAI (2026)** fue utilizado para resolver dudas técnicas, apoyar la identificación de errores, sugerir alternativas de implementación y colaborar en la elaboración de documentación.

Las respuestas proporcionadas por la herramienta fueron revisadas y validadas por los integrantes del equipo antes de ser utilizadas en el proyecto.

## Ejecución local con Docker Compose

El proyecto utiliza Docker Compose para ejecutar el microservicio `ms-pedido` junto con una base de datos MySQL 8.4.

Esta configuración permite levantar ambos servicios de forma conjunta, sin necesidad de instalar MySQL directamente en el computador.

### Requisitos

Para ejecutar el proyecto se necesita:

- Docker Desktop instalado y en ejecución.
- Docker Compose disponible.
- Tener descargado el repositorio del proyecto.

### Configuración de variables de entorno

El proyecto incluye un archivo `.env.example` con las variables necesarias para configurar MySQL y el puerto de la aplicación.

Desde la carpeta raíz del proyecto, crear una copia del archivo:

```powershell
Copy-Item .env.example .env
```

Luego, abrir `.env` y reemplazar las contraseñas de ejemplo por contraseñas propias.

Las variables utilizadas son:

| Variable | Descripción |
|---|---|
| `MYSQL_DATABASE` | Nombre de la base de datos. |
| `MYSQL_USER` | Usuario utilizado por la aplicación para conectarse a MySQL. |
| `MYSQL_PASSWORD` | Contraseña del usuario de MySQL. |
| `MYSQL_ROOT_PASSWORD` | Contraseña del administrador de MySQL. |
| `APP_PORT` | Puerto de acceso al microservicio desde el computador. |

El archivo `.env` está excluido del repositorio mediante `.gitignore`, para evitar publicar credenciales locales.

### Construcción e inicio de contenedores

Para construir la imagen de Spring Boot e iniciar los servicios, ejecutar:

```powershell
docker compose up --build -d
```

Este comando construye la imagen del microservicio utilizando el `Dockerfile` y levanta los contenedores de Spring Boot y MySQL.

La opción `-d` permite ejecutarlos en segundo plano.

### Verificación de los contenedores

Para revisar el estado de los servicios:

```powershell
docker compose ps
```

Se espera que los servicios `mysql` y `ms-pedido` estén en ejecución.

MySQL incluye un `healthcheck` que comprueba su disponibilidad. Además, el servicio `ms-pedido` utiliza `depends_on` para esperar hasta que MySQL se encuentre saludable antes de iniciar.

### Revisión de los logs

Para consultar los registros del microservicio:

```powershell
docker compose logs ms-pedido
```

Para consultar los registros de MySQL:

```powershell
docker compose logs mysql
```

Estos comandos permiten revisar el inicio de los servicios y detectar posibles errores de conexión.

### Acceso a la API

Por defecto, la aplicación utiliza el puerto `8080`.

El endpoint de pedidos se encuentra disponible en:

```text
http://localhost:8080/api/v1/pedidos
```

Se puede realizar una solicitud GET desde Postman para consultar los pedidos registrados.

Si el puerto `8080` está ocupado, se puede modificar la variable `APP_PORT` del archivo `.env`, por ejemplo:

```env
APP_PORT=8081
```

En ese caso, la dirección de acceso será:

```text
http://localhost:8081/api/v1/pedidos
```

El cambio de puerto afecta el acceso desde el computador, mientras que Spring Boot continúa utilizando el puerto `8080` dentro del contenedor.

### Persistencia de los datos

La configuración utiliza un volumen llamado `mysql_data`, asociado al directorio de almacenamiento de MySQL.

Esto permite conservar los datos aunque los contenedores sean detenidos o recreados, siempre que no se elimine el volumen.

### Detención de los servicios

Para detener y retirar los contenedores:

```powershell
docker compose down
```

Este comando conserva el volumen de MySQL.

Para volver a iniciar los servicios:

```powershell
docker compose up -d
```

No se recomienda utilizar `docker compose down -v` si se desea conservar la información, ya que esa opción también elimina los volúmenes definidos por el proyecto.

### Problemas frecuentes

**Puerto ocupado:** si el puerto `8080` está siendo utilizado por otra aplicación, modificar `APP_PORT` en el archivo `.env` y recrear los contenedores.

**Docker no está iniciado:** verificar que Docker Desktop esté ejecutándose antes de utilizar Docker Compose.

**Error de conexión con MySQL:** revisar el estado del servicio y sus registros mediante `docker compose ps` y `docker compose logs mysql`.

**Cambios en las contraseñas:** las variables de inicialización de MySQL se aplican cuando se crea una base de datos nueva. Si ya existe un volumen con datos, modificar `.env` no cambia automáticamente las contraseñas almacenadas en MySQL.