# INDEX — Material para TP5, Problema 2 (evacuación híbrida EDMD + TSDMD)

Índice para buscar rápido en los 9 archivos Markdown y el `.tex` del TP4. Cada entrada da archivo y rango de líneas (`Lx–y`, numeración desde 1; sirve para `sed -n 'x,yp' archivo` o para la herramienta Read con offset/limit).

## Por dónde empezar

Todas las rutas de este índice son relativas a `sources/`. Elegí la carpeta según lo que necesites hacer:

| Necesito… | Carpeta | Qué mirar |
|---|---|---|
| Saber qué pide el TP: parámetros, ítems a–d, entregables, fecha | `enunciado/` | `enunciado/TP5_Enunciado_Problema2.md`. **Máxima prioridad** sobre todo lo demás, junto con `enunciado/TP5_Consideraciones.md` (cambios de la cátedra) |
| Física, fórmulas, modelos, observables, estadística, formato de archivos, vecinos | `teoria/` | Según el tema, abajo |
| Separar simulación / análisis / animación, observables, estadística, ajustes | `teoria/` | `teoria/Teorica_0_Introduccion_Simulacion.md` |
| Cell Index Method, formato de archivos estático y dinámico, reglas de presentación | `teoria/` | `teoria/Teorica_1_Reglas_de_Simulacion.md` |
| Contacto, superposición, versores, fuerzas, Δt y Δt₂ | `teoria/` | `teoria/Teorica_5_Medios_Granulares.md` |
| SFM, CPM, caudal específico, evacuación, desired target | `teoria/` | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` |
| Observables típicos (curvas de población, tiempos de evacuación) | `teoria/` | `teoria/Teorica_7_Simulacion_de_Multitudes_II.md` |
| Armar la presentación o un gráfico | `presentacion/` | `presentacion/Guia_Presentaciones.md` (reglas) y `presentacion/TP4_Presentacion.tex` (template; ver el mapa de diapositivas en la sección 1b) |
| Decidir algo de diseño, de código o de figuras | `correcciones/` | Leé primero `correcciones/Correcciones_TP3_EventDriven.md` y `correcciones/Correcciones_TP4_TimeStep.md`: son errores que no podemos repetir |

Material nuevo (por ejemplo la teórica de EDMD o código del TP3/TP4 para citar): va en la carpeta que corresponda y se agrega a este índice con el mismo formato.

## Archivos

| Archivo | Contenido | Líneas |
|---|---|---|
| `enunciado/TP5_Enunciado_Problema2.md` | Enunciado literal: parte general + Problema 2. Problema 1 omitido. **Máxima prioridad** sobre todo lo demás. | 73 |
| `enunciado/TP5_Consideraciones.md` | Mensaje de la cátedra del 2026-11-08 que modifica el enunciado: colisión y recuperación de v_d en la Introducción, N = 200 opcional, atascos válidos, sin fricción tangencial opcional. **Misma prioridad que el enunciado.** | 24 |
| `presentacion/Guia_Presentaciones.md` | Guía de la cátedra para presentaciones (texto completo): secciones obligatorias, reglas de figuras, unidades, notación, animaciones, conclusiones. | 96 |
| `teoria/Teorica_0_Introduccion_Simulacion.md` | Clase 0 (filtrada): simulación / análisis / animación separados, output primario → observable primario → escalar, determinista vs estocástico, tiempo discreto vs eventos, estadística (PDF, μ ± σ, cifras significativas), regresiones. | 245 |
| `teoria/Teorica_1_Reglas_de_Simulacion.md` | Clase 1 (filtrada): Cell Index Method, reglas generales de simulaciones, formato de archivos estático y dinámico, entregables, formato y consejos de presentaciones. | 247 |
| `teoria/Teorica_5_Medios_Granulares.md` | Clase 5 (filtrada): contacto entre discos y con paredes, superposición, versores, fuerzas normal/tangencial, suma de fuerzas, Δt y Δt₂, Beverloo. | 212 |
| `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` | Clase 6 (filtrada): evacuación y "Faster is Slower", caudal específico, SFM sin fuerza social, CPM, desired target. | 250 |
| `teoria/Teorica_7_Simulacion_de_Multitudes_II.md` | Clase 7 (filtrada): solo observables típicos (curvas de población, tiempos de evacuación). | 23 |
| `correcciones/Correcciones_TP3_EventDriven.md` | Devolución del TP3 (Event Driven, nota 6): estructura de la presentación, figuras, nombres, animaciones, conclusiones y checklist para el TP5. | 116 |
| `correcciones/Correcciones_TP4_TimeStep.md` | Devolución del TP4 (Time Step Driven, nota 5): código (observables fuera del motor, estado en el archivo dinámico, integrador), diapositivas y checklist para el TP5. | 58 |
| `presentacion/TP4_Presentacion.tex` | Código Beamer de la presentación que entregamos en el TP4. **Template** para la presentación del TP5 (preámbulo, macros, estructura). Su contenido tiene errores que la devolución del TP4 corrigió: ver el mapa de diapositivas abajo. | 941 |

Convenciones:
- En las teóricas cada diapositiva es una sección `## Diapo N — Título` con su **numeración original** (por eso hay saltos). En la Clase 0, N es la página del PDF y las diapos armadas en varias páginas se agrupan (`## Diapo 7–10`). Para saltar a una: `grep -n "## Diapo 15 " teoria/Teorica_5_Medios_Granulares.md`.
- Fórmulas en LaTeX (`$...$`, `$$...$$`). Figuras descriptas como `[Figura: …]`.
- `> Nota TP5:` = aclaración agregada para el TP5; **no** está en el material original.
- Prioridad (Guía 1.4): enunciado > guía de presentaciones > teóricas.
- Las devoluciones de TP3 y TP4 (`correcciones/Correcciones_*.md`) son errores que la cátedra ya marcó y penaliza si se repiten (Guía 3.3). Revisarlas antes de entregar.

