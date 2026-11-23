# Web de MovilSimple

## Estructura propuesta

| Dirección | Qué es | Dónde se aloja |
|---|---|---|
| `movilsimple.com` | Web de presentación, precios, guías, legal | Cloudflare (como hoy) |
| `movilsimple.com/descargar/` | Guía de instalación y código QR de aprovisionamiento | Cloudflare (como hoy) |
| `descargas.movilsimple.com` | El APK firmado (URL fija que usa el QR, con su SHA-256) | Cloudflare R2 |
| `app.movilsimple.com` | Panel de familias (suscripción) | Cloudflare Pages, datos en Firebase región UE |

La web actual ya está en Cloudflare: páginas estáticas más dos funciones (`/api/newsletter` y `/api/contact`).
Se mantiene todo eso; aquí solo se añaden páginas.

## Carpetas

- `sitio/` — archivos para `movilsimple.com`.
  - `index.html` — portada actual con: menú con Precios, Descargar y Acceso familias; botón «Descargar gratis»;
    sección «Cómo se empieza»; sección «Precios» (Gratis / MovilSimple Familia); llamadas de desconocidos y sin SMS.
  - `descargar/index.html` — guía de instalación con el hueco para el QR.
  - `assets/sitio.css` — los estilos de la portada en un archivo común (la portada publicada los lleva dentro del HTML).
    Corrige además el desbordamiento lateral en móvil que tiene la portada actual.
  - Faltan por copiar del sitio publicado: `assets/app.js`, imágenes, guías, legal y las funciones `/api/*`.
- `panel/` — página provisional de `app.movilsimple.com` («Próximamente»).

## Ver en local

```
cd web/sitio && python3 -m http.server 8000
```

## Pendiente de decidir con Carlos

- Precios (Carlos, 23 nov): primer móvil 9 €/mes o 79 €/año; cada móvil más 49 €/año. Falta el precio mensual del móvil adicional (propuesta: 5 €/mes).
- Acceso a Cloudflare para publicar (o el repositorio desde el que se publica hoy la web).
