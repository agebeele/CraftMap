# 🗺️ CraftMap: Voxel GPS & World Spawns

Convierte el mundo real en un mapa con estilo Minecraft. Conecta con tus amigos, define el **World Spawn** en coordenadas `(0, 64, 0)`, coloca pines para casas y bases, visualiza las cabezas/skins de Minecraft de tus compañeros y navega con mapas satelitales y callejeros en tiempo real.

---

## 📱 Cómo compilar e instalar la app en tu teléfono Android

Este proyecto incluye un flujo automatizado de **GitHub Actions** (`.github/workflows/build-apk.yml`) para compilar el archivo `.apk` instalable de forma automática en la nube de GitHub.

### Paso 1: Subir el proyecto a tu repositorio de GitHub
1. Crea un nuevo repositorio en tu cuenta de GitHub (ej. `CraftMap`).
2. Conecta y sube el código desde tu terminal:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of CraftMap"
   git branch -M main
   git remote add origin https://github.com/TU_USUARIO/TU_REPOSITORIO.git
   git push -u origin main
   ```

### Paso 2: Descargar el APK desde GitHub Actions
1. En tu repositorio de GitHub, haz clic en la pestaña **"Actions"** en la barra superior.
2. Verás el flujo de trabajo **"Build Android APK (CraftMap)"** ejecutándose automáticamente.
3. Una vez terminado (con un check verde ✅), entra al detalle de la ejecución.
4. En la sección **Artifacts** (al final de la página), descarga el archivo **`CraftMap-Debug-APK`**.
5. Descomprime el archivo `.zip` descargado para obtener `CraftMap.apk`.

### Paso 3: Instalar en tu teléfono móvil Android
1. Transfiere o descarga el archivo `CraftMap.apk` a tu teléfono (por WhatsApp, Telegram, Google Drive, cable USB o abriendo GitHub directamente desde el navegador de tu móvil).
2. Toca el archivo `CraftMap.apk` en tu gestor de archivos o descargas.
3. Si el sistema te lo pide, permite la opción **"Instalar aplicaciones desconocidas"** para tu navegador o gestor de archivos.
4. Toca **"Instalar"** y abre la app. ¡Listo!

---

## ✨ Características Principales

* 📐 **Conversión GPS ⟷ Bloques de Minecraft:** 1 metro real equivale a 1 bloque de Minecraft. El World Spawn acordado actúa como origen `(X: 0, Y: 64, Z: 0)`.
* 🌍 **Mapa del Mundo Real Integrado:** Capas en vivo de Satélite de alta resolución (Esri World Imagery), Calles (OpenStreetMap) y Modo Oscuro Deepslate.
* 👥 **Reinos y Amigos:** Crea servidores/reinos, comparte códigos de 6 caracteres (ej. `MC-777`) y observa la ubicación de tus compañeros con sus skins de Minecraft en vivo.
* 📍 **Pines y Waypoints:** Marca Casas 🏠, Minas ⛏️, Portales al Nether 🟣, Aldeas 🌾 y Fortalezas 🏰.
* 👤 **Personalización de Skin:** Sube cualquier imagen o archivo `.png` de skin desde tu galería de fotos o escribe tu usuario de Java/Bedrock para descargar tu cabeza oficial.
* ⚡ **Persistencia Reactiva:** Base de datos Room offline-first con soporte para sincronización en tiempo real vía Firebase Cloud Firestore.
