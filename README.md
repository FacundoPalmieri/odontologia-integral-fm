# Odontología Integral FM

## Descripción
Este es un proyecto de gestión interna para un **consultorio odontológico**.

## Seguridad
El proyecto implementa autenticación y autorización basada en roles utilizando credenciales, JWT y OAuth2. Permite la creación y actualización de usuarios, roles y permisos, además de ofrecer funcionalidades como el restablecimiento de contraseñas con envío de correos electrónicos.

Cuenta con **endpoints de parametrización** disponibles exclusivamente para usuarios con rol de tipo "desarrollador", lo que permite actualizar de manera ágil:
- Mensajes personalizados para usuarios.
- Logs configurables mediante pares clave-valor.
- Límites de intentos de inicio de sesión fallidos.
- Tiempo de expiración del token JWT.

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


