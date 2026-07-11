# 🛡️ Proyecto VERA - Guía de Despliegue

Este documento contiene el resumen de los pasos necesarios para implementar y desplegar tanto el backend como el frontend del **Proyecto VERA** (interfaz web en React + TypeScript con Vite y backend en Spring Boot + Java 25).

---

## 🚀 Despliegue del Backend

El backend se distribuye mediante contenedores Docker, utiliza GitHub Container Registry (GHCR) como almacenamiento e integra cambios automáticamente en Render a través de un Webhook.

### Paso 1: Obtención de Claves e Interfaces de IA/Seguridad
Antes de configurar los servidores, es necesario generar las credenciales de los servicios externos que utiliza VERA para proteger a los usuarios:
1. **Google AI Studio:** Ingresa a la plataforma y crea una API Key para el modelo Gemini.
2. **Google Cloud Console:** Crea un proyecto, activa la API de **Safe Browsing** y genera una clave de API correspondiente.
3. **Neon.tech:** Crea un proyecto PostgreSQL en la nube y copia los datos de tu *Connection String* (Host, Base de datos, Usuario y Contraseña).

### Paso 2: Automatización de la Imagen (GitHub Actions & GHCR)
El proceso de integración continua ya está configurado en el repositorio (`main.yml` y `Dockerfile`):
1. Cada `push` a la rama `main` activa el pipeline que corre los **Tests Unitarios** con JDK 25.
2. Si los tests son exitosos, se compila el archivo `.jar` mediante un empaquetado multi-etapa optimizado.
3. El flujo genera la imagen Docker en minúsculas y la publica automáticamente en **GitHub Container Registry (GHCR)** con la etiqueta `:latest`.

### Paso 3: Alojamiento y Configuración en Render
1. En [Render.com](https://render.com/), crea un nuevo **Web Service** apuntando a tu imagen externa de GHCR (`ghcr.io/...`).
2. En la pestaña **Environment**, debes configurar obligatoriamente las siguientes variables de entorno:

   | Variable                       | Descripción / Valor                                           |
      |:-------------------------------|:--------------------------------------------------------------|
   | `PORT`                         | `8080` *(Puerto requerido al ser una aplicación Spring Boot)* |
   | `APP_FRONTEND_URL`             | URL de producción generada por Vercel (Paso del Frontend)     |
   | `GOOGLE_GEMINI_API_KEY`        | Clave API obtenida en Google AI Studio                        |
   | `GOOGLE_SAFE_BROWSING_API_KEY` | Clave API obtenida en Google Cloud                            |
   | `DB_URL`                       | URL de conexión provista por Neon                             |
   | `DB_USERNAME`                  | Usuario de la base de datos de Neon                           |
   | `DB_PASSWORD`                  | Contraseña de la base de datos de Neon                        |

3. En la configuración avanzada de Render, copia la URL del **Deploy Hook**.
4. Ve a tu repositorio de GitHub (`Settings` > `Webhooks`), añade un nuevo webhook pegando esa URL (*Payload URL*), selecciona el formato `application/json` y actívalo para eventos de `push`.

---

## 💻 Despliegue del Frontend

El frontend consiste en la interfaz de usuario tanto del tipo CARER como PROTECTED construida en **React + TypeScript con Vite**. Su despliegue es gestionado de manera nativa por Vercel.

### Pasos para la Implementación:

1. **Importar Proyecto:** Inicia sesión en [Vercel.com](https://vercel.com/) e importa el repositorio del frontend.
2. **Configuración del Framework:** Vercel detectará automáticamente el entorno de Vite. Deja los comandos de compilación (`npm run build`) y la carpeta de salida (`dist`) por defecto.
3. **Variables de Entorno:** En la sección **Environment Variables**, añade la URL de producción de tu API de Render para que la interfaz pueda consumir el backend:
   ```env
   VITE_API_URL="[https://tu-backend-en-render.onrender.com](https://tu-backend-en-render.onrender.com)"
