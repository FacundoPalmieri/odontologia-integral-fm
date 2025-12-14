# Estructura de Estilos Globales

Esta carpeta contiene los estilos globales de la aplicación organizados en módulos para mejor mantenibilidad.

## 📁 Archivos

### `_scrollbar.scss`

Estilos personalizados para el scrollbar de la aplicación.

- Scrollbar delgado (8px)
- Color primario del tema
- Soporte para WebKit y Firefox

### `_animations.scss`

Animaciones globales y keyframes.

- `fadeIn`: Aparición con fade
- `slideUp`: Deslizamiento desde abajo
- `slideDown`: Deslizamiento desde arriba
- `scaleIn`: Escalado desde 95%
- Clases: `.fade-in`, `.slide-up`, `.slide-down`, `.scale-in`, `.stagger-item`
- Animación automática para `mat-card` y `router-outlet`

### `_utilities.scss`

Clases utilitarias reutilizables.

- `.rotate-on-hover`: Rotación 360° en hover

### `_components.scss`

Estilos de componentes globales.

- `.card-header`: Header de cards con iconos
- Colores disponibles: `bg-blue`, `bg-green`, `bg-orange`, `bg-purple`, `bg-red`, `bg-gray`

### `_material-overrides.scss`

Customizaciones de Angular Material.

- Snackbars (success, error, warning, info)
- Buttons, Cards, Form Fields
- Tables, Tabs, Dialogs
- Checkboxes, Tooltips, etc.

## 🎯 Uso

Los estilos se importan automáticamente en `styles.scss`. No es necesario importarlos en componentes individuales.

### Ejemplo de uso de clases globales:

```html
<!-- Card con header -->
<div class="card-header">
  <div class="header-icon-container bg-blue">
    <i-tabler name="calendar" class="header-icon"></i-tabler>
  </div>
  <div class="header-text">
    <h3 class="card-title">Título</h3>
    <p class="card-subtitle">Subtítulo</p>
  </div>
</div>

<!-- Botón con rotación -->
<button class="rotate-on-hover">
  <i-tabler name="settings"></i-tabler>
</button>

<!-- Elementos con animación stagger -->
<div class="stagger-item">Item 1</div>
<div class="stagger-item">Item 2</div>
<div class="stagger-item">Item 3</div>
```

## 📝 Notas

- Los archivos parciales comienzan con `_` para indicar que son módulos
- El orden de importación en `styles.scss` es importante
- Las animaciones se aplican automáticamente a `mat-card` y transiciones de ruta