**No está en estos archivos**: el algoritmo de EDMD (tiempo de choque, cola de eventos, regla de choque). La Clase 0 solo define simulaciones basadas en eventos. Hay que tomarlo de la clase correspondiente o de conocimiento general.

## 1. Por tema

### Enunciado del Problema 2

| Tema | Dónde |
|---|---|
| Consideraciones de la cátedra: Introducción de EDMD (colisión y recuperación de v_d) | `enunciado/TP5_Consideraciones.md` L17 |
| N = 200 opcional en lugar de 500 (ítems a–c) | `enunciado/TP5_Consideraciones.md` L19 |
| Atascos: válidos en EDMD; en TSDMD con SFM se puede omitir la fricción tangencial | `enunciado/TP5_Consideraciones.md` L21–24 |
| Reglas generales: dt fijo, dt₂ de output, en CPM el dt es del modelo, output en texto, animación independiente, qué mostrar en cada estudio | `enunciado/TP5_Enunciado_Problema2.md` L8–35 |
| Entregables (presentación, PDF con links, zip solo con el motor) | `enunciado/TP5_Enunciado_Problema2.md` L20–26 |
| Fecha (16/11/2026 13 hs) y nombres de archivo | `enunciado/TP5_Enunciado_Problema2.md` L28–35 |
| Sistema y parámetros: N=500, L=50 m, puerta d=0.8 m, vd=1.5 m/s, paredes rígidas, retiro a 1 m (segundo target), radios | `enunciado/TP5_Enunciado_Problema2.md` L43 |
| Figura 1 (zona diluida → EDMD, bottleneck → TSDMD) | `enunciado/TP5_Enunciado_Problema2.md` L45 |
| Qué describir del híbrido: criterios de pasaje, convivencia/sincronización, colisiones y recuperación de vd | `enunciado/TP5_Enunciado_Problema2.md` L51 y las 3 viñetas siguientes |
| Ítem a) tres esquemas, animaciones coloreadas, fracción por esquema | `enunciado/TP5_Enunciado_Problema2.md` L59 |
| Ítem b) curvas de población, t_evac, ≥10 realizaciones, TSDMD de referencia | `enunciado/TP5_Enunciado_Problema2.md` L61 |
| Ítem c) wall-clock, barrido del parámetro de pasaje | `enunciado/TP5_Enunciado_Problema2.md` L63 |
| Ítem d) escalamiento con N (densidad constante / L constante) | `enunciado/TP5_Enunciado_Problema2.md` L65 |
| Referencias Helbing (2000), Baglietto & Parisi (2011) | `enunciado/TP5_Enunciado_Problema2.md` L69–73 |

### Arquitectura del código: simulación, análisis y animación

| Tema | Dónde |
|---|---|
| Simulación off-line: la simulación solo produce el estado vs tiempo; análisis y animación son herramientas aparte | `teoria/Teorica_0_Introduccion_Simulacion.md` L55–64; `teoria/Teorica_1_Reglas_de_Simulacion.md` L107–112 |
| La animación es un postproceso separado; el simulador escribe posiciones y velocidades | `teoria/Teorica_1_Reglas_de_Simulacion.md` L116–120 |
| Visualizadores recomendados (Matlab/Octave, Matplotlib, Ovito) | `teoria/Teorica_1_Reglas_de_Simulacion.md` L124–129 |
| Observables + videos = resultados | `teoria/Teorica_1_Reglas_de_Simulacion.md` L133–135 |
| Lenguajes: simulación en Java/C++; análisis en Python/Matlab/R; no usar Excel | `teoria/Teorica_0_Introduccion_Simulacion.md` L162–166 |
| Estado del sistema: lo que hay que guardar para reconstruir todo | `teoria/Teorica_0_Introduccion_Simulacion.md` L106–114 |
| Cadena Sistema real → modelo matemático → implementación → simulación | `teoria/Teorica_0_Introduccion_Simulacion.md` L39–45 |

