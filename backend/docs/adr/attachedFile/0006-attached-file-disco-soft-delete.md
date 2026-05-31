# ADR-0006 — Almacenar archivos en disco local con soft delete y scheduler

- **Fecha**: 2025-12-11
- **Estado**: aceptado
- **Scope**: feature:attachedFile
- **Supersedes**: —
- **Related**: —

## Contexto
La aplicación necesita almacenar documentos PDF e imágenes de perfil asociados a usuarios y pacientes, con control de acceso granular y trazabilidad de eliminaciones. Se requería una solución simple de implementar que permitiera auditoría y recuperación ante errores.

## Decisión
Almacenar archivos en disco local separando responsabilidades en dos servicios: AttachedFilesService maneja metadata, validaciones y control de acceso; FileStorageService maneja el almacenamiento físico. Las bajas son lógicas (enabled=false) y una tarea programada elimina físicamente los archivos que superan un período configurable.

## Alternativas consideradas
- **Almacenamiento en base de datos**: descartada por mayor consumo de DB y peor rendimiento para archivos grandes.
- **Almacenamiento en la nube (S3/Azure)**: descartada por costo y configuración adicional innecesaria para el tamaño actual del proyecto.
- **Eliminación física inmediata**: descartada por pérdida de auditoría y riesgo de eliminar archivos por error.

## Consecuencias

**Positivas**:
- ✓ Implementación simple — sin dependencias externas de almacenamiento.
- ✓ Baja lógica permite recuperación ante eliminaciones erróneas dentro del período configurable.
- ✓ Separación clara entre lógica de negocio y almacenamiento físico.

**Negativas / tradeoffs**:
- ✗ No escala a múltiples instancias — disco local es exclusivo de cada servidor.
- ✗ Requiere monitoreo de espacio en disco y scheduler activo para evitar acumulación de archivos borrados.
