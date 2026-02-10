# Tablón de Avisos

Proyecto de escritorio desarrollado en **Java (Swing)** que implementa un **tablón de avisos** con persistencia en **MySQL**, siguiendo una arquitectura basada en **DAO**.

## Funcionalidad principal

La aplicación permite:

- Crear, listar y eliminar **avisos**
- Gestionar **autores**
- Gestionar **categorías**
- Asociar cada aviso a un autor y una categoría
- Cambiar el estado de un aviso (pendiente / resuelto)
- Visualizar los avisos ordenados por fecha de creación

## Tecnologías utilizadas

- Java SE
- Swing (interfaz gráfica)
- JDBC
- MySQL
- Git / GitHub

## Estructura del proyecto

```text
src/
├─ DAO/
├─ model/
└─ ui/
```

## Base de datos

La aplicación utiliza una base de datos MySQL llamada `tablon_aviso` con las tablas:

- `autor`
- `categoria`
- `aviso`

Las relaciones se gestionan mediante claves foráneas desde `aviso` hacia `autor` y `categoria`.

## Configuración

1. Crear la base de datos en MySQL.
2. Ajustar los datos de conexión en `DBConnection.java`:
   - URL
   - Usuario
   - Contraseña
3. Añadir el conector JDBC de MySQL al proyecto.
4. Ejecutar la clase `SwingMain`.

## Estado del proyecto

Proyecto funcional y operativo, orientado a práctica académica y trabajo colaborativo.