### Formato de archivos de output

| Tema | Dónde |
|---|---|
| Info dinámica: por cada tiempo, una fila por partícula (x y vx vy) | `teoria/Teorica_1_Reglas_de_Simulacion.md` L139–158 |
| Info estática: N, L, radio y color por partícula | `teoria/Teorica_1_Reglas_de_Simulacion.md` L162–175 |
| Lista de vecinos (formato de ejemplo) | `teoria/Teorica_1_Reglas_de_Simulacion.md` L88–97 |
| Δt vs Δt₂ de guardado; lista de tiempos de salida con precisión Δt | `teoria/Teorica_5_Medios_Granulares.md` L184–212 |

### Observables, estadística y ajustes

| Tema | Dónde |
|---|---|
| Output primario → observable primario → observable escalar | `teoria/Teorica_0_Introduccion_Simulacion.md` L76–82 |
| Ejemplo curva de descarga y caudal Q = ΔN/Δt | `teoria/Teorica_0_Introduccion_Simulacion.md` L86–96 |
| Gráfico input vs observable con barras de error (IMPORTANTE) | `teoria/Teorica_0_Introduccion_Simulacion.md` L68–72; `teoria/Teorica_0_Introduccion_Simulacion.md` L100–102; `teoria/Teorica_0_Introduccion_Simulacion.md` L200–202 |
| Determinista vs estocástico; promediar realizaciones | `teoria/Teorica_0_Introduccion_Simulacion.md` L118–130 |
| Histograma, distribución de probabilidad y PDF | `teoria/Teorica_0_Introduccion_Simulacion.md` L170–178 |
| Error: μ ± σ sobre realizaciones con distintas semillas | `teoria/Teorica_0_Introduccion_Simulacion.md` L182–186 |
| Cifras significativas (L = 45.4 ± 0.3 cm) | `teoria/Teorica_0_Introduccion_Simulacion.md` L190–196 |
| Regresiones: solo modelos teóricos; E(c) = Σ[yᵢ − f(xᵢ,c)]² y su mínimo | `teoria/Teorica_0_Introduccion_Simulacion.md` L206–245 |
| Observables típicos de dinámica peatonal (curvas de población, tiempos de evacuación) | `teoria/Teorica_7_Simulacion_de_Multitudes_II.md` L13–23 |
| Caudal específico: 1.33 p/m/s reglamentario, 1.25–2.0 experimental | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L30–33; `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L37–57 |
| "Faster is Slower" y "delays" | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L22–26; `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L123–125 |
| Caudal constante (Beverloo) → tramo recto de N(t) | `teoria/Teorica_5_Medios_Granulares.md` L22–34 |

### Tipos de simulación

| Tema | Dónde |
|---|---|
| Basadas en tiempo discreto (dt fijo) → TSDMD | `teoria/Teorica_0_Introduccion_Simulacion.md` L134–139 |
| Basadas en eventos (instantáneos) → EDMD | `teoria/Teorica_0_Introduccion_Simulacion.md` L143–147 |
| Simulación vs animación | `teoria/Teorica_0_Introduccion_Simulacion.md` L151–158 |

### Vecinos: Cell Index Method

| Tema | Dónde |
|---|---|
| Interacciones de corto alcance; fuerza bruta N² vs CIM lineal en N | `teoria/Teorica_1_Reglas_de_Simulacion.md` L11–26 |
| Idea general, tamaño de celda L/M > r_c | `teoria/Teorica_1_Reglas_de_Simulacion.md` L30–60 |
| Asignación de celdas, simetría (mirar 4 vecinas), contorno periódico (no aplica) | `teoria/Teorica_1_Reglas_de_Simulacion.md` L64–84 |

### Contacto y fuerzas (TSDMD)

| Tema | Dónde |
|---|---|
| Medio granular: contacto normal y tangencial, disipación | `teoria/Teorica_5_Medios_Granulares.md` L13–18 |
| t_choque ≫ t_vuelo en sistemas densos; elemento discreto | `teoria/Teorica_5_Medios_Granulares.md` L38–42 |
| Superposición ξ partícula–partícula y velocidad relativa | `teoria/Teorica_5_Medios_Granulares.md` L46–56 |
| Superposición partícula–pared | `teoria/Teorica_5_Medios_Granulares.md` L60–67 |
| Ecuaciones de movimiento (en el TP sin rotación ni gravedad) | `teoria/Teorica_5_Medios_Granulares.md` L71–83 |
| Versores ê^n, ê^t partícula–partícula y con paredes | `teoria/Teorica_5_Medios_Granulares.md` L87–113 |
| Opciones de fuerza N.1, N.2, T.1, T.2, T.3 (SFM = N.2 + T.3) | `teoria/Teorica_5_Medios_Granulares.md` L117–141 |
| Suma, proyección a x, y y fuerza total | `teoria/Teorica_5_Medios_Granulares.md` L145–170 |
| Criterio Δt ≈ 0.1·sqrt(m/kn) | `teoria/Teorica_5_Medios_Granulares.md` L174–180 |

