package com.unlam.verabackend.domain.model;

public enum Source {
    MOBILE, // El contenido proviene de un dispositivo móvil, como un mensaje SMS o una aplicación de mensajería en el celular. Las respuestas deben ser acotadas y fáciles de leer en una pantalla pequeña.
    WEB // El contenido proviene de la web, como un correo electrónico o un documento descargado. Las respuestas pueden ser más detalladas y explicativas, ya que se asume que el usuario tiene una pantalla más grande para leer.
}