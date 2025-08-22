# Odontología Integral FM

## Descripción
Este es un proyecto de gestión interna para un **consultorio odontológico**.

## Seguridad
El proyecto implementa autenticación y autorización basada en roles. permisos y acciones utilizando credenciales, JWT y OAuth2. Permite la creación y actualización de usuarios, roles y permisos, además de ofrecer funcionalidades como el restablecimiento de contraseñas con envío de correos electrónicos.

Cuenta con **endpoints de parametrización** disponibles exclusivamente para usuarios con rol de tipo "desarrollador", lo que permite actualizar de manera ágil:
- Mensajes personalizados para usuarios.
- Logs configurables mediante pares clave-valor.
- Límites de intentos de inicio de sesión fallidos.
- Tiempo de expiración del token JWT.
- Tiempo de expiración de refresh Token.


## Logs

La aplicación cuenta con tipos y niveles de logs.

## Tipos:

- EXCEPTION
- SCHEDULED
- SECURITY
- SYSTEM

## Niveles:
- NONE
- INFO
- WARN
- ERROR

## Loguea:
- Inicio sesión.
- Cierre sesión.
- Creación de Rol.
- Actualización de Rol.
- Creación Usuario.
- Actualización de Usuario.
- Eliminación lógica de documento de Usuarios y pacientes.
- Envío de emails.
- Todos los tipos de exception que arroja la aplicación (Contraseña incorrecta, bloqueo de cuenta, etc)


## Auditoría

Este proyecto utiliza **Hibernate Envers** para auditar automáticamente los cambios realizados en las entidades anotadas con `@Audited`.

### ¿Cómo funciona?

Cuando una entidad auditada cambia, Hibernate genera un registro en una tabla con sufijo `_AUD`, que conserva el estado del objeto en ese momento.

### Estructura típica de una tabla `_AUD`

Cada tabla de auditoría contiene:

- `id`: Identificador del objeto (igual que la entidad original).
- `REV`: ID de la revisión (clave foránea a `revinfo.REV`).
- `REVTYPE`: Tipo de operación realizada:
   - `0` = INSERT
   - `1` = UPDATE
   - `2` = DELETE
- `...otros campos...`: Una copia de los atributos auditados de la entidad original.

## Tareas Programadas


## Gestión
El objetivo de este proyecto es abarcar la gestión interna del consultorio, tomando como referencia los siguientes aspectos:
- Historia clínica de los pacientes.
- Gestión de turnos.
- Registro de cobros.
- Control de stock e insumos.
- Registro de consultas, con odontograma interactivo.

### Características principales:
- **Autenticación y autorización** basadas en roles.
- **Creación y actualización** de usuarios, roles y permisos.
- **Restablecimiento de contraseñas** con envío de correo electrónico.
- **Mensajes personalizables** a usuarios y logs configurables.
- **Control de intentos de inicio de sesión fallidos** (solo para usuarios con rol de tipo "desarrollador").
- **Tiempo de expiración del Token JWT** (solo para usuarios con rol de tipo "desarrollador").
- **Configuración flexible** mediante variables de entorno definidas en el archivo `application.properties`, permitiendo personalizar parámetros como límites de intentos, mensajes y más.
- **Documentación**: Todo el proyecto está documentado usando **Javadoc** y los endpoints están descritos en **Swagger**.

## Tecnologías utilizadas

- **Java 17**
- **Spring Boot 3.2.5**
- **JWT (JSON Web Tokens)**
- **OAuth2**
- **MySQL**


## Instalación

Para instalar y ejecutar el proyecto, sigue estos pasos:

1. Clona el repositorio:
   ```bash
   git clone <URL del repositorio>
   cd spring-security-jwt-oauth2

2. Verificar tener Java 17 instalado en tu sistema.

3. Configura las variables de entorno necesarias, que pueden definirse en el archivo application.properties.

4. Para compilar y ejecutar el proyecto, usa Maven:
   mvn clean install
   mvn spring-boot:run