### Social Force Model

| Tema | Dónde |
|---|---|
| Ecuación de Newton (social = 0 en el TP) | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L61–72 |
| Fuerza de contacto granular (kn, kt, ε, g(ε)) | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L76–88 |
| Fuerza de deseo m(vd·e_target − v)/τ | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L92–96 |
| Parámetros (kn=1.2e5, kt=2.4e5, τ=0.5 s) y evacuación de Helbing | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L100–119 |

### Contractile Particle Model

| Tema | Dónde |
|---|---|
| Radio variable y vd(r); esquemas de contacto | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L129–166 |
| Evolución sin contacto (vd(r) con β, crecimiento de r) y e_target | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L170–188 |
| Evolución con contacto (r = rmin, |ve| = vdmax) | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L192–202 |
| Evacuación con CPM y algoritmo por paso (paper) | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L206–228 |
| Bondades: ~50× más rápido que SFM | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L232–235 |
| Desired target único / lista / dinámico | `teoria/Teorica_6_Simulacion_de_Multitudes_I.md` L239–250 |

### Presentación y figuras

| Tema | Dónde |
|---|---|
| Enunciado > guía > teóricas; responder todos los puntos | `presentacion/Guia_Presentaciones.md` L17 |
| Reglas de figuras: sin título ni caption, parámetros al costado, ejes en palabras con unidades MKS, fuente ≥ 20 | `presentacion/Guia_Presentaciones.md` L23–25 |
| Notación científica con supraíndice (no 1E2 ni 10^2) | `presentacion/Guia_Presentaciones.md` L27 |
| Cifras significativas en tablas | `presentacion/Guia_Presentaciones.md` L29 |
| Sin bibliografía; cita abreviada en la diapo | `presentacion/Guia_Presentaciones.md` L31 |
| Títulos coherentes, Beamer, separadores sin numerar | `presentacion/Guia_Presentaciones.md` L33–35 |
| Secciones obligatorias: Introducción (≤3 diapos), Implementación (solo el motor), Simulaciones (observables definidos matemáticamente, nro. de repeticiones) | `presentacion/Guia_Presentaciones.md` L39–49 |
| Resultados: animación → observable vs tiempo → input vs observable con barras de error | `presentacion/Guia_Presentaciones.md` L51–56 |
| Ajustes, puntos con símbolo, sin splines, escala log | `presentacion/Guia_Presentaciones.md` L57–59 |
| Animaciones embebidas en vivo; en el PDF fotograma + link | `presentacion/Guia_Presentaciones.md` L60 |
| Conclusiones solo de lo mostrado; cierre sin "preguntas?" | `presentacion/Guia_Presentaciones.md` L62–64 |
| Reparto entre expositores, corrección previa, notación de vectores/escalares/unidades | `presentacion/Guia_Presentaciones.md` L68–82 |
| Checklist rápido de figuras (resumen agregado) | `presentacion/Guia_Presentaciones.md` L86–96 |
| Tiempos y partes de la presentación (Clase 1) | `teoria/Teorica_1_Reglas_de_Simulacion.md` L193–205 |
| Formato y consejos (numerar, separadores, poco texto, audiencia, preguntas, varios expositores) | `teoria/Teorica_1_Reglas_de_Simulacion.md` L209–247 |
| Entregables: PDF con imagen + link; no entregar animaciones | `teoria/Teorica_1_Reglas_de_Simulacion.md` L179–183 |
| Dictado: grupos, entrega en campus, código original, uso responsable de IA | `teoria/Teorica_0_Introduccion_Simulacion.md` L11–35 |

### Template de la presentación (presentacion/TP4_Presentacion.tex)

