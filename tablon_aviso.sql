DROP DATABASE IF EXISTS tablon_aviso;
CREATE DATABASE IF NOT EXISTS tablon_aviso;
USE tablon_aviso;


CREATE TABLE Autor (
    ID_autor INT  PRIMARY KEY,
    nombre VARCHAR (60) NOT NULL
 );
 
 
 CREATE TABLE Aviso (
    ID_aviso INT  PRIMARY KEY,
    descripcion VARCHAR(128) NOT NULL,
    fecha_creacion DATE,
    estado VARCHAR(60) NOT NULL,
    autor_asociado VARCHAR(60) NOT NULL,
    categoria_asociado VARCHAR(60) NOT NULL 
);
 
 
 CREATE TABLE Categoria (
    ID_categoria INT PRIMARY KEY,
    nombre VARCHAR (60) NOT NULL
 )