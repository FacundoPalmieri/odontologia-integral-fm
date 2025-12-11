## ADR: Gestión de archivos adjuntos en la aplicación

### Fecha: 11/12/2025
### Contexto: Feature attached files (documentos PDF e imágenes de perfil)

## 1. Contexto

La aplicación requiere almacenar documentos PDF asociados a usuarios y pacientes, así como imágenes de perfil (avatars).
Los requisitos principales son:

-   Guardar archivos de manera persistente con metadata asociada a la persona/paciente.

-   Control de acceso granular: usuarios solo pueden acceder a sus propios documentos, algunos roles especiales (ej. administradores) tienen permisos ampliados.

-   Garantizar consistencia y unicidad de nombres de archivo para evitar colisiones.

-   Permitir eliminación de archivos de manera segura, con registro de auditoría.

-   Respetar límites de tamaño y extensión configurables.

-   Evitar pérdida accidental de archivos mediante baja lógica y limpieza física programada.

-   Se deben tomar decisiones sobre dónde y cómo se almacenan los archivos, cómo se controla el acceso, y cómo se gestionan bajas y limpieza.

## 2.  Decisión

Se decidió implementar la siguiente arquitectura:

### Separación de responsabilidades:

-   AttachedFilesService: maneja la metadata, validaciones, control de acceso, baja lógica y auditoría.

-   FileStorageService: maneja el almacenamiento físico de archivos en disco local (uploadDirDocument y uploadDirImage), incluyendo creación de directorios, almacenamiento, recuperación y eliminación física.

### Nomenclatura y sanitización:

-   Los nombres de archivo se generan combinando: personaId + ApellidoNombre + nombre original sanitizado.

-   Se reemplazan caracteres no alfanuméricos por _ para evitar problemas con el filesystem.

### Validaciones:

-   Tamaño máximo y extensiones permitidas configurables desde parámetros del sistema (SystemParameterService).

### Control de acceso:

-   Usuarios solo pueden acceder a sus propios documentos.

-   Usuarios con permisos especiales (PERMISO_CONFIGURATION_UPLOAD) pueden acceder a documentos de otros.

-   Desarrollo y administración diferenciados por roles.

### Eliminación de archivos:

-   Se realiza baja lógica en la metadata (enabled=false) para permitir auditoría.

-   Una tarea programada (deleteAttachedFiles) elimina físicamente los archivos que superen un período configurable (ATTACHMENT_MIN_DAYS) y actualiza la metadata (deletedByScheduler, deletedAtScheduler).

### Auditoría y logging:

-   Todas las acciones relevantes (guardar, eliminar, descarga) registran logs y metadata de creación y eliminación (createdBy, disabledBy).

### Consideraciones de escalabilidad:

Actualmente se almacena en disco local.

Esta decisión permite desarrollo y testing sencillo, pero podría migrarse a almacenamiento en la nube (S3, Azure Blob) si se requieren múltiples instancias o alta disponibilidad.

### Alternativas consideradas

#### Almacenamiento en base de datos

-   Pros: Transacciones atómicas, backup junto a datos

-   Contras: Mayor consumo de DB, rendimiento peor para archivos grandes


#### Almacenamiento en la nube (S3, Azure)

-   Pros: Escalable, alta disponibilidad, fácil replicación

-   Contras: Coste, configuración de permisos, integración adicional



#### Eliminación física inmediata

-   Pros: Simplicidad

-   Contras: Pérdida de auditoría, riesgo de eliminar archivos por error



#### Baja lógica con limpieza programada

-   Pros: Auditoría, seguridad

-   Contras: Complejidad adicional, requiere scheduler

## 3. Consecuencias
La aplicación permite gestión segura de documentos y avatars sin comprometer la integridad de la base de datos.

El control de acceso garantiza que solo usuarios autorizados puedan descargar o eliminar archivos.

La baja lógica y limpieza programada permite recuperación de errores y evita pérdida accidental de archivos.

La solución es extensible para migrar a almacenamiento en nube si la aplicación crece.

Requiere monitorización de espacio en disco y scheduler funcionando correctamente para evitar acumulación de archivos borrados.