| Tema | Dónde |
|---|---|
| Preámbulo completo del template (tema, pie, paquetes, macros) | `presentacion/TP4_Presentacion.tex` L7–167 |
| Versión en vivo vs entrega (`\vivotrue`/`\vivofalse`, animaciones con `animate`) | `presentacion/TP4_Presentacion.tex` L9–15 |
| Tema Berlin, títulos centrados, pie solo con el número de diapositiva | `presentacion/TP4_Presentacion.tex` L17–35 |
| Notación: `\vect`, `\un`, `\sci` (guía 3.5 y 1.9) | `presentacion/TP4_Presentacion.tex` L69–78 |
| `\pend{}` para valores pendientes (buscar antes de entregar) | `presentacion/TP4_Presentacion.tex` L80–83 |
| `\figres` (figura con placeholder si falta el archivo) y `\fotograma` | `presentacion/TP4_Presentacion.tex` L104–127 |
| `\figanim` (fotograma + link en la entrega, animación en vivo; máx. 2 por diapo) | `presentacion/TP4_Presentacion.tex` L129–139 |
| `\panel{}` de parámetros al costado (M, nunca la semilla) | `presentacion/TP4_Presentacion.tex` L141–154 |
| Separadores de sección automáticos (guía 1.13) | `presentacion/TP4_Presentacion.tex` L156–162 |
| Ejemplo de esquema del sistema con TikZ + tabla de parámetros fijos | `presentacion/TP4_Presentacion.tex` L316–379 |
| Ejemplo de definición de observables y promedios (σ con M − 1) | `presentacion/TP4_Presentacion.tex` L442–462 |
| Ejemplo de ajuste mostrando E(c) (bien según la devolución) | `presentacion/TP4_Presentacion.tex` L698–714 |
| Ejemplo de animaciones con dos valores extremos + panel | `presentacion/TP4_Presentacion.tex` L756–779 |
| Diapositiva de conclusiones y cierre | `presentacion/TP4_Presentacion.tex` L887–918 |
| Disclaimer de uso de IA | `presentacion/TP4_Presentacion.tex` L921–939 |

### Errores a evitar (devoluciones TP3 y TP4)

| Tema | Dónde |
|---|---|
| Checklist TP3 aplicado al TP5 (presentación y figuras) | `correcciones/Correcciones_TP3_EventDriven.md` L102–116 |
| Checklist TP4 aplicado al TP5 (código del motor y presentación) | `correcciones/Correcciones_TP4_TimeStep.md` L41–58 |
| Introducción solo con teoría general, sin datos del sistema particular (IMPORTANTE) | `correcciones/Correcciones_TP3_EventDriven.md` L21–23; `correcciones/Correcciones_TP3_EventDriven.md` L25–27 |
| Orden obligatorio por estudio: animación → evolución temporal → input vs observable (IMPORTANTE) | `correcciones/Correcciones_TP3_EventDriven.md` L37–47 |
| No nombres arbitrarios (A1, A2); rotular por el valor del parámetro | `correcciones/Correcciones_TP3_EventDriven.md` L53–57; `correcciones/Correcciones_TP3_EventDriven.md` L59–63 |
| Leyendas: sin "promedio ± desvío", sin valores sobre los puntos; nro. de realizaciones al costado | `correcciones/Correcciones_TP3_EventDriven.md` L59–63 |
| Animaciones: embebidas en vivo, máx. 2 por diapo, con propósito claro; no mencionar semillas | `correcciones/Correcciones_TP3_EventDriven.md` L77–81; `correcciones/Correcciones_TP3_EventDriven.md` L83–87 |
| Ajuste lineal mostrando la minimización del error (Teórica 0) | `correcciones/Correcciones_TP3_EventDriven.md` L89–92 |
| Pie de diapo sin nombres ni nro. de grupo; fuentes legibles | `correcciones/Correcciones_TP3_EventDriven.md` L11–15 |
| Conclusiones basadas en datos mostrados | `correcciones/Correcciones_TP3_EventDriven.md` L96–98 |
| Observables fuera del motor (ni código sin usar ni en el UML) (IMPORTANTE) | `correcciones/Correcciones_TP4_TimeStep.md` L16; `correcciones/Correcciones_TP4_TimeStep.md` L27 |
| El archivo dinámico debe incluir el estado de cada partícula en cada Δt₂ (IMPORTANTE) | `correcciones/Correcciones_TP4_TimeStep.md` L17; `correcciones/Correcciones_TP4_TimeStep.md` L28 |
| Integrador: aclarar cuál se usó; Verlet vs Velocity Verlet; pseudocódigo coherente | `correcciones/Correcciones_TP4_TimeStep.md` L15; `correcciones/Correcciones_TP4_TimeStep.md` L18 |
| Billar: fuerzas con paredes (partícula imagen) y Cell Index Method OK (reutilizable) | `correcciones/Correcciones_TP4_TimeStep.md` L19 |
| Definir los observables antes de usarlos; ECM relativo; no repetir letras | `correcciones/Correcciones_TP4_TimeStep.md` L30; `correcciones/Correcciones_TP4_TimeStep.md` L31 |
| Criterio para elegir Δt y marcarlo en la figura | `correcciones/Correcciones_TP4_TimeStep.md` L32 |
| Notación consistente; misma escala en paneles; decir cómo se obtiene la incerteza | `correcciones/Correcciones_TP4_TimeStep.md` L33; `correcciones/Correcciones_TP4_TimeStep.md` L34; `correcciones/Correcciones_TP4_TimeStep.md` L35 |
| Sin tabla de estudios; pocas realizaciones = poca estadística | `correcciones/Correcciones_TP4_TimeStep.md` L29 |
| Tiempo final no puede acotar el observable (realizaciones que no terminan) (IMPORTANTE) | `correcciones/Correcciones_TP4_TimeStep.md` L37 |

## 1b. Mapa de diapositivas del TP4 (`presentacion/TP4_Presentacion.tex`)

Número de diapositiva tal como lo cuenta la devolución del TP4 (incluye carátula y separadores de sección). Las líneas en negrita marcan lo que **no** hay que copiar.

| D | Líneas del `.tex` | Diapositiva | Devolución (`correcciones/Correcciones_TP4_TimeStep.md`) |
|---|---|---|---|
| 1 | L171–189 | Carátula |  |
| 2 | L194–215 | Comparación de métodos integradores (Sistema 1) | Falta la definición del ECM [Correcciones_TP4, D2] |
| 3 | L217 | Separador: Introducción |  |
| 4 | L223–237 | Sistema Real |  |
| 5 | L239–256 | Fundamentos |  |
| 6 | L259 | Separador: Implementación |  |
| 7 | L263–272 | Arquitectura del motor (UML) | **El UML incluía "Energía": sacar observables del UML** [D7] |
| 8 | L274–303 | Algoritmo implementado | **Pseudocódigo de Velocity Verlet cuando se pedía Verlet; "estado" no estaba en el archivo dinámico** [D8] |
| 9 | L306 | Separador: Simulaciones |  |
| 10 | L316–379 | Esquema y parámetros fijos | Los rangos ya estaban acá [D11] |
| 11 | L381–412 | Parámetros variables (tabla de estudios) | **La tabla de estudios no va; M = 3 es poca estadística** [D11] |
| 12 | L419–438 | Observables: energía y ECM | **Falta definir F_u; ECM absoluto (usar relativo); E usada para dos cosas** [D12, D12/D14] |
| 13 | L442–462 | Observables: tiempos característicos y promedios |  |
| 14 | L466–485 | Observables: distribución de velocidades | E usada también para el error del ajuste [D12/D14] |
| 15 | L488 | Separador: Resultados |  |
| 16 | L504–521 | Elección de Δt: energía vs tiempo | E usada para la energía [D12/D14] |
| 17 | L523–541 | Elección de Δt: ⟨ECM⟩ vs Δt | **Falta el criterio para elegir Δt y marcar el umbral** [D17] |
| 18 | L546–563 | Tiempo de ejecución vs N |  |
| 19 | L568–591 | Posición de los obstáculos: animaciones | Notación "x₀ = R/2" inconsistente con D20–D21 [D19/D20/D21] |
| 20 | L593–611 | Evolución de ⟨F_u(t)⟩ | Notación "2,55×10⁻¹ m" inconsistente [D19/D20/D21] |
| 21 | L615–633 | ⟨t₉₀⟩ vs x₀ | Ejes en ×10⁻¹ m, inconsistente [D19/D20/D21] |
| 22 | L658–676 | Distribución de velocidades: animación |  |
| 23 | L678–694 | Evolución de f(v,t) | **Paneles con escalas verticales distintas** [D23] |
| 24 | L698–714 | Ajuste: error vs kT | Bien el ajuste con el método de la Teórica 0; falta decir cómo se obtiene la incerteza [D24/D26] |
| 25 | L716–732 | Búsqueda del estacionario | Bien el criterio explícito [D25] |
| 26 | L734–750 | Distribución estacionaria y ajuste | Falta cómo se obtiene la incerteza [D24/D26] |
| 27 | L756–779 | Efecto de la densidad: animaciones |  |
| 28 | L783–800 | Evolución de ⟨F_u(t)⟩ |  |
| 29 | L802–819 | ⟨F_u(t_f)⟩ vs densidad |  |
| 30 | L821–838 | ⟨t₉₀⟩ y ⟨t₁₀₀⟩ vs densidad | **Valores acotados por t_f; realizaciones que no terminan sin explicar; marcador vacío sin leyenda** [D30] |
| 31 | L843–860 | ⟨t₉₀⟩ y ⟨t₁₀₀⟩ vs densidad, t_f = 10² s |  |
| 32 | L866–884 | Mapa de calor ⟨t₉₀⟩(x₀, N) | M = 3 realizaciones, poca estadística [D11] |
| 33 | L887 | Separador: Conclusiones |  |
| 34 | L892–912 | Conclusiones |  |
| 35 | L914–918 | Muchas gracias |  |
| 36 | L921–939 | Disclaimer: uso de IA |  |

## 2. Todas las secciones por archivo

### `enunciado/TP5_Enunciado_Problema2.md`

| Línea | Sección |
|---|---|
| 8 | Parte general |
| 20 | Entregables |
| 28 | Fecha y forma de entrega |
| 39 | Problema 2: Evacuación con esquema híbrido Event Driven + Time Step Driven |
| 57 | Puntos a resolver |
| 69 | Referencias |

### `presentacion/Guia_Presentaciones.md`

| Línea | Sección |
|---|---|
| 9 | 1. Consideraciones Generales |
| 39 | 2. Secciones |
| 68 | 3. Otras consideraciones |
| 86 | Checklist rápido para figuras (resumen de 1.7–1.9, 2.4.6–2.4.7) |

### `teoria/Teorica_0_Introduccion_Simulacion.md`

| Línea | Sección |
|---|---|
| 11 | Diapo 3 — Dictado de la Materia (1) |
| 20 | Diapo 4 — Dictado de la Materia (2) |
| 30 | Diapo 5 — Dictado de la Materia (3) |
| 39 | Diapo 7–10 — De Sistema Real a Simulación |
| 49 | Diapo 19 — Caracterización de un Sistema: tomar datos |
| 55 | Diapo 20 — Simulación off-line (IMPORTANTE) |
| 68 | Diapo 21 — Caracterización de un Sistema: gráfico input vs observable (IMPORTANTE) |
| 76 | Diapo 22 — OUTPUT primario → Observable primario → Observable escalar |
| 86 | Diapo 23 — Ejemplo: curva de descarga y caudal |
| 100 | Diapo 24 — Gráfico input vs observable (repetido) |
| 106 | Diapo 26 — El Estado de un Sistema |
| 118 | Diapo 40 — Modelos Deterministas y Estocásticos |
| 125 | Diapo 41 — Modelo Estocástico |
| 134 | Diapo 46–49 — Simulaciones basadas en Tiempo Discreto |
| 143 | Diapo 50–51 — Simulaciones basadas en Eventos |
| 151 | Diapo 62–64 — Simulación vs. Animación |
| 162 | Diapo 66 — Consejos previos (lenguajes) |
| 170 | Diapo 67–68 — Histograma, Distribución de Probabilidad y PDF |
| 182 | Diapo 69 — Error Muestral o de Medición |
| 190 | Diapo 70–72 — Cifras significativas (IMPORTANTE) |
| 200 | Diapo 73 — Barras de error |
| 206 | Diapo 75 — Regresiones: ajustar con modelos teóricos |
| 212 | Diapo 76–78 — Ejemplos de ajuste |
| 221 | Diapo 79–80 — Error del ajuste |
| 233 | Diapo 81 — Ejemplo con el modelo lineal |
| 243 | Diapo 82 — Reiteramos |

### `teoria/Teorica_1_Reglas_de_Simulacion.md`

| Línea | Sección |
|---|---|
| 11 | Diapo 18 — Muchas Partículas Interactuantes |
| 19 | Diapo 19 — Detección de vecinos: Cell Index Method (CIM) |
| 30 | Diapo 20 — ¿Qué se quiere averiguar? |
| 38 | Diapo 21 — Idea general del CIM |
| 44 | Diapo 22 — Tamaño de celda incorrecto |
| 52 | Diapo 23 — Tamaño de celda correcto |
| 64 | Diapo 24 — Asignación de celdas |
| 70 | Diapo 25 — Simetría |
| 78 | Diapo 26 — Condiciones Periódicas de Contorno |
| 88 | Diapo 27 — Lista de Vecinos: ejemplo de formato |
| 101 | Diapo 31 — Sección: Reglas Generales de Simulaciones |
| 107 | Diapo 32 — Esquema simulación / análisis / animación (IMPORTANTE) |
| 116 | Diapo 33 — Animaciones |
| 124 | Diapo 34 — Visualizadores recomendados |
| 133 | Diapo 35 — Resultados (IMPORTANTE) |
| 139 | Diapo 36 — Formato de archivo: Info Dinámica |
| 162 | Diapo 37 — Formato de archivo: Info Estática |
| 179 | Diapo 38 — Entregables de los Trabajos Prácticos (TP2 en adelante) |
| 187 | Diapo 39 — Sección: Presentaciones, Formato |
| 193 | Diapo 40 — Presentaciones: tiempos y partes |
| 209 | Diapo 41 — Presentaciones: formato |
| 217 | Diapo 42 — Algunos consejos (Garland) |
| 227 | Diapo 44 — Interactuar con la audiencia |
| 235 | Diapo 45 — Preguntas de la audiencia |
| 243 | Diapo 46 — Varios expositores |

### `teoria/Teorica_5_Medios_Granulares.md`

| Línea | Sección |
|---|---|
| 13 | Diapo 2 — Definición |
| 22 | Diapo 5 — Propiedades: Ley de Beverloo (1961) |
| 38 | Diapo 7 — Simulaciones |
| 46 | Diapo 8 — Notación y variables geométricas (partícula–partícula) |
| 60 | Diapo 9 — Notación y variables geométricas (partícula–pared) |
| 71 | Diapo 10 — Ecuaciones de movimiento |
| 87 | Diapo 11 — Versores durante la colisión partícula–partícula |
| 102 | Diapo 12 — Versores durante la colisión partícula–pared |
| 117 | Diapo 14 — Fuerzas de contacto (esquemas) |
| 127 | Diapo 15 — Opciones de fuerzas de contacto |
| 129 | Dirección normal ($\hat{\mathbf{e}}^n$) |
| 134 | Dirección tangencial ($\hat{\mathbf{e}}^t$) |
| 145 | Diapo 16 — Suma de fuerzas (esquema) |
| 151 | Diapo 17 — Proyección a componentes cartesianas |
| 162 | Diapo 18 — Fuerza total sobre cada partícula |
| 174 | Diapo 19 — Criterio para elegir el paso temporal |
| 184 | Diapo 20 — Δt de simulación vs Δt₂ de guardado |
| 200 | Diapo 21 — Guardar con la máxima precisión |

### `teoria/Teorica_6_Simulacion_de_Multitudes_I.md`

| Línea | Sección |
|---|---|
| 13 | Diapo 4 — Evacuación de un recinto (condiciones competitivas) |
| 22 | Diapo 5 — El SFM predice cualitativamente el efecto "Faster is Slower" (FIS) |
| 30 | Diapo 20 — Principales observables en dinámica peatonal |
| 37 | Diapo 22 — Observable 2: Caudal Específico |
| 45 | Diapo 23 — Caudal Específico: tabla de fuentes |
| 61 | Diapo 28 — Social Force Model: ecuación de movimiento |
| 76 | Diapo 29 — SFM: Fuerza de Contacto (Granular) |
| 92 | Diapo 31 — SFM: Fuerza de autopropulsión (driving force) |
| 100 | Diapo 33 — SFM: Evacuación de un recinto, "Faster is Slower" |
| 123 | Diapo 34 — SFM: curva "Faster is Slower" |
| 129 | Diapo 45 — CPM: radio variable y velocidad deseada |
| 144 | Diapo 46 — CPM: sin contacto |
| 152 | Diapo 47 — CPM: cuando entran en contacto (1) |
| 160 | Diapo 48 — CPM: cuando entran en contacto (2) |
| 170 | Diapo 49 — CPM: evolución temporal, sin contacto |
| 182 | Diapo 50 — CPM: Desired Target |
| 192 | Diapo 51 — CPM: evolución temporal, con contacto |
| 206 | Diapo 52 — CPM: Evacuación de un recinto |
| 214 | Diapo 53 — CPM: Pista de carrera |
| 232 | Diapo 56 — Bondades del CPM |
| 239 | Diapo 57 — Desired Target (SFM y CPM) |

### `teoria/Teorica_7_Simulacion_de_Multitudes_II.md`

| Línea | Sección |
|---|---|
| 13 | Diapo 40 — Observables típicos |


### `correcciones/Correcciones_TP3_EventDriven.md`

| Línea | Sección |
|---|---|
| 11 | Observaciones generales |
| 19 | Por diapositiva |
| 21 | D4 y D5 — Introducción con información del sistema particular (IMPORTANTE) |
| 25 | D6 y D7 — Teoría general |
| 29 | D13 — Esquema del sistema |
| 33 | D19 — Ítem 1.1 |
| 37 | D20 — Orden de los resultados (IMPORTANTE) |
| 49 | D21 — Animaciones del caso estudiado |
| 53 | D21 y D22 — Nombres arbitrarios y evoluciones temporales |
| 59 | D23, D26 y D27 — Nombres, valores sobre los puntos y leyendas |
| 65 | D24 |
| 69 | D25 |
| 73 | D26 — Nombre del eje |
| 77 | D28 y otras — Animaciones |
| 83 | D29 — Semilla, leyendas y tamaño |
| 89 | D30 — Ajuste lineal |
| 96 | Conclusiones |
| 102 | Para el TP5 (agregado por nosotros, no es parte de la devolución) |

### `correcciones/Correcciones_TP4_TimeStep.md`

| Línea | Sección |
|---|---|
| 13 | Código |
| 24 | Por diapositiva |
| 41 | Para el TP5 (agregado por nosotros, no es parte de la devolución) |

