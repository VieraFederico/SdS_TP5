# DECISIONS — registro de decisiones del TP5

Qué decidimos, qué otras opciones había y por qué. Se actualiza con cada decisión nueva, de diseño o teórica. `DESIGN.md` dice cómo queda el sistema; este archivo dice por qué.

En cada decisión, **Por qué** es el argumento de la elección. Los números (★9, 21, …) son los de la sección 5 del prompt inicial y de `DESIGN.md`.

## Resumen

| # | Tema | Elegido | Estado |
|---|---|---|---|
| ★1 | Modelo | SFM sin fuerza social | Confirmada |
| ★3 | Integrador | Beeman predictor-corrector | Confirmada |
| 6 | Target | (L/2, R_i), luego (L/2, −2 m) | Confirmada |
| ★9 | Choque en EDMD | Elástico y con masas iguales, el del TP3. Reemplaza e = 0.94/0.92 y v_sep | Confirmada (2026-11-20, Etapa 9) |
| ★10 | Recuperación de v_d | Eventos fantasma | Confirmada |
| 16 | Avance en EDMD | Perezoso | Confirmada |
| 18 | Vecinos en EDMD | CIM con evento de cruce de celda | Confirmada |
| ★21 | Criterio de pasaje | **Uno solo:** tiempo entre contactos comparado con t_s, en los dos sentidos. Un EDMD que toca a un TSDMD pasa a TSDMD. Reemplaza zona + piel + tiempo de vuelo | Confirmada (2026-11-24, Etapa 10) |
| ★21b | Umbral del criterio | **t_s = 0.3 s ≈ 5·π/ω** (antes π/ω = 0.057 s) | Confirmada (revisada 2026-11-27, con el barrido del ítem c) |
| ★22 | Parámetro principal | **t_s** (antes R_s) | Confirmada (2026-11-24, Etapa 10) |
| ★23 | Sincronización | Reloj = la cola de eventos, con el paso de TSDMD como evento `Step`. El contacto EDMD–TSDMD se detecta en el `Step`, sin horizonte seguro | Confirmada (2026-11-24, Etapa 10) |
| H1 | v_max | **Eliminado:** sin piel no hace falta | 2026-11-24, Etapa 10 |
| H2 | Grilla | Celdas ≥ 2 r_max = 0.5 m en los tres esquemas (antes 0.75 m) | Confirmada (2026-11-24, Etapa 10) |
| 2 | Parámetros | m = 80 kg, τ = 0.5 s, k_n = 1.2·10⁵ N/m; k_t = 0 (ver "Fricción tangencial") | Confirmada |
| F | Fricción tangencial en TSDMD | k_t = 0, sin T.3 | Confirmada (2026-11-08) |
| 4, 5, 8, 11, 12, 19, 24, 25, 29, 32 | Ver "Aprobadas en bloque". La 11 fue reemplazada (Etapa 9); la 12 y la 24 cambiaron (Etapa 10) | Como se propuso | Confirmada |
| 7, 13, 14, 15, 17, 20, 26, 27, 28, 30, 31 | Ver "Por precedente" | Como en el TP3 o el TP4 | Por precedente |

Las secciones de abajo guardan la historia: lo reemplazado lleva una nota al principio que dice qué lo reemplaza. **Lo vigente sobre el híbrido está en la "Etapa 10".**

---

## Confirmadas

Todas el 2026-11-06.

### ★1 — Modelo peatonal

**Pregunta:** ¿qué modelo peatonal usamos?

| Opción | A favor | En contra |
|---|---|---|
| **SFM sin fuerza social (elegida)** | Un agente a v_d hacia un target fijo tiene fuerza total nula: va recto a velocidad constante, que es lo que supone EDMD. Parámetros en T6, diapo 33 | dt chico, por la rigidez del contacto |
| CPM | Más rápido; dt fijo por el modelo | El radio crece y la velocidad cambia sin contacto: el vuelo libre deja de ser recto y uniforme entre eventos, y hay que predecir con radios variables |

**Por qué:** es el modelo donde EDMD es exacto entre choques. Fue la inclinación inicial del grupo.

**Fuente:** enunciado; Teorica_6, diapos 28–33 y 45–56.

### ★3 — Integrador TSDMD

**Pregunta:** ¿qué integrador TSDMD usamos?

| Opción | A favor | En contra |
|---|---|---|
| **Beeman predictor-corrector (elegida)** | Variante pensada para fuerzas que dependen de la velocidad. Una fuerza por paso. Fue el más preciso en nuestro TP4 | Necesita a(t − dt): hay que definirla al pasar un agente a TSDMD |
| Velocity Verlet | Sin historia, más simple en el híbrido | La devolución del TP4 dice que con estas fuerzas queda en orden 1 |

**Por qué:** la fuerza de deseo y la tangencial dependen de la velocidad, que es el caso para el que está hecho. Además resuelve la corrección del TP4 de aclarar qué integrador se usó: hay uno solo.

**Consecuencia:** al entrar a TSDMD, a(t − dt) es la aceleración de deseo del agente. Sin interfaz `Integrator`.

**Fuente:** Teorica_4, p. 20 (repo del TP4); Correcciones_TP4, Código y D8.

### 6 — Targets

**Pregunta:** ¿qué target usamos? Con target en (L/2, 0), los agentes tocan la pared antes de la puerta.

| Opción | A favor | En contra |
|---|---|---|
| **(L/2, R_i) (elegida)** | La recta nunca baja de y = R_i: no toca la pared. Un agente solo no siente fuerza hasta cambiar de target, lo que da un test analítico | Apunta a un punto un radio adentro: la dirección final es más horizontal cerca de la puerta |
| Centro de la puerta (L/2, 0) | Más directo y fiel a la letra del enunciado | Con \|x − L/2\| > y, el agente toca la pared hasta metros antes y se arrastra; en EDMD son eventos a tiempo casi cero |
| Punto más cercano del vano | Target distinto por agente, más realista | Conserva el problema de la pared |

**Por qué:** evita el arrastre contra la pared, que es lo más problemático para EDMD. Ejemplo: con R = 0.2 m y salida en (1, 2), la recta baja a y = R cuando le faltan 2.4 m para la puerta.

**Consecuencia:** pasa a target 2, (L/2, −2 m), cuando |x − L/2| < d/2 e y ≤ R_i. Retiro en y = −1 m. Al cambiar de target la velocidad relaja, igual en los tres esquemas.

**Fuente:** Teorica_6, diapo 57; decisión nueva. Hay que decirlo en la presentación.

### ★9 — Regla de choque en EDMD

> **Reemplazada (2026-11-20).** El choque es el del TP3: elástico y con masas iguales (ver "Etapa 9"). Lo que sigue es la decisión original.

**Pregunta:** ¿qué restitución usamos en EDMD?

| Opción | A favor | En contra |
|---|---|---|
| **e derivado del SFM: 0.94 y 0.92 (elegida)** | Sale del modelo, sin ajustar nada. Debería acercar EDMD a la referencia TSDMD | Agentes que rebotan: poco intuitivo |
| e = 0 | Intuitivo para personas. Era la inclinación inicial | Se aleja de la referencia; más riesgo de colapso inelástico |
| Decidir con un piloto | Elegir con datos | Más tiempo antes de avanzar |
| e = 1 | Como el TP3, sin colapso | Ignora la disipación |

**Por qué:** la fuerza normal del SFM (N.2) es un resorte sin amortiguar, así que el choque blando rebota casi elástico. Para comparar con TSDMD, lo fiel es e alto, aunque e = 0 parezca más natural para personas.

**Derivación (nuestra, no está en `sources/`):** en un choque frontal entre iguales, la superposición cumple

```
ξ̈ + ξ̇/τ + (2 k_n/m) ξ = 0,   con ω = √(2 k_n/m) = 54.8 s⁻¹
e = exp(−π/(2τω)) ≈ 0.94
```

Contra una pared, ω = √(k_n/m) = 38.7 s⁻¹ y e ≈ 0.92. Valores: k_n = 1.2·10⁵ N/m, m = 80 kg, τ = 0.5 s.

**Límites:** ignora la parte constante de la fuerza de deseo y la fricción tangencial. Es una estimación. El choque es liso, sin fricción tangencial: esa diferencia con TSDMD debería verse en el ítem b.

**Consecuencia:** e queda como parámetro del motor.

**Fuente:** Teorica_3, diapos 20 y 23–24 (coeficientes c_n y c_t); Teorica_5, diapo 15.

### ★10 — Recuperación de v_d

**Pregunta:** ¿cómo se recupera v_d después de un choque en EDMD?

| Opción | A favor | En contra |
|---|---|---|
| **Eventos de relajación (elegida)** | Usa la solución exacta de la fuerza de deseo, con el mismo τ que TSDMD. Vuelo recto entre eventos: sigue valiendo la cuadrática | Agrega eventos; la curva queda en escalones de Δτ |
| Instantánea | Muy simple | En multitud recolisiona a tiempo cero; salta la velocidad, cosa que el SFM no hace |
| Proyección sobre la tangente | Evita meterse en el otro agente | EDMD no tiene contactos persistentes: habría que inventarlos |

**Por qué:** es la versión por eventos de la misma física que TSDMD.

**Consecuencia:** cada Δτ = 0.1 s, v ← v_d ê + (v − v_d ê)·exp(−Δτ/τ). Con |v − v_d ê| < 0.01·v_d se fija v = v_d ê y se cortan los eventos. Los valores de Δτ y la tolerancia son iniciales.

**Fuente:** Teorica_6, diapo 31; decisión nueva.

### 16 y 18 — Escala de EDMD

**Pregunta:** ¿nos apartamos del TP3 para que EDMD escale? El TP3 movía a todas en cada evento y predecía contra todas.

| Opción | A favor | En contra |
|---|---|---|
| **Perezoso + CIM (elegida)** | Costo por evento independiente de N. El ítem c mide EDMD, no una implementación ingenua | Agrega el evento de cruce de celda |
| Como el TP3 | Menos código; precedente no corregido | Costo N por evento, muy caro con multitud y N = 1000 |

**Por qué:** en el TP3 no había multitud ni variaba N hasta 1000. La devolución no lo corrigió, pero el precedente no aplica a este problema.

**Consecuencia:** `update` solo a los agentes del evento. Lado de celda ≥ 2·r_max.

**Fuente:** Teorica_1, diapos 19–25; Teorica_3, diapo 28.

### ★21 y ★22 — Criterio de pasaje y parámetro principal

> **Reemplazada (2026-11-24).** La cátedra pidió un solo criterio. Quedó solo el de tiempo (★21b) con su recíproca, y el parámetro principal pasó a ser t_s (ver "Etapa 10"). Lo que sigue es la decisión original.

**Pregunta:** ¿qué criterio de pasaje y qué parámetro principal usamos?

| Opción | A favor | En contra |
|---|---|---|
| **Zona R_s + contagio (elegida)** | R_s = 0 da EDMD puro exacto y R_s ≥ 56 m da TSDMD puro exacto. No hay contacto duro–blando | Espero salto entre R_s = 0 y 1 m, y meseta hasta unos 7–8 m |
| Solo zona R_s | Más simple, sin meseta | Si la multitud supera R_s, quedan duros contra blandos |
| Solo piel δ | Local, sin zona fija | Ningún valor reproduce EDMD puro, que pide el ítem c |
| Densidad local o número de vecinos (descartada en `DESIGN.md`) | Mide directamente lo denso | Hay que calcularla para todos en cada paso: EDMD pierde su ventaja |

**Por qué:** zona y contagio dan extremos exactos sin dejar contactos duro–blando. Se sumó además un criterio por tiempo: ver ★21b y `DESIGN.md`, 2.6.

**Criterio:**

```
EDMD → TSDMD  si  r_i ≤ R_s,  o  g_ij ≤ δ_in para algún j en TSDMD,  o  t_vuelo,i < t_s
TSDMD → EDMD  si  r_i ≥ R_s + h,  y  g_ij ≥ δ_out para todo j,  y  |v_i − v_d ê_i| ≤ ε v_d
```

**Valores iniciales:** δ_in = 0.05 m, δ_out = 0.25 m, h = 1 m, ε = 0.05, t_s = 0.057 s. Barrido: R_s ∈ {0, 1, 2, 3, 5, 8, 12, 20, 35, 56} m con t_s fijo.

**Riesgo aceptado:** la meseta, que con ★21b se extiende hasta R_s = 0. Es una predicción sin medir; si aparece, es un resultado del ítem c.

**Fuente:** enunciado, viñetas del híbrido e ítem c; decisión nueva.

### ★21b — Criterio por tiempo entre choques

> **Vigente (2026-11-24) como único criterio,** con su recíproca para volver (ver "Etapa 10"). Dos puntos quedaron viejos:
> - El argumento contra t_s = dt usaba v_sep, que ya no existe: dt pasó a ser un punto del barrido del ítem c.
> - Las "Consecuencias" sobre R_s y el contagio ya no aplican.

**Idea:** agregar un criterio para pasar de EDMD a TSDMD que compare el tiempo entre choques con un tiempo característico del paso.

**Qué tiempo medir:**

| Opción | A favor | En contra |
|---|---|---|
| **Tiempo de vuelo entre choques consecutivos (elegida)** | Solo es corto si el agente choca seguido. Se evalúa al procesar el choque, sin costo extra | Necesita un choque para detectar: el primero siempre lo resuelve EDMD |
| Tiempo al próximo choque | Anticipa el contacto | Tiende a cero antes de todo choque: pasaría todos a TSDMD y no quedaría ningún choque duro |

**Con qué umbral compararlo:**

| Opción | A favor | En contra |
|---|---|---|
| t_s = duración de un choque blando, π/ω ≈ 0.057 s (elegida hasta el 2026-11-27) | Es la escala de la condición de validez de EDMD: t_vuelo ≫ t_choque [Teorica_3, diapo 5] | Toma el "≫" como "=". En el barrido del ítem c es de los más caros (0.95 s) |
| **t_s = 0.3 s ≈ 5·π/ω (elegida, 2026-11-27)** | Implementa el "≫": unas 5 veces lo que dura un choque blando. En el barrido del ítem c (sesión 2026-11-25) es el más barato junto con 0.1 y 1 s (0.82 s contra 0.95 s, ~3.5 errores estándar), termina 10/10 y su ⟨t_evac⟩ es el más cercano a TSDMD puro (81.5 contra 81.7 s) | Se elige mirando datos del mismo barrido que se presenta; se explica así en la defensa |
| t_s = dt | Es lo más literal del pedido | Casi nunca dispara: el piso v_sep separa los choques por más que un paso |
| t_s = κ·dt | Liga el umbral al costo de un paso | Agrega un parámetro sin base teórica, y depende del dt elegido |

**Por qué:** la comparación con el step sigue presente, porque t_s = 300 pasos a dt = 10⁻³ s (con π/ω eran 57). El umbral es un parámetro del motor (`SocialForceModel.FLIGHT_THRESHOLD`); π/ω queda como un punto más del barrido del ítem c.

**Revisión 2026-11-27 (pedido del grupo):** de π/ω a 0.3 s, por el barrido del ítem c (ver la fila de arriba). Δt queda en 10⁻³ s: lo fija el estudio de Δt, que es TSDMD puro y no depende de t_s. Se vuelven a correr a/b, c, d y las animaciones; el estudio de Δt no.

**Consecuencias:**

- **Cambia el extremo de EDMD:** EDMD puro ahora es R_s = 0 **y** t_s = 0. Con t_s > 0, R_s = 0 es un híbrido válido.
- **Pasaje en cadena:** al pasar a TSDMD se revisan los vecinos con el contagio, para mantener la garantía de no perder contactos.
- Evita que una multitud fuera de la zona quede en EDMD con tasa de eventos enorme.

**Fuente:** Teorica_3, diapo 5; decisión del grupo. t_s sale de ω en ★9.

### ★23 — Sincronización

> **Cambió dos veces.**
> - **2026-11-13:** el reloj pasó a ser la cola de eventos, con el paso de TSDMD como evento `Step` (P5, Etapa 7).
> - **2026-11-24:** sin piel no hay horizonte seguro ni v_max. El contacto EDMD–TSDMD se detecta en cada `Step` (ver "Etapa 10").
>
> Lo que sigue es la decisión original.

**Pregunta:** ¿cómo sincronizamos los dos esquemas?

| Opción | A favor | En contra |
|---|---|---|
| **Reloj dt + horizonte seguro (elegida)** | Cada EDMD agenda su chequeo: los lejanos no cuestan nada por paso | Un tipo de evento más; hay que acotar v_max |
| Reloj dt + piel por paso | Más simple | Costo N_ED por paso: resta la ventaja que mide el ítem c |

**Por qué:** conserva la ventaja del híbrido en la zona diluida y mantiene la garantía de no perder contactos.

**Consecuencia:** en cada intervalo se procesan los eventos hasta t + dt, después un paso TSDMD; los pasajes se aplican al final del paso. Horizonte: t + (g − δ_in)/(v_i + v_max). Garantía: (v_i + v_j)·dt < δ_in, con v_max = 3 m/s (valor inicial; pasó a 5 m/s, ver "Etapa 4: híbrido"); si se supera, el motor corta con error.

**Fuente:** enunciado; decisión nueva.

---

### 2 — Parámetros no especificados

**Pregunta:** ¿de dónde salen la masa y los demás parámetros? Había dudas sobre si el enunciado fijaba la masa.

**Verificado el 2026-11-06:** en el PDF original (`TP5_Enunciado.pdf`, 4 páginas, con el Problema 1) no hay ninguna masa ni τ, k_n o k_t. Dice: "Tomar los parámetros no especificados aquí de los papers correspondientes".

**Elegido, de Helbing, Farkas y Vicsek (2000):**

| Parámetro | Valor | Texto del paper |
|---|---|---|
| m | 80 kg | "With a mass of m_i=80 kg, we represent an average soccer fan." |
| τ | 0.5 s | "A reasonable estimate for the acceleration time is τ_i=0.5 s." |
| k_n | 1.2·10⁵ kg/s² (= N/m) | "k=1.2·10^5 kg s^-2" |
| k_t | 2.4·10⁵ kg/(m·s) | "κ=2.4·10^5 kg m^-1s^-1" |

La masa es la misma para todos. Los radios y v_d salen del enunciado, no del paper. Leído en la versión de arXiv, cond-mat/0009448. k_n, k_t y τ también están en Teorica_6, diapo 33.

## Aprobadas en bloque

Aprobadas el 2026-11-06 junto con `DESIGN.md`, sin cambios.

| # | Tema | Propuesta | Alternativas | Por qué la propuesta |
|---|---|---|---|---|
| 4 | dt y dt₂ | dt por estudio con M = 10: el mayor con ⟨t_evac⟩ a menos de un σ del de menor dt. dt₂ = 0.5 s, 0.1 s en animaciones | Fijar dt = 2.6·10⁻³ s de T5, diapo 19 | C4, D17 pide criterio y umbral marcado |
| 5 | Velocidad inicial | v₀ = v_d ê_target | Reposo: suma un transitorio y deja de ser vuelo libre | Arrancan en vuelo libre |
| 8 | Coordenadas | Sala [0, L]², puerta en y = 0 | — | Simple, sin alternativa relevante |
| 11 | Colapso inelástico | **Reemplazada (Etapa 9): con el choque elástico no hay colapso y v_sep desapareció.** Antes: piso v_n,sal = max(e·v_n,ent, v_sep), v_sep = 0.02 m/s | e chico: no alcanza. Tiempo mínimo entre choques: puede dejar pasar contactos. Separación mínima: mueve posiciones | Acota la tasa de eventos con cualquier e, sin tocar posiciones |
| 12 | Tipos de evento | Choque, pared, borde, target, puerta, retiro, fantasma, bounce back, cruce de celda, escritura y, en el híbrido, `Step`. Zona y piel se eliminaron en la Etapa 10 | Fusionar algunos | Salen de las decisiones ya tomadas |
| 19 | Escritura en EDMD | Evento de escritura cada dt₂ | Cada k eventos, como el TP3: no cumple dt₂ | El enunciado pide dt₂ |
| 24 | Colisiones en el híbrido | ED–ED: evento duro; pasa a TSDMD si es el segundo contacto en menos de t_s. ED–TS: el EDMD que se superpone con un TSDMD pasa a TSDMD en el `Step` (Etapa 10; antes lo evitaba la piel) | Pasar a TSDMD ante cualquier choque ED–ED: no da EDMD puro con ningún parámetro | Consecuencia de ★21 |
| 25 | Formato | `id x y vx vy scheme location`, N filas siempre, `exits.txt` aparte | Omitir retirados: archivo más chico, pero menos claro respecto de "cada partícula" [C4] | Cumple C4 sin reconstruir nada |
| 29 | Ítem d | N ∈ {100, 200, 300, 500, 700, 1000} | Otros valores | Cubre el rango del enunciado |
| 32 | Figuras | Comando que copia a `presentation/figuras/` | `\graphicspath` a `output/`: rutas con fecha, cambian en cada sesión | El TP4 lo hacía a mano |

---

## Por precedente

Se usa lo mismo que en el TP3 o el TP4, y la devolución no lo corrigió.

| # | Tema | Qué se usa | Origen |
|---|---|---|---|
| 7 | Paredes y puerta | Punto más cercano del segmento; extremos como obstáculos puntuales | TP4, pared con imagen, OK en C4 |
| 13 | Tiempo de choque | Cuadrática | TP3, `Particle.collides` |
| 14 | Cola | `PriorityQueue`, desempate por orden | TP3, `Board`, `Event` |
| 15 | Invalidación | Contador por partícula. Ajuste: crece con todo cambio de velocidad | TP3, `Event.wasInvalidated` |
| 17 | Recálculo | Solo los involucrados | TP3, `Board.simulate` |
| 20 | Robustez | Tolerancia `-1e-10`; error si hay superposición real | TP3, `Board.scheduled` |
| 26 | Configuración | Flags y semillas consecutivas | TP4, `RunConfig` |
| 27 | Wall-clock | `nanoTime` en el loop, dt₂ grande, un proceso, `--warmup` | TP4, `ExperimentRunner`; TP3, `Main` |
| 28 | Estadística | M = 10, misma semilla entre esquemas, σ con M − 1 | TP4, `stats.py`; enunciado |
| 30 | CLI | Menú y subcomandos por estudio | TP4, `cli.py` |
| 31 | Animación | matplotlib + `imageio-ffmpeg`; `animate` con PDF | TP4, `animation.py` |

---

## De proceso

| Decisión | Motivo |
|---|---|
| Los commits los hace el grupo a mano | Control de lo que entra al repo |
| Las dependencias las instala el grupo a mano | Decisión del grupo |
| `venv`, no `uv` | Decisión del grupo |
| `scratchpad/` ignorado por git | Para retomar en otra sesión |
| `DECISIONS.md` con todo lo decidido | Para saber qué se eligió y por qué |
| LaTeX se compila en un editor online, no localmente | Decisión del grupo |
| Python solo dentro de `simulation/.venv` | Decisión del grupo |
| Compilar con `javac --release 21`, sin Maven | Como el TP4; `mvn` no está instalado |
| Entrega: `SdS_TP5_2026Q2G09CS_Presentación` y `SdS_TP5_2026Q2G09CS_Codigo` | Grupo 9, comisión S |

---

## Abiertas

### Atascos permanentes en TSDMD (2026-11-06)

**Problema:** con SFM sin fuerza social, N = 500 y d = 0.8 m, la puerta se traba para siempre en la mayoría de las corridas. Un arco de agentes queda quieto (v = 0) y `t_evac` no existe. El enunciado pide el tiempo de salida del último agente.

**Medido en TSDMD (dt = 2.5·10⁻⁴ s, tope 2500 s):**

| Target | Corridas trabadas | Caudal |
|---|---|---|
| (L/2, R_i), decisión 6 | 2 de 3 | 0.45–0.51 p/s |
| (L/2, 0) | 2 de 3 | 0.47–0.57 p/s |
| (L/2, −0.5 m) | 2 de 3 | 0.45–0.47 p/s |
| Punto más cercano del vano | 5 de 6 | 0.40–0.48 p/s |

Una corrida siguió hasta t = 48 000 s con 15 agentes quietos. Cuando no se traba: t_evac ≈ 960–1090 s.

**Hipótesis descartadas:** bug de fuerzas (con fuerza social el caso de Teorica_6, diapo 33 da t_evac ≈ 93 s, Q ≈ 2.5 p/s), posición del target y campo convergente.

**Otros datos:** sin fuerza social, ese mismo caso de T6 da Q ≈ 0.7 p/s y una de dos corridas se trabó. El rango 1.0–1.6 p/s sale de datos experimentales, no del SFM.

**También medido:** dt = 10⁻³ s es inestable con esta multitud (explota en t ≈ 27 s, ξ ≈ 6.6 cm); dt ≤ 5·10⁻⁴ s fue estable hasta t = 120 s. El valor final sale del estudio de dt (decisión 4).

**Decisión (2026-11-07):** mantener SFM con velocidad inicial, no consultar a la cátedra y que la puerta no se trabe: todos los agentes tienen que seguir en camino. Además, un evento "bounce back" para EDMD: cuando la velocidad es la deseada o muy parecida y el agente apunta a la dirección deseada.

**Descartado (2026-11-07):** agente impaciente (giro aleatorio del deseo cuando queda quieto). Se descartó y la prueba se cortó sin resultados.

**Indicación del profesor (notas de clase, 2026-11-06):** "Eventos fantasmas"; "tiempo característico, o sea que una partícula viaja con cierta velocidad, cuánto tarda en viajar su radio"; "mientras está pasando eso podríamos tener otro evento"; "ojo"; "tiempo de corte para event driven"; "es un resultado viable que event driven falle".

**Resuelto por las consideraciones de la cátedra (2026-11-08):** ver más abajo.

### Decisiones derivadas de las notas del profesor (2026-11-07)

| Tema | Decisión | Por qué | Estado |
|---|---|---|---|
| ★10, Δτ | Los eventos de relajación pasan a ser **eventos fantasmas**: Δτ_i = R_i/max(\|v_i\|, v_d), unos 0.12–0.17 s. Reemplaza los 0.1 s fijos | Interpretación de "cuánto tarda en viajar su radio". El max evita que un agente lento no se recupere nunca | Interpretación del grupo; confirmada |
| ★10, fin de la relajación | Evento **bounce back**: fija v = v_d ê cuando \|v − v_d ê\| < 0.01·v_d. El agente vuelve al vuelo libre | Decisión del grupo | Confirmada |
| Otro evento en el medio | Un choque invalida el fantasma pendiente; el agente conserva la hora del próximo fantasma | "Mientras está pasando eso podríamos tener otro evento. Ojo" | Interpretación del grupo |
| Tiempo de corte | EDMD corta por `--max-time` y `--max-events`, y escribe `complete false`. El análisis lo informa, sin descartar la corrida | Profesor; Correcciones_TP4, D30 | Confirmada; falta el valor |

### Consideraciones de la cátedra (2026-11-08)

La cátedra mandó cambios al enunciado (`sources/enunciado/TP5_Consideraciones.md`) que resuelven buena parte de lo anterior.

| Tema | Decisión | Por qué | Estado |
|---|---|---|---|
| N | **N = 200 por ahora** en los ítems a–c (default del motor). N = 500 queda posible | Opción ii de la cátedra; para correr más rápido | Confirmada |
| Atascos en EDMD | Válidos; no se cambia el mecanismo. Tiempo de corte y `complete false` | Opción iii de la cátedra, coincide con lo que dijo el profesor | Confirmada |
| Fricción tangencial en TSDMD | **k_t = 0.** `--kt` sigue como parámetro del motor, ahora con default 0 | Opción iii de la cátedra: "opcionalmente pueden desestimar las fuerzas de rozamiento tangencial". Ver "Fricción tangencial: elegida" abajo | Confirmada (2026-11-08) |
| Introducción | Además de lo pedido, explicar en la Introducción la colisión de EDMD y la recuperación de v_d. Contradice la guía 2.1, pero manda el enunciado | Consideración i de la cátedra | Confirmada |

**Argumento a favor de sacar la fricción:** EDMD tiene choques lisos (c_t = 1). Sin T.3, los dos esquemas tienen la misma física de contacto, solo normal, y la comparación del ítem b queda más limpia. Además, la inestabilidad de dt = 10⁻³ s venía del amortiguamiento tangencial.

**Resultados de fricción** (TSDMD, N = 200, 6 semillas, tope 3000 s, 2026-11-08):

| Caso | Terminan | t_evac | Caudal | Wall-clock |
|---|---|---|---|---|
| Con fricción, k_t = 2.4·10⁵, dt = 2.5·10⁻⁴ | 1 de 6 | 436 s la que terminó; las otras se trabaron con 80–190 salidos | 0.38–0.55 p/s | 16–111 s |
| Sin fricción, k_t = 0, dt = 2.5·10⁻⁴ | 6 de 6 | 76–85 s | 2.7–3.0 p/s | 3.0–3.3 s |
| Sin fricción, k_t = 0, dt = 10⁻³ | 6 de 6 | 77–85 s | 2.7–3.1 p/s | 0.6–0.7 s |

Sin fricción: no hay atascos, dt = 10⁻³ es estable (v_max 2.1–2.5 m/s) y da los mismos t_evac que dt = 2.5·10⁻⁴. El caudal específico (≈ 3.6 p/m/s) queda por encima del experimental (1.25–2.0). Ojo: v_max supera v_d; la garantía del híbrido usa v_max = 3 m/s, revisar el margen.

**Elegida:** k_t = 0.

### Fricción tangencial: elegida (2026-11-08)

**Pregunta:** ¿usamos la fricción tangencial T.3 de Helbing (k_t = 2.4·10⁵ kg/(m·s)) o la sacamos, como permite la cátedra?

| Opción | A favor | En contra |
|---|---|---|
| **k_t = 0 (elegida)** | Terminan 6 de 6 corridas (t_evac 76–85 s). Misma física de contacto que EDMD, que tiene choques lisos (c_t = 1): el ítem b compara esquemas y no modelos. dt = 10⁻³ s es estable y da los mismos t_evac que 2.5·10⁻⁴ s | Caudal específico ≈ 3.6 p/m/s, por encima del experimental (1.25–2.0). Hay que decir en la presentación que se sacó T.3 y por qué |
| k_t = 2.4·10⁵ | Es el SFM de Helbing tal cual | Se traban 5 de 6 corridas (atascos válidos para la cátedra, pero t_evac no existe); pide dt ≤ 5·10⁻⁴ s; TSDMD tiene una fricción que EDMD no puede representar |

**Por qué:** sin fricción no hay atascos en TSDMD, el dt puede ser 4 veces mayor y los tres esquemas comparten la misma física de contacto.

**Consecuencias:**

- Default del motor: `--kt 0`. `SocialForceModel.KT` sigue valiendo 2.4·10⁵ como constante de Helbing, para los tests de fuerza y para correr con fricción si hace falta.
- Default de `--dt`: 10⁻³ s (antes 2.5·10⁻⁴) y `--dump-every` 500, para mantener dt₂ = 0.5 s. Es un valor provisorio: el dt final sale del estudio de la decisión 4.
- La derivación de e (★9) ignoraba la fricción: ahora esa limitación desaparece.

**Fuente:** `sources/enunciado/TP5_Consideraciones.md`, iii; tabla "Resultados de fricción".

**Corrección (2026-11-08, misma sesión):** con más semillas, **k_t = 0 también se traba**, aunque menos. N = 200, 50 semillas nuevas (11–60), tope 600 s, dt = 10⁻³ s: TSDMD puro 10 de 50 trabadas, EDMD puro 8 de 50, híbrido (R_s = 5 m) 5 de 50. El 6 de 6 de la tabla de arriba fue una muestra chica. Los atascos son arcos sobre la puerta con apoyo en agentes apretados contra la pared al lado de los bordes; con discos todas las fuerzas normales son centrales, así que un arco puede quedar en equilibrio sin fricción. Siguen siendo un resultado válido (consideraciones iii) y no cambian la elección: con fricción se traban 5 de 6. Lo que cambia es que el análisis **tiene que** informar las corridas no terminadas en los tres esquemas.

---

## Etapa 4: híbrido (2026-11-08)

Decisiones de implementación. Ninguna cambia lo aprobado en `DESIGN.md`, salvo v_max, que era un valor inicial. Confirmadas el 2026-11-08.

### v_max de la garantía de la piel: 3 → 5 m/s

> **Eliminado (2026-11-24).** Sin piel, nada depende de v_max: se borraron `--v-max` y el corte por error (ver "Etapa 10").

**Pregunta:** ¿alcanza v_max = 3 m/s? El horizonte seguro y el corte por error suponen que ningún agente TSDMD lo supera.

**Medido** (N = 200, dt = 10⁻³ s, k_t = 0, velocidad máxima en **cada paso**, 160 corridas TSDMD e híbridas): mediana 2.69 m/s, p95 3.05, máximo 3.29. **12 de 160 corridas superan 3 m/s**: con v_max = 3 el motor cortaba con error en el 7.5 % de las corridas (se vio en vivo: 3.02 m/s en t = 25.6 s). El 2.5 m/s de antes salía de frames cada dt₂ y se perdía los picos.

| Opción | A favor | En contra |
|---|---|---|
| **v_max = 5 m/s (elegida)** | 52 % de margen sobre el máximo de 160 corridas. 2·v_max·dt = 10 mm, sigue muy por debajo de δ_in = 50 mm, incluso con dt = 2.5·10⁻³ s | Horizonte un 30 % más corto: más chequeos de piel. Medido: sin diferencia visible en el wall-clock |
| v_max = 4 m/s | Menos chequeos | 22 % de margen: con N = 500 o más semillas puede volver a cortar |
| v_max adaptativo (el máximo actual) | Horizonte justo | El horizonte mira hacia adelante: un pico posterior lo invalida. Complica la garantía |
| Sin corte, solo aviso | Nunca pierde corridas | Deja de ser una garantía |

**Por qué:** el corte tiene que ser una red de seguridad que no salte nunca en los estudios, no un filtro de corridas. **Confirmada (2026-11-08).** Sigue siendo `--v-max`.

### Una sola grilla para los tres esquemas, con celdas ≥ 2 r_max + δ_out = 0.75 m

> **Cambió (2026-11-24).** δ_out ya no existe. Las celdas son de 2 r_max = 0.5 m, lo mínimo para que todo contacto quede en las 3×3 celdas. Sigue habiendo una sola grilla para los tres esquemas (ver "Etapa 10").

**Pregunta:** el criterio S2 pide g ≥ δ_out con **todos** los agentes, y el CIM solo ve las 3×3 celdas. Con celdas de 0.5 m, un agente fuera de las 3×3 puede estar a gap ≈ 0.

| Opción | A favor | En contra |
|---|---|---|
| **Celdas de 2 r_max + δ_out en los tres esquemas (elegida)** | Fuera de las 3×3 celdas el gap es ≥ δ_out: S2 y el horizonte de piel quedan completos. Misma grilla en los tres: el ítem c compara esquemas, no grillas, y los extremos dan bit a bit lo mismo que los puros | Más vecinos por celda (5 m² contra 2.25 m²) también en los puros |
| 0.75 m solo en el híbrido | Los puros quedan como estaban | Los extremos del híbrido no coinciden con los puros (caos con distinta grilla) y el ítem c mezcla grilla con esquema |
| Mirar 5×5 celdas en S2 | Grilla de 0.5 m | Más código; el horizonte de piel igual necesita la cota |

**Por qué:** es la única que da extremos exactos y una comparación limpia en el ítem c. El motor valida que el lado de celda alcance (`HybridScheme` corta si no).

### Cómo se implementa el chequeo de piel (E2)

> **Eliminado (2026-11-24),** junto con E2 (ver "Etapa 10").

| Pieza | Decisión | Por qué |
|---|---|---|
| Posición de los TSDMD durante los eventos | Están al inicio del paso: se resta v_max·(t − t_n) al gap | Los eventos se procesan antes del paso TSDMD |
| Agentes EDMD sin ningún TSDMD en sus 3×3 celdas | Sin chequeo de piel | Un EDMD lejos no cuesta nada |
| TSDMD que entra a una celda nueva, o agente que pasa a TSDMD | Al final del paso, sus vecinos EDMD a ≤ δ_in pasan a TSDMD (cadena) y los demás reprograman su chequeo | Antes de entrar estaba fuera de las 3×3: gap ≥ δ_out, y en un paso se mueve v_max·dt |
| Varios chequeos pendientes | Uno solo vivo por agente (sello `skinStamp`); no toca `revision` | El chequeo no cambia la velocidad: no debe invalidar los choques predichos |
| Verificación | Al pasar a TSDMD, el motor corta con error si el agente toca a un TSDMD | La garantía se comprueba, no se supone |

### Otras

> **Ojo (2026-11-24):** las filas de S1, S2 y S3 y la de la vuelta a EDMD quedaron viejas (ver "Etapa 10"). Siguen vigentes la de escritura y la de entrada a TSDMD, aunque ahora el agente puede entrar en contacto.

| Tema | Decisión | Por qué |
|---|---|---|
| Escritura en EDMD | Frame k en (k·dump)·dt, igual que el conteo de pasos de los otros esquemas (antes, sumando dt₂) | Mismos instantes, bit a bit, en los tres esquemas: permite el test de extremos exacto |
| S2 | Además de g ≥ δ_out, no tocar ninguna pared | Un agente EDMD superpuesto con la pared rebotaría con evento a tiempo cero |
| Entrada a TSDMD | a(t) = a(t − dt) = aceleración de deseo, como dice ★3 | No hay contactos al entrar (g > 0 con todos) |
| Vuelta a EDMD | Arranca con fantasmas desde ese instante y sin último choque (E3 empieza de nuevo) | Como `DESIGN.md` 2.6 |
| S1, S3 y S2 | Se evalúan en ese orden y el gap se calcula solo si hace falta | Calcular el gap de todos los TSDMD en cada paso duplicaba el costo del híbrido (1.14 s contra 0.55 s) |

### Primeras observaciones (no son el estudio del ítem c)

N = 200, dt = 10⁻³ s, 8 semillas (101–108), dt₂ = 0.5 s, wall-clock medio de las terminadas: TSDMD 0.52 s, EDMD 2.42 s, híbrido R_s = 0: 0.55 s, R_s = 12: 0.62 s, R_s = 56: 0.61 s. Fracción TSDMD media adentro ≈ 0.55 con R_s ≤ 5 m y ≈ 0.7 con R_s = 12 m: con t_s > 0 la multitud pasa a TSDMD por E3 aunque R_s sea chico (la meseta prevista en `DESIGN.md` 2.6). Casi nadie vuelve a EDMD (0–3 agentes por corrida): S2 y S3 casi nunca se cumplen juntos fuera de la zona. Es un resultado, a mirar en el ítem c. Con N = 200, TSDMD es tan barato que el híbrido no le puede ganar; con N = 500 o 1000 (ítem d) puede cambiar.

---

## Revisión del motor (2026-11-08)

Revisión de código de `experiment/src` (nivel alto). Diez hallazgos, verificados uno por uno contra el código: ocho corregidos y dos descartados.

### ★10, corrección: el fantasma amortiguaba el rebote desde antes del choque

**Problema:** un choque con un fantasma pendiente conserva la hora del fantasma y `relaxedAt`, como se decidió. Pero en el fantasma, e^{−(t−relaxedAt)/τ} se aplicaba a la velocidad **posterior** al choque: el impulso se amortiguaba por un tiempo que pasó antes de que existiera. Con un choque 10 ms antes del fantasma, se perdía un 26 % del rebote casi en el acto.

| Opción | A favor | En contra |
|---|---|---|
| **Solución exacta por componentes en el fantasma (elegida)** | Respeta ★10: la velocidad sigue fija entre fantasmas. La EDO es lineal: la velocidad de `relaxedAt` decae todo el intervalo y cada impulso Δ_k solo desde su instante t_k. Alcanza con guardar ΣΔ_k y ΣΔ_k·e^{(t_k − relaxedAt)/τ} por agente | Dos vectores más por agente |
| Aplicar la relajación pendiente en cada choque | Una línea | Probada y descartada: cada choque recibe un empujón de la fuerza de deseo. En la multitud de la puerta la tasa de eventos subió unas 9 veces (≈ 30 000 contra 3 500 eventos por segundo simulado) y la cola se quedó sin memoria. Además contradice ★10 |

**Consecuencia medida** (N = 200, semillas 101–108): EDMD puro pasa de unos 380 000 a unos 200 000 eventos por corrida y de 2.4 s a 1 s, con t_evac de 94 a 145 s. Al pasar de EDMD a TSDMD se aplica la relajación pendiente con la misma fórmula (antes se perdía).

**Corrección** (no cambia lo decidido). El test `collisionIsDampedOnlyAfterIt` la verifica con el valor calculado a mano.

### Otros corregidos

| Hallazgo | Corrección |
|---|---|
| Un agente que se desliza por y = r hacia el vano solo cambiaba de target si otro evento caía adentro del vano (hoy lo salvaba de casualidad un borde de celda en x = 25 o el roce con el borde) | Evento `Opening` en el instante exacto en que entra al vano |
| Híbrido cortado por `--max-events` en medio de un paso: avanzaba a los EDMD más allá de eventos sin procesar y podía cortar con "touches time-step agent" | Corta antes del paso TSDMD y devuelve `complete false` |
| EDMD cortado justo después de un frame: escribía dos frames con el mismo t | El frame final solo si t es mayor que el último escrito |
| La carpeta `run-NNN` se creaba antes de validar: un flag inválido la dejaba vacía y bloqueaba la siguiente corrida | Se construye el esquema (que valida) antes de crear la carpeta |
| `--delta-out` fija las celdas de los tres esquemas pero solo el híbrido lo validaba; `--e-pair`, `--e-wall`, `--v-sep` y `--max-events` no se validaban | Validados en `RunConfig` para todos los esquemas |
| El criterio usaba la constante v_d del SFM en vez de `model.desiredSpeed()` | Usa el modelo |

### Descartados

| Hallazgo | Por qué no |
|---|---|
| En el híbrido, `predict` y `watch` recorren dos veces las 3×3 celdas | El híbrido tiene unos 10 000 a 17 000 eventos por corrida: el ahorro es despreciable y juntar los recorridos ensucia `predict` |
| `switchSchemes` crea dos listas por paso | Medido: el híbrido con R_s = 0 cuesta lo mismo que TSDMD (0.5 s); la JVM elimina esas asignaciones cortas |

---

## Etapa 5: CLI de Python (2026-11-09)

Base del CLI (`simulation/src/simulation/`) y estudio de dt. Se reutiliza el CLI del TP4 casi tal cual: `options.py`, `sessions.py`, `cli.py`, `batch.py`, `engine.py`, `studies/base.py` y el estilo de `plotting.py`. `requirements.txt` y `pyproject.toml` son copia exacta de los del TP4. Nuevos: `reader.py` (formato de la sección 4 de `DESIGN.md`), `observables.py` y `export.py`.

### Tope de seguridad en los estudios: 1000 s con N = 200 (provisoria, a confirmar)

**Problema:** el tope de 10⁵ s del resumen sirve para no acotar t_evac [Correcciones_TP4, D30], pero una corrida trabada (arco estático sobre la puerta: con k_t = 0 no hay nada que lo rompa) llega siempre hasta el tope. Con dt = 10⁻³ s son 10⁸ pasos y unos 2 GB de `dynamic.txt` a dt₂ = 0.5 s, por corrida trabada. Con un 10–20 % de atascos, el estudio de dt solo tendría 5–10 corridas así.

| Opción | A favor | En contra |
|---|---|---|
| 10⁵ s (el default del motor) | No hay discusión posible sobre el tope | Horas de CPU y GB de disco por cada atasco, sin información nueva |
| **Tope por estudio, 1000 s con N = 200 (elegida, provisoria)** | Es 7–13 veces el t_evac de una corrida sin atasco (76–145 s medidos); se informa además el instante del último cruce, que muestra que la corrida estaba trabada mucho antes del corte | Hay que justificar que 1000 s alcanza: se ve en el último cruce de cada corrida trabada |
| Detectar el atasco en el motor | Corta apenas se traba | Es un observable dentro del motor: prohibido |

**Por qué:** el tope no acota t_evac de ninguna corrida que termina (todas terminan antes de 150 s), y las trabadas se informan con cuántos salieron y cuándo fue el último cruce. Es un parámetro de cada estudio (`--max-time`). **Falta que el grupo lo confirme.**

### Corridas sin terminar en el análisis

| Tema | Decisión | Por qué |
|---|---|---|
| Qué es una corrida completa | `complete true` en `params.txt` y todos los agentes con `t_retire` | El motor escribe `complete false` al cortar por tope |
| Qué se informa | Por caso, k/M completas y, por cada incompleta: salidos, último cruce y tope. Todas van al CSV `runs.csv` | Atascos válidos en los tres esquemas [Consideraciones, iii]; no se descartan en silencio |
| ⟨t_evac⟩ | Promedio y σ (M − 1) de las completas, diciendo cuántas son | t_evac no existe en una trabada; usar el tope sería acotarlo [Correcciones_TP4, D30] |
| Caso terminado para el CLI | Una corrida trabada es un resultado: el caso queda `DONE` aunque tenga `complete false` | En el TP4 se exigía `complete true` y una corrida trabada se repetiría siempre |

### Observables (`observables.py`)

| Observable | Definición | Fuente |
|---|---|---|
| N(t) | N − #{i : t_door,i ≤ t}, de `exits.txt` (exacto en t, sin depender de dt₂) | `DESIGN.md` 4; T7, diapo 40 |
| t_evac | max_i t_door,i; NaN si alguno no cruzó | Enunciado, ítem b |
| f_TS(t) | N_TS(t)/N(t) entre los agentes con `location` 0, de `dynamic.txt` | Enunciado, ítem a |
| Q | (k₉₀ − k₁₀)/(t₍k₉₀₎ − t₍k₁₀₎) sobre los cruces ordenados, con k₁₀ = ⌈0.1 N⌉ y k₉₀ = ⌊0.9 N⌋: deja afuera la llegada de los primeros y la cola de los últimos. Q/d es el caudal específico | T6, diapos 21–23; decisión del grupo |
| ξ_max | Mayor superposición entre agentes no retirados en los cuadros guardados. Chequeo de cordura, no un resultado | Prompt, chequeos de cordura |

### Estudio de dt (`timestep`)

| Tema | Decisión | Por qué |
|---|---|---|
| Casos | TSDMD puro, k_t = 0 (default del motor), N = 200, dt ∈ {2.5·10⁻³, 10⁻³, 5·10⁻⁴, 2.5·10⁻⁴, 10⁻⁴} s, M = 10, semillas 1–10 iguales para todo dt, dt₂ = 0.5 s | `DESIGN.md` 2.2 |
| Un caso por dt con `--repeats` | Un proceso Java por dt, las 10 realizaciones en serie | Como el TP4 |
| Criterio | El mayor dt con \|⟨t_evac⟩(dt) − ⟨t_evac⟩(dt_min)\| < σ(dt_min) y al menos 2 corridas completas | `DESIGN.md` 2.2; Correcciones_TP4, D17 |
| Figura principal | ⟨t_evac⟩ vs dt (eje x logarítmico, ticks en los dt estudiados), banda gris = ⟨t_evac⟩(dt_min) ± σ, línea punteada en el dt elegido (`--chosen-dt`; si no, el sugerido) | Umbral marcado en la figura [Correcciones_TP4, D17] |
| Figuras secundarias | ⟨Q⟩ vs dt, y N(t) de una realización completa por dt | Evolución temporal típica [Guía 2.4.2] |

### Figuras y carpetas

| Tema | Decisión | Por qué |
|---|---|---|
| Sesiones | `output/<estudio>/<AAAA-MM-DD_HH-MM-SS>[_quick]/`, sin prefijo de ítem | `DESIGN.md` 4 |
| Figuras | `<sesión>/figures/<ítem>/<nombre>.png`, con ítem ∈ {dt, a, b, c, d} | Un estudio puede generar figuras de dos ítems (a y b) |
| A la presentación | `simulation figures [--studies …] [--session …]` copia la última sesión completa (nunca una `_quick`) a `presentation/figuras/<ítem>/` | Decisión 32 |
| Estilo | `plotting.py` aplica la guía por defecto: fuente 20, sin título, notación 10ˣ, leyenda adentro y sin marco, un nombre por eje (constantes `AXIS_*`), EDMD azul y TSDMD rojo como la Figura 1 del enunciado | Guía 1.7–1.10, 2.4.6; Correcciones_TP3, D21–D29 |

### Resultados del estudio de dt (2026-11-09, sesión `output/dt-timestep/2026-11-09_18-11-29`)

TSDMD puro, k_t = 0, N = 200, M = 10, semillas 1–10, tope 1000 s.

| dt (s) | Completas | ⟨t_evac⟩ (s) | ⟨Q⟩ (p/s) | ξ_max (mm) | Wall-clock total del caso |
|---|---|---|---|---|---|
| 10⁻⁴ | 10/10 | 82 ± 3 | 2.8 ± 0.1 | 59 | 50 s |
| 2.5·10⁻⁴ | 9/10 | 82 ± 3 | 2.8 ± 0.1 | 54 | 45 s |
| 5·10⁻⁴ | 9/10 | 82 ± 3 | 2.8 ± 0.1 | 57 | 20 s |
| 10⁻³ | 10/10 | 84 ± 5 | 2.7 ± 0.2 | 56 | 6 s |
| 2.5·10⁻³ | 8/10 | 82 ± 2 | 2.8 ± 0.1 | 54 | 7.5 s |

Trabadas: el último cruce fue a los 35–73 s y no salió nadie más hasta el tope de 1000 s, lo que confirma que 1000 s alcanza. Con el criterio de 1σ (3.0 s), **pasan los cinco dt**: t_evac no distingue entre ellos. ξ_max ≈ 55 mm no depende de dt, así que es física (la multitud empujada por la fuerza de deseo comprime contra la puerta) y no error numérico. **El dt final lo decide el grupo.**

### dt y tope: elegidos (2026-11-09)

- **dt = 10⁻³ s** (confirmada por el grupo). Pasan los cinco dt; se elige 10⁻³ s porque es con el que se midió todo lo anterior (v_max, extremos del híbrido, garantía δ_in ≥ 2 v_max dt = 0.01 m < 0.05 m), queda 2.5 veces por debajo del mayor dt que pasa y cuesta ≈ 0.5 s por corrida. Es el default del motor y de todos los estudios.
- **Tope de 1000 s con N = 200** (confirmada). Para otros N (ítem d): tope = **1.5 s × N** (reemplaza los 5 s × N de esta primera versión; ver "Tope del ítem d" abajo).

### Estudios a–d y animaciones (2026-11-09)

Ninguna tiene precedente directo; son decisiones del grupo dentro de lo ya aprobado en `DESIGN.md`.

| Tema | Decisión | Por qué |
|---|---|---|
| `schemes` (a, b) | Los tres esquemas en una sola sesión, mismas semillas 1–10, N = 200, dt = 10⁻³ s, dt₂ = 0.5 s, híbrido con R_s = 5 m | Ítems a y b comparten las corridas; misma semilla entre esquemas (decisión 28) |
| Fracción por esquema (a) | Una realización con f_TS y f_ED = 1 − f_TS, y ⟨f_TS⟩(t) ± σ sobre las completas | "Evolución temporal de la fracción de partículas en cada esquema" [Enunciado, a]; una evolución típica [Guía 2.4.2] |
| Curvas de población (b) | Una realización por esquema y ⟨N(t)⟩ ± σ (banda) sobre las completas, en una grilla común | Ítem b pide las curvas de los tres esquemas |
| Comparación con TSDMD (b) | Δt_evac respecto de TSDMD puro, en segundos y en errores estándar combinados (σ/√M de cada uno) | "Tomando TSDMD puro como referencia, discutir las diferencias" [Enunciado, b] |
| `runtime` (c) | En serie, 2 corridas de calentamiento por caso en la misma JVM, dt₂ = 5 s. R_s ∈ {0, 1, 2, 3, 5, 8, 12, 20, 35, 56} m con t_s fijo. TSDMD puro y EDMD puro como bandas ⟨·⟩ ± σ en las figuras vs R_s | Decisión 27 y ★22; "indicando los valores de TSDMD puro y EDMD puro como referencia" [Enunciado, c] |
| Wall-clock de las trabadas | Afuera del promedio (corrieron hasta el tope); se listan | No son comparables con las que terminan |
| ⟨f_TS⟩ vs R_s | Promedio temporal de f_TS por realización, con los cuadros cada 5 s | Muestra la meseta prevista en `DESIGN.md` 2.6 |
| `scaling` (d) | N ∈ {100, 200, 300, 500, 700, 1000}; variante densidad: L = √(N/0.2); variante L = 50 m; M = 3; 1 calentamiento; tope 1.5 s × N; wall-clock en log-log (M y tope corregidos: ver "Ítem d: M = 3" y "Tope del ítem d" abajo) | Decisión 29; Guía 2.4.7 |
| Animaciones | Una por esquema, dt₂ = speed/fps = 5/25 = 0.2 s. Semilla 1 en TSDMD y EDMD, **2 en el híbrido** (la 1 se traba con dt₂ = 0.2 s: el video sería casi todo el atasco quieto) | Ítem a; la realización es ilustrativa |
| Colores | Azul EDMD, rojo TSDMD, por agente y por cuadro (columna `scheme`); los retirados no se dibujan; círculo punteado = zona R_s | Figura 1 del enunciado |
| Fotograma | Híbrido: primer cuadro con f_TS ≥ 0.5 (conviven los dos colores); puros: cuando cruzó un cuarto de los agentes. Sin texto sobre la figura | Guía 1.7 y 2.4.8 |
| Versión en vivo | PDF de hasta 60 páginas (`figures/anim_lite/<nombre>.pdf`), lo que espera `\figanim` del template | TP4_Presentacion.tex L133–139 |

### Tope del ítem d: 1.5 s × N (2026-11-09, confirmada por el grupo)

En `scaling` el tope del tiempo simulado es **1.5 s × N** (parámetro `--cap-per-agent`), en vez de 5 s × N. Es 3–4 veces el t_evac esperado (N/Q más el recorrido) y alcanza para reconocer un atasco por el último cruce. El wall-clock de una corrida trabada no se usa, así que un tope más largo solo cuesta tiempo: con 5 s × N, una trabada de N = 700 con 491 agentes adentro tardó 190 s, contra 5 s de una completa. El tope de los ítems a–c sigue en 1000 s con N = 200.

### Ítem d: M = 3 y N = 500 compartido (2026-11-09, confirmada por el grupo)

- **M = 3 en todo el ítem d** (`--realizations`). El enunciado no fija M para el ítem d (solo pide "al menos 10" en el b). EDMD escala mucho peor que lineal: con N = 500 y L = 50 m, 40–190 s por corrida completa (3–14·10⁶ eventos). Con M = 10 el estudio pasaba las 10 horas. Con M = 3 se pueden revisar los observables hoy; se sube M después si hace falta. En la diapositiva se aclara M = 3 al costado.
- **N = 500 se simula una sola vez:** √(500/0.2) = 50 m, así que las dos variantes son la misma sala. El análisis usa ese caso en las dos.
- **Sin tope de eventos:** una corrida cortada por eventos no es una "simulación completa" y sacaría justo las EDMD más caras del promedio (sesgo hacia abajo). `--max-events` queda en su default.
- Lo que no ocupa disco son los eventos: `dynamic.txt` se escribe cada dt₂ = 5 s, como el `--dump-every` del TP3 (que contaba eventos) pero a dt₂ fijo, como pide el enunciado.

### Ítem d: EDMD solo hasta N = 500 (2026-11-09, confirmada por el grupo)

EDMD con N = 700 no terminó ni la corrida de calentamiento en 12 minutos de CPU, y el proceso ya ocupaba 4.6 GB de RAM (la cola de eventos crece en la multitud). Medido hasta N = 500: 1.4 s (N = 200), 5–11 s (N = 300), 32–190 s (N = 500); el costo crece más o menos como N⁴–N⁵. Proyectado: unas 3 horas por caso con N = 1000, más de 6 horas en total.

- **EDMD hasta N = 500** en las dos variantes (`--edmd-max-n 500`); TSDMD y el híbrido hasta N = 1000.
- El costo prohibitivo de EDMD con N grande es un resultado del ítem d y se informa como tal.
- Si hace falta, EDMD con N = 700 y 1000 va en una sesión aparte, de noche y con `--warmup 0`: con corridas de minutos el calentamiento de la JVM no cambia nada.

### Ítem d: v_max = 8 m/s en el híbrido (2026-11-09, confirmada por el grupo)

> **Eliminado (2026-11-24):** v_max ya no existe (ver "Etapa 10").

Con v_max = 5 m/s (H1), el híbrido cortó por la garantía de la piel en tres casos: densidad constante con N = 1000, y L = 50 m con N = 700 y 1000. Un agente TSDMD llegó a 5.01–5.02 m/s cerca de t = 35 s: con multitudes grandes la presión en la puerta "dispara" agentes.

- **v_max = 8 m/s en todo el híbrido del ítem d** (`--v-max` del estudio; el CLI lo pasa al motor solo para el híbrido). La garantía sigue: δ_in = 0.05 m ≥ 2 v_max dt = 0.016 m. El horizonte seguro es más corto, así que hay algunos chequeos de piel más.
- Se rehicieron los 11 casos del híbrido del ítem d con 8 m/s, para que todo el ítem use el mismo valor. Los ítems a–c siguen con 5 m/s (con N = 200 el pico medido es 3.29 m/s).
- `EngineRun` tiene el campo `v_max`; un `case.json` viejo sin ese campo se toma como `v_max = None` (default del motor), así que las sesiones anteriores siguen valiendo.

### Carpetas por ítem y figuras al estilo del TP4 (2026-11-09, pedido del grupo)

**Carpetas.** `output/<ítems>-<estudio>/`, como en el TP4 (`2.1a-timestep`): `dt-timestep`, `ab-schemes`, `c-runtime`, `d-scaling`, `a-animation`. Las sesiones existentes se renombraron con `mv`, sin volver a simular.

**Figuras como en el TP4, salvo lo que corrigieron las devoluciones:**

| Tema | Decisión | Fuente |
|---|---|---|
| Ejes | Palabras + símbolo + (unidad), como en el TP4: "Tiempo $t$ (s)", "Paso temporal $\Delta t$ (s)", "Número de agentes $N$". Los promedios con ⟨·⟩: "Tiempo de evacuación ⟨t_evac⟩ (s)" | TP4 (`timestep.py`, `density.py`); Guía 1.8 |
| Barras de error | μ ± σ (M − 1), marcador + barra con capsize 3, línea recta solo como guía (fmt "o-"); en eje logarítmico una barra que llegaría a ≤ 0 conserva solo la mitad de arriba | TP4 (`plotting.py`, `timestep.py`); Guía 2.4.6 |
| Evoluciones promedio | ⟨·⟩(t) con banda ±σ (alpha 0.2) | TP4 (`density.py`) |
| Puntos con atascos | Marcador **vacío** = promedio de las corridas completas, porque alguna se trabó; **siempre con la entrada "Con atascos" en la leyenda** | TP4 (marcador vacío) corregido por Correcciones_TP4, D30 ("marcador vacío sin leyenda") |
| Agentes adentro | **N_in(t)** (antes N(t)); f_TS = N_TS/N_in | Correcciones_TP4, D12/D14: N ya es el número de agentes |
| Tiempo de ejecución | **T_ej** | Correcciones_TP4, "Para el TP5": no usar t para el tiempo simulado y para el de ejecución; τ ya es la relajación del SFM |
| Fracción media en TSDMD | f̄_TS = promedio temporal de f_TS en una realización; en la figura ⟨f̄_TS⟩ | Un símbolo por cantidad |

## Etapa 6: presentación (2026-11-09)

### Animaciones y evoluciones de c y d

La guía (2.4.1–2.4.2) y el enunciado piden, para **cada** estudio, animación → evolución temporal → observable escalar. Faltaban las de los ítems c y d, así que el estudio `animation` pasó a tener escenas (`output/acd-animation/`):

| Escena | Ítem | Esquema | Semilla |
|---|---|---|---|
| `tsdmd`, `edmd` | a | puros, N = 200 | 1 |
| `hibrido` | a | R_s = 5 m | 2 |
| `hibrido_Rs0`, `hibrido_Rs20` | c | R_s = 0 y 20 m: la meseta y una zona ancha. **Reemplazadas (Etapa 10)** por `hibrido_ts_chico` y `hibrido_ts_grande` (t_s = 0.01 y 1 s) | 2 |
| `N100`, `N1000` | d | híbrido, L = 50 m (el v_max = 8 m/s se eliminó en la Etapa 10) | 1 y **4** (con N = 1000 las semillas 1–3 se traban) |

Además genera las evoluciones de c (f_TS(t) de R_s = 0 y 20 m) y de d (N_in(t) de N = 100 y 1000), una realización cada una.

### `presentation/main.tex`

| Tema | Decisión | Fuente |
|---|---|---|
| Preámbulo | L7–163 del `.tex` del TP4 tal cual; título, notación del TP5 y `\algorithmicwhile` agregados después | Prompt inicial, sección 9 |
| Introducción (3) | Sistema real; SFM sin fuerza social en forma general (con k_t; k_t = 0 va en Simulaciones); colisión de EDMD y recuperación de v_d | Guía 2.1; consideración i de la cátedra |
| Implementación (7) | UML en TikZ sin observables (clases reales del motor); TSDMD con las fórmulas de Beeman que usa `BeemanIntegrator`; EDMD; cuatro diapositivas del híbrido (E1–E3, S1–S3 y extremos, sincronización y garantías, pseudocódigo) | Enunciado, a; Correcciones_TP4, D7 y D8 |
| Simulaciones (3) | Esquema de la sala (como la Figura 1) con tabla de parámetros fijos y del método; parámetros variables en viñetas, sin tabla de estudios; observables con su definición, M_c y cómo se tratan los atascos | Guía 2.3; Correcciones_TP3, D13; Correcciones_TP4, D11, D12 y D30 |
| Resultados | dt; a: animaciones → fracción → ⟨f_TS(t)⟩; b: ⟨N_in(t)⟩ → ⟨t_evac⟩; c: animaciones → f_TS(t) y ⟨f̄_TS⟩ → ⟨T_ej⟩ y ⟨t_evac⟩ vs R_s; d: animaciones → N_in(t) → ⟨T_ej⟩ vs N | Guía 2.4.1–2.4.4; Correcciones_TP3, D20 |
| Pendiente | Solo los 7 links de YouTube (`\pend{link de YouTube}`) | — |
| Conclusiones | Borrador basado solo en lo mostrado; **lo revisa el grupo** | Guía 2.5 |

---

## Etapa 7: notas del profesor sobre la presentación (2026-11-13)

Once notas sobre el PDF de la presentación. Todas son indicaciones del profesor, salvo que se diga otra cosa.

| # | Nota | Decisión | Estado |
|---|---|---|---|
| P1 | "No podemos mostrar variables antes de presentarlas formalmente" | Cada variable se define en la diapositiva donde aparece por primera vez (SFM, criterios, chequeo de piel, casos extremos). Los valores siguen en Simulaciones | Hecha |
| P2 | Separar E1–E3 en tres diapositivas | Una diapositiva por condición, con su fórmula, qué detecta y sus variables. S1–S3 igual, para que quede parejo. El chequeo de piel va pegado a E2 | Hecha |
| P3 | "¿Cuántos saltos hay del time step al event driven?" | Observable nuevo, en el análisis: cambios de esquema de cada agente entre cuadros seguidos, en cada sentido. Ver abajo | Hecha |
| P4 | Animaciones con pocas partículas para mostrar las tres condiciones | Ver "P4" abajo | Confirmada (2026-11-13) |
| P5 | El Δt de TSDMD no debería dictar cuándo se disparan los eventos: reloj global separado | Ver "P5" abajo | Confirmada (2026-11-13) |
| P6 | Revisar que haya eventos de cruce de umbral, de cambio de estado y de alineación con el deseo | Ya existían: Zona y Piel (umbrales), Progreso y Vano (estado), Bounce back (alineación). Quedan en dos tablas de eventos en la presentación | Hecha |
| P7 | "S2" (sobre el chequeo de piel) | Interpretación del grupo: falta explicar cómo se chequea S2. Va en su diapositiva: al final del paso, solo si ya valen S1 y S3, mirando las 3×3 celdas | Hecha; a confirmar la interpretación |
| P8 | t_f en los parámetros variables | Tabla de símbolos (N, M, R_s, L, t_f) en vez de viñetas. t_f ≤ 10³ s con N = 200 | Hecha |
| P9, P10 | T_ej vs R_s: mejorar la escala; versión sin EDMD | Figura nueva `wall_clock_vs_rs_no_edmd`: misma figura sin la banda de EDMD, en una diapositiva siguiente | Hecha |
| P11 | "Darle más tiempo" (T_ej vs R_s) | Interpretación del grupo: más tiempo de exposición en esa diapositiva. No cambia archivos | A confirmar la interpretación |

### P3: saltos entre esquemas

`observables.scheme_switches`: cuenta, para los agentes no retirados, los cambios de la columna de esquema de `dynamic.txt` entre dos cuadros seguidos. El esquema inicial no cuenta como salto. Con cuadros cada Δt₂, un salto y su vuelta dentro del mismo intervalo no se ven: es una cota inferior. Se calcula en el análisis, nunca en el motor [Correcciones_TP4, Código].

**Medido** (sin volver a simular): en a–b (N = 200, R_s = 5 m, M = 10, Δt₂ = 0.5 s), **197 ± 1 saltos de EDMD a TSDMD y 0 de vuelta** por corrida. Los agentes que faltan para 200 ya arrancan en TSDMD. En el barrido de c (Δt₂ = 5 s) tampoco vuelve nadie con ningún R_s. Cada agente cambia de esquema una sola vez: S2 y S3 casi nunca se cumplen juntos fuera de la zona, como ya se había observado en la etapa 4.

### P5: reloj global separado del paso de TSDMD

**Problema:** hoy el híbrido avanza de a Δt (`HybridScheme`, bucle principal): procesa los eventos hasta t_n + Δt y después da un paso de Beeman. Los eventos ocurren en su instante exacto, pero el reloj del sistema es el de TSDMD. Lo hace aunque no haya ningún agente TSDMD, y los pasajes esperan hasta el borde del paso.

| Opción | A favor | En contra |
|---|---|---|
| **Paso TSDMD como evento de la cola (elegida)** | El reloj global es la cola de eventos. Un evento `Step` en t_n + Δt integra a los TSDMD y agenda el siguiente; solo existe mientras haya agentes TSDMD. Sin TSDMD, el híbrido avanza de evento en evento, como EDMD puro. Δt queda como parámetro solo del integrador | Cambia `HybridScheme`, la sincronización de la piel y el orden de los empates. Hay que volver a correr a, b, c, d y las animaciones del híbrido |
| Dejarlo como está y explicarlo | Sin cambios de código; los eventos ya son exactos | El profesor lo marcó como incorrecto |
| Un reloj por agente TSDMD | — | Los TSDMD interactúan entre sí: tienen que integrarse juntos |

**Detalles de la opción propuesta:**
- **Empates:** un `Step` en t se procesa después de los demás eventos de ese mismo instante, como hoy.
- **Pasajes:** se aplican dentro del `Step`, con todos en el mismo instante. Si no hay ningún `Step` agendado, un pasaje marcado por E1 o E3 agenda uno en el instante del evento.
- **Extremos:** con R_s = 0 y t_s = 0 nunca hay `Step`, así que tiene que dar bit a bit EDMD puro. Con todos en TSDMD desde t = 0 no hay eventos de agentes, así que tiene que dar bit a bit TSDMD puro. El test de extremos lo verifica.
- **Garantía de la piel:** sin cambios. Los TSDMD siguen moviéndose de a Δt, y el horizonte t + (g − δ_in)/(|v_i| + v_max) vale igual.

### P4: animaciones de las tres condiciones

| Opción | A favor | En contra |
|---|---|---|
| **Estado inicial desde archivo (elegida)** | Flag del motor `--initial <archivo>` con id, x, y, vx, vy, R y esquema. Tres escenas armadas a mano, una por condición: E1 (agentes que entran a la zona), E2 (un EDMD que se acerca a un TSDMD y pasa antes de tocarlo), E3 (choques más seguidos que t_s contra la pared al lado de la puerta) | Código nuevo en el motor (lectura) y en el CLI (escenas) |
| Buscar semillas con N chico donde se vea cada condición | Sin código nuevo en el motor | No se controla qué pasa; puede no aparecer una condición aislada |

Las escenas usan los mismos parámetros que el resto. Son demostrativas: no se usan para ningún observable.

### P5: implementación y verificación (2026-11-13)

| Pieza | Cómo quedó |
|---|---|
| Orden en el mismo instante | `Event.rank()`: 0 eventos de agentes, 1 `Step`, 2 escritura. Antes: tiempo y orden de creación |
| `Step` | `Event.Step` en n·dt, con n entero (la misma expresión que el conteo de pasos de TSDMD puro). No cuenta para `--max-events` |
| Sin TSDMD | No hay `Step`. Una marca (E1, E2 o E3) con ningún `Step` pendiente agenda el primero de la grilla n·dt no anterior al evento |
| Escritura del híbrido | Pasa a ser un evento, como en EDMD puro. Avanza solo a los EDMD: los TSDMD ya están en ese instante por el orden |
| Corte por `--max-events` entre dos `Step` | El cuadro final se escribe solo si todos los agentes están en el mismo instante |

**Verificación:** los 1 044 640 chequeos de `EngineChecks` pasan, incluidos los dos extremos bit a bit. Además, con las semillas 1–3 de a–b, `static.txt`, `dynamic.txt` y `exits.txt` dan **idénticos byte a byte** a los del motor anterior en los tres esquemas: el orden de los eventos no cambió, solo quién lleva el reloj. Por eso t_evac, N_in(t) y f_TS no cambian. Cambian `events` (las escrituras del híbrido ahora son eventos, como en EDMD puro) y T_ej: con la semilla 1, 0.59 s contra 0.83 s. Se vuelven a correr a–b, c, d y las animaciones.

**Saltos:** el conteo en el análisis es una cota inferior. Los chequeos del motor, que miran cada paso, ven 5 vueltas TSDMD → EDMD en una corrida con R_s = 0 que cada Δt₂ no se ven.

### P4: implementación (2026-11-13)

> **Ojo (2026-11-24):** las escenas `criterio_E1` y `criterio_E2` se eliminaron. Queda `criterio_E3`, que ahora muestra el único criterio: los primeros pasan por contactos seguidos, después por tocar a un TSDMD, y vuelven tras t_s sin contacto. El nombre se cambia cuando se actualice la presentación.

`--initial <archivo>`: una línea `x y R` por agente; N tiene que coincidir con la cantidad de líneas. Se valida como el sorteo: adentro de la sala, sin superposiciones y con radios en [0.18, 0.25] m. v(0) = v_d ê, como siempre.

Tres escenas en el estudio `animation`, sala de 10 m, R = 0.2 m, 1 s simulado por segundo de video:

| Escena | Configuración | Verificado en la salida |
|---|---|---|
| `criterio_E1` | 6 agentes separados; R_s = 3 m; t_s = 0 (sin E3) | Los 6 pasan en r ≈ 2.96–2.99 m, sin ningún TSDMD a menos de 0.3 m |
| `criterio_E2` | 13 agentes atascados en la zona (R_s = 1.5 m) y 24 detrás; t_s = 0 | 13 pasan **afuera** de la zona (r = 1.6–1.96 m): solo puede ser E2 |
| `criterio_E3` | 11 agentes que llegan a la puerta pegados a la pared; R_s = 0 | Los 4 primeros pasan sin zona y sin TSDMD cerca: solo puede ser E3; después, contagio |

### Cómo defender el costo del híbrido y el radio R_s (2026-11-13)

> **Reemplazada (2026-11-24).** R_s, E2 y la piel ya no existen. La defensa vigente está en "Etapa 10, Cómo defenderlo".

Explicación para la exposición, verificada contra el código (`HybridScheme`, `EventCore`) y contra las mediciones del ítem c con el motor nuevo.

**Qué se calcula en cada paso de TSDMD (evento `Step`):**

- Solo Beeman de los agentes TSDMD: fuerzas entre TSDMD (nunca hay contacto cruzado) y actualización de sus celdas.
- Los agentes EDMD **no se tocan**: no se mueven ni se les predicen choques nuevos. Su posición sale de la recta r(t₀) + v·(t − t₀) y se calcula recién en su próximo evento o en la escritura (avance perezoso, decisión 16).
- Solo los EDMD vecinos de un TSDMD hacen chequeo de piel, y lo agendan en su horizonte seguro t + (g − δ_in)/(|v_i| + v_max), no en cada paso. Se reprograma solo cuando un TSDMD entra a una celda nueva o un agente pasa a TSDMD.
- El costo de un paso crece con N_TS, no con N. Los eventos solo son de los agentes EDMD.

**Qué pasa con lo predicho hacia un agente que pasa a TSDMD:**

- Cuando j pasa a TSDMD, su contador sube (`revision++`): **todos los eventos predichos con j quedan invalidados**. El motor nunca predice un choque contra un TSDMD.
- Un EDMD i que iba a chocar con j conserva el resto de sus eventos: paredes, otros EDMD, fantasma, zona y piel.
- **i no puede tocar a j**: el chequeo de piel lo pasa a TSDMD antes de quedar a δ_in de j (E2, garantizado por δ_in ≥ 2·v_max·Δt; el motor lo verifica y corta con error si no se cumple).
- Si i va hacia la puerta, además cruza R_s y pasa por E1, aunque nunca llegue a acercarse a j.
- **El radio solo no alcanza:** j puede estar fuera de la zona (pasó por contagio o por E3). La garantía de que no haya choque entre un agente duro y uno blando la da E2, no R_s.

**Lo que no hay que decir:**

| Afirmación | Por qué no | Qué decir |
|---|---|---|
| "En cada Δt se actualiza la posición de todas las partículas" | Los EDMD no se mueven en el paso; se avanzan solo en sus eventos y en la escritura | "En cada paso solo se integran los TSDMD; los EDMD siguen en su recta sin costo" |
| "El híbrido es muchísimo más rápido que TSDMD" | Ítem c, N = 200: TSDMD puro 0.52 ± 0.03 s, híbrido 0.53–0.63 s según R_s, EDMD puro 1.4 ± 0.5 s. Por E3, la multitud pasa casi entera a TSDMD (f̄_TS ≥ 0.73 con cualquier R_s) | "El híbrido evita el costo de EDMD en la multitud (le gana a EDMD puro) y cuesta como TSDMD puro, porque casi todos los agentes terminan en TSDMD" |

---

## Etapa 8: revisión final (2026-11-15)

### ⟨f̄_TS⟩ solo sobre las corridas completas (confirmada por el grupo)

**Problema:** `runtime.py` y `schemes.py` promediaban f̄_TS sobre **todas** las corridas, incluidas las trabadas. Era la única excepción a la regla de "Corridas sin terminar en el análisis" (etapa 5): t_evac, T_ej y la curva ⟨f_TS(t)⟩ ya usaban solo las completas, y la presentación define ⟨X⟩ y σ_X "sobre las M_c realizaciones completas" para todo X. Efecto visible: el pico de ⟨f̄_TS⟩ en R_s = 2 m (0.83 ± 0.13), donde se trabaron 4 de 10 corridas. Re-analizado solo sobre las completas, ese punto da 0.74 ± 0.02, como sus vecinos.

**Por qué una trabada no entra al promedio:** pasa unos 35–65 s evacuando y después queda quieta hasta el tope con f_TS = 1. Su f̄_TS da 0.978 con cualquier R_s, y 1.000 con un tope de 10⁵ s: lo fija el tope, no el criterio de pasaje. No es una cota, como el t_evac cortado del TP4 [Correcciones_TP4, D30: "los valores están acotados por el tiempo de simulación"].

**Sesgo de selección, medido:** excluir corridas solo sesga si las trabadas pasaban agentes a TSDMD distinto *antes* de trabarse. Se comparó cada trabada con las completas del mismo R_s, promediando f_TS sobre la misma ventana [0, último cruce de la trabada] (N = 200, M = 10, semillas 1–10; corridas de prueba de esta revisión, no de las sesiones de los estudios):

| Caso | Ventana | Trabada | Completas, misma ventana |
|---|---|---|---|
| a/b, R_s = 5 m | 0–36 s | 0.486 | 0.486 ± 0.020 |
| a/b, R_s = 5 m | 0–47 s | 0.599 | 0.611 ± 0.016 |
| R_s = 0 | 0–51 s | 0.591 | 0.595 ± 0.011 |
| R_s = 2 m | 0–45 s | 0.555 | 0.558 ± 0.014 |
| R_s = 2 m | 0–51 s | 0.595 | 0.599 ± 0.013 |
| R_s = 2 m | 0–54 s | 0.589 | 0.599 ± 0.013 |
| R_s = 3 m | 0–63 s | 0.653 | 0.663 ± 0.011 |

Las siete trabadas caen a menos de 1σ de las completas: hasta trabarse evolucionan igual. El atasco es un arco en la puerta, independiente del criterio. El sesgo es del orden de 0.01, menor que el σ de cada punto.

| Opción | Resultado (R_s = 0 / 2 / 3 m) | Problema |
|---|---|---|
| Todas, hasta el tope (antes) | 0.77 ± 0.08 / 0.81 ± 0.12 / 0.76 ± 0.08 | El valor de las trabadas lo fija el tope |
| **Solo completas (elegida)** | 0.74 ± 0.02 / 0.74 ± 0.01 / 0.74 ± 0.02 | Sesgo de selección, medido y despreciable |
| Todas, hasta el último cruce | 0.73 ± 0.05 / 0.69 ± 0.08 / 0.73 ± 0.03 | Ventanas de distinto largo: la trabada solo cubre la subida de f_TS |

**Diferencia con t_evac:** ahí el sesgo sí existe y no se puede sacar (una trabada tarda más por definición). Por eso los puntos con atascos llevan marcador hueco y la leyenda "Con atascos"; desde esta revisión, también los de ⟨f̄_TS⟩.

**Cambios:** `runtime.py` y `schemes.py` promedian f̄_TS sobre las completas; en las figuras vs R_s el marcador hueco ahora vale para los tres observables.

### Figuras nuevas: solo re-análisis, sin volver a simular (confirmada por el grupo)

Las figuras de la presentación salen de las sesiones originales de `output/`, que están en la máquina de un integrante. **No se vuelve a simular:** se re-analizan esas mismas sesiones con el código corregido (`--analyze-only`), así los números de las diapositivas y de las conclusiones siguen siendo los de una sola tanda.

Por qué no simular de nuevo: en la revisión se corrieron `timestep`, `schemes`, `animation` y `runtime` en otra máquina y **no reproducen bit a bit** las originales.

- TSDMD puro sí: la figura de Δt dio idéntica.
- EDMD y el híbrido no: la dinámica es caótica y una diferencia de redondeo entre máquinas (por ejemplo en `Math.exp` de la relajación) cambia las trayectorias y qué semillas se traban. El híbrido de a–b terminó 8 de 10 corridas en vez de 9, y la escena `N1000` (semilla 4) se trabó.
- T_ej depende de la máquina y de su carga: dio unas 2 veces los valores originales.

Qué cambia al re-analizar:

| Estudio | Figura | Cambio |
|---|---|---|
| `runtime` (c) | `ts_fraction_vs_rs` | ⟨f̄_TS⟩ solo sobre las completas, con marcador hueco (sección anterior) |
| `scaling` (d) | `wall_clock_vs_n_*`, `evacuation_vs_n_*` | Mismo eje y en las dos variantes, que van lado a lado [Correcciones_TP4, D23]. M sigue en 3 |

El resto de las figuras de c y d se regenera con los mismos datos y queda igual. Δt, a, b y las animaciones no se tocan.

**Aplicado (2026-11-15)** sobre las sesiones `c-runtime/2026-11-13_15-09-41` y `d-scaling/2026-11-13_15-23-29`.
- Las sesiones del 09/11 y del 13/11 tienen las mismas corridas: a–b 30 de 30, c 120 de 120 y d 71 de 71 coinciden en `exits.txt` o en completas, t_evac y último cruce. Solo cambia T_ej. Por eso se pueden usar figuras de las dos fechas.
- Cambiaron solo `c/ts_fraction_vs_rs.png` (meseta 0.73–0.74 para R_s ≤ 3 m, sin el pico) y las figuras `d/*_vs_n_*`. `c/evacuation_vs_rs.png` y las de T_ej salieron idénticas byte a byte.
- A la copia de c le faltaba `dynamic.txt` en 11 corridas: son exactamente las 11 trabadas, que el promedio sobre las completas no lee.

**Ojo:** `simulation figures` copia la última sesión de cada estudio. Con la opción `--session` se elige la original; no correrlo después de simular en otra máquina.

## Etapa 9: recuperatorio (2026-11-20)

La defensa no se aprobó por la explicación de EDMD: demasiado compleja y difícil de seguir con fórmulas apiladas. El profesor pidió explicar el choque con dibujos, cada vector antes y después.

### Choque de EDMD: elástico, como en el TP3 (decisión del grupo, ya en el motor)

| Opción | Ventajas | Desventajas |
|---|---|---|
| Inelástico, c_n sacado del SFM (0.94 / 0.92) y piso v_sep (antes) | Más cerca del rebote blando de TSDMD | Dos parámetros derivados y un piso ad hoc; difícil de justificar y de explicar |
| **Elástico, c_n = 1, masas iguales (elegida)** | Es el choque de la Teórica 3 y del TP3; sin parámetros; se explica con un dibujo | Rebotes más vivos que en TSDMD; necesita masas iguales (el motor lo verifica) |

**Por qué:** simplificar el mecanismo que había que defender. La recuperación de v_d (fantasmas cada R/max(|v|, v_d), bounce back con ε_b = 0.01) no cambia. Sin v_sep, dos agentes pueden quedar en contacto con rebotes lentos: el motor acepta superposiciones de redondeo (10⁻⁹ m) y los separa con un choque en t = 0. Desaparecen las opciones `--e-pair`, `--e-wall` y `--v-sep`.

**Ojo:** las figuras y los números de Resultados y Conclusiones son de corridas con el choque anterior. Hay que volver a simular antes de presentar.

### Presentación (decisión del grupo)

- La teoría y las ecuaciones de EDMD van a la Introducción: cuándo chocan (|Δr + Δv t_c| = R_i + R_j), cómo chocan (normal y tangencial, J, después), paredes y bordes, fantasmas (trayectoria y diferencia con v_d ê en el tiempo, en escala log).
- Los dibujos usan valores calculados con las reglas del motor (comentados en el `.tex`), no inventados.
- Colores fijos, paleta Okabe-Ito (apta para daltónicos): agente i azul, j naranja, normal verde azulado, tangencial púrpura rojizo, v_d ê celeste. Impulso y fuerza de contacto en negro: el bermellón se confunde con el naranja de j con deuteranopia.
- Anexo después de "Muchas gracias": SFM completo, tiempos de choque, J general y forma del código, choques durante la recuperación, Beeman. Cada diapo del cuerpo tiene un botón al anexo y el anexo, uno de vuelta.
- Implementación conserva solo arquitectura, algoritmos, eventos e híbrido. Se sacaron "Predicción de choques", "Restitución sacada del SFM", "Eventos fantasma" y "Integrador" (están en la Introducción o en el Anexo).

### Correcciones de la cátedra (2026-11-23), solo presentación

Texto literal en `sources/correcciones/Correcciones_TP5_Hibrido.md`. Los criterios de pasaje (uno solo o tres condiciones, elegir uno en el ítem c) **quedan a decidir por el grupo**: en la presentación no se cambió su contenido. **Decidido el 2026-11-24:** ver "Etapa 10".

- **El híbrido va a la Introducción:** diapositiva nueva con la idea (EDMD lejos, TSDMD en la multitud, alguna de E1–E3 / todas S1–S3, una sola cola de eventos con el paso Δt como evento), y después E1, E2, E3 y S1–S3.
- **Menos diapositivas** (52 → 41 páginas hasta "Muchas gracias", sin contar overlays):
  - El choque, antes y después, en un solo frame.
  - S1, S2 y S3, que se cumplen todas, en una sola diapositiva.
  - Algoritmos de TSDMD y EDMD en una sola diapositiva; eventos de EDMD y del híbrido en una sola tabla.
  - Sincronización y costo en una diapositiva.
  - Observables: de tres diapositivas a dos.
  - Ítem b: N_in(t) y t_evac lado a lado; ítem c: T_ej con y sin EDMD lado a lado.
  - Al Anexo: la recuperación en el tiempo (escala log), el chequeo de piel y las predicciones hacia un agente que pasa a TSDMD.
  - Se sacó la diapositiva "Condiciones de transición" (la absorbe la del híbrido).
- **Animaciones más grandes:** alto 0.66 del texto en lugar de 0.50 (largo `\altoanim`) en a, híbrido, c y d.

## Etapa 10: un solo criterio de pasaje (2026-11-24, decisión del grupo, ya en el motor)

**Corrección de la cátedra** (`Correcciones_TP5_Hibrido.md`): "En caso que sean 3 criterios distintos, por favor limitarse a un solo criterio". Teníamos tres, unidos por "o": E1 (zona), E2 (contagio) y E3 (tiempo de vuelo). La vuelta exigía S1 (fuera de la zona más h), S2 (gap ≥ δ_out) y S3 (velocidad relajada). El grupo también pidió sacar todo lo que fuera complejidad innecesaria.

### ★21: el criterio que queda es el tiempo entre contactos

| Opción | A favor | En contra |
|---|---|---|
| E1, zona R_s | Fácil de explicar; daba los dos extremos exactos | No detecta una multitud fuera de la zona; si la multitud supera R_s, quedan duros contra blandos |
| E2, contagio | Local | Solo propaga: necesita otro criterio que cree el primer TSDMD |
| **E3, tiempo entre contactos (elegida)** | Es la condición de validez de EDMD: t_vuelo ≫ t_choque [Teorica_3, diapo 5]. Detecta la multitud donde se forme. No cuesta nada: se evalúa en cada choque | El primer choque de cada agente siempre es duro. Ningún t_s da TSDMD puro |

**Por qué:** ya estaba medido que E3 sola alcanzaba. Con R_s = 0, la multitud pasaba a TSDMD por E3: es la meseta del ítem c, con f̄_TS de 0.73–0.74 para cualquier R_s ≤ 3 m. E1 no agregaba nada.

### Convivencia: qué pasa cuando un EDMD toca a un TSDMD

**Problema:** E2 era lo que impedía que un EDMD tocara a un TSDMD. El motor no predice ese choque, porque la trayectoria de TSDMD no se conoce de antemano. Sin E2, un EDMD atravesaría a un TSDMD, y el enunciado pide explicar justamente esta convivencia.

| Opción | A favor | En contra |
|---|---|---|
| **Contacto ⇒ TSDMD, visto en el `Step` (elegida)** | Sin parámetros. Es el mismo mecanismo con el que TSDMD ve sus propios contactos: en cada paso. Se borran la piel, el horizonte seguro, los eventos `Skin` y v_max | El EDMD se superpone hasta (\|v_e\| + \|v_t\|)·dt antes del `Step`: unos mm, con un máximo medido de 2.4 mm. La fuerza de contacto arranca hasta 1 dt más tarde que en TSDMD puro |
| Mantener la piel δ_in como "garantía" | Cero superposiciones | Es E2 con otro nombre |
| Choque duro EDMD–TSDMD | Simétrico con ED–ED | Hay que predecir contra una trayectoria de Beeman, y el impulso rompe el estado del integrador |

**Por qué no lo atraviesa:** entre dos `Step` el EDMD avanza unos 3–5 mm. Para cruzar a un TSDMD necesitaría recorrer R_i + R_j ≥ 0.36 m en un dt, es decir, más de 360 m/s. Esos mm son del orden de la superposición normal de TSDMD: la fuerza de deseo contra un contacto da 240 N / 1.2·10⁵ N/m ≈ 2 mm.

**Por qué es C3 y no C2:** no mira cercanía ni tiene parámetro, y solo actúa con contacto real. Un contacto con un agente blando dura, no es instantáneo: el próximo contacto está a 0 < t_s.

### Vuelta a EDMD: la recíproca de C3

| Opción | A favor | En contra |
|---|---|---|
| **t_s sin ningún contacto, ni con agentes ni con paredes (elegida)** | Misma magnitud y mismo umbral que C3: el híbrido tiene un solo parámetro. Vuelve sin tocar a nadie, por construcción. Lo que quede de desvío de velocidad lo relajan los fantasmas | Idas y vueltas en el borde de la multitud (ver "Medido") |
| Solo S3 (velocidad relajada) | Lo primero que pensó el grupo | **S3 no es la recíproca de C3:** mide la velocidad, no el tiempo entre contactos. Un agente relajado puede estar superpuesto. Si vuelve así contra un EDMD, el motor corta ("Overlapping agents approaching"); contra un TSDMD, vuelve a TSDMD en cada paso y la fuerza de contacto queda apagada de forma intermitente |
| S2 + S3, sin S1 | Cambio mínimo | Dos parámetros (δ_out, ε) sin relación con C3 |
| 2·t_s sin contacto (medida y descartada) | La mitad de idas y vueltas: 1085 contra 2174 por corrida | Mismo T_ej (1.40 contra 1.39 s): el tiempo extra en TSDMD lo compensa. Agrega un factor 2 sin base física |

### Modelo resultante

```
EDMD → TSDMD   en un contacto (agente, pared o borde): si pasó menos de t_s desde el anterior
               en cada Step: si se superpone con un TSDMD (en cadena)
TSDMD → EDMD   en cada Step: si no toca a nadie y su último contacto fue hace ≥ t_s
Único parámetro: t_s, por defecto π/ω = 0.057 s.  t_s = 0 es EDMD puro, bit a bit.
```

- **Una sola variable por agente, `lastContact`.** En EDMD guarda el instante de cada choque. En TSDMD, el de cada `Step` en que el agente se superpone con alguien o con una pared.
- **Al volver a EDMD no se resetea.** Ya pasó t_s, así que el primer choque nunca dispara C3 por sí solo.
- **Todos arrancan en EDMD.**

### ★22: el parámetro principal pasa a ser t_s

- **Barrido del ítem c:** t_s ∈ {0.001 (= dt), 0.003, 0.01, 0.03, 0.1, 0.3, 1, 3} s, más el valor por defecto π/ω, con el eje x en escala logarítmica. El x de cada punto se lee de `params.txt`, así Python no duplica π/ω.
- **t_s = 0 es EDMD puro** (verificado bit a bit). No va en el barrido, porque ya es una referencia.
- **Ningún t_s da TSDMD puro.** Todos arrancan en EDMD y pasan recién en su segundo contacto; quien no choca dos veces nunca pasa. Con t_s = 3 s, f̄_TS ≈ 0.8. TSDMD puro queda como banda de referencia, que es lo que pide el enunciado: "indicando los valores de los casos TSDMD puro y EDMD puro como referencia".
- **Se perdió un check propio:** el de híbrido = TSDMD bit a bit. La parte TSDMD del híbrido usa el mismo `ForceStepper` que `TimeStepScheme`.

### Implementación

| Qué | Cómo quedó |
|---|---|
| Borrado del motor | `SwitchingCriterion`, `ZoneSkinFlightCriterion`, `Event.Zone`, `Event.Skin`, `Agent.skinStamp`, `EventCore.watch`, el horizonte seguro y v_max. `lastCollision` pasa a llamarse `lastContact` |
| Flags | Salen `--Rs`, `--delta-in`, `--delta-out`, `--h`, `--eps` y `--v-max` (dan "Unknown flag"). Queda `--ts ≥ 0`. `params.txt` solo lleva `ts` |
| Grilla | Celdas ≥ 2 r_max = 0.5 m en los tres esquemas: δ_out era lo único que las agrandaba |
| `HybridScheme.switchSchemes` | (1) los marcados por C3 pasan a TSDMD; (2) `catchContacts`: cada TSDMD pasa a TSDMD a los EDMD vecinos con gap < 0, en cadena; (3) a cada TSDMD que ya lo era: si toca a alguien, `lastContact = t`; si no, y pasó t_s, vuelve |
| Modelo | `velocityDeviation` y `desiredSpeed` salen de la interfaz `PedestrianModel`: solo las usaba S3 |
| Checks | Nuevo `contactsWithTimeStepAreCaught`: en ningún paso queda un par ED–TS superpuesto, el pasaje se superpone ≤ 1 cm y los que vuelven no tocan a nadie. `loneAgentInHybrid` comprueba que un agente solo nunca pasa a TSDMD. Se borró `hybridWithFullZoneIsTimeStep`. **1 045 078 checks OK** |
| Python | `runtime` barre t_s (casos `hybrid_ts=default` y `hybrid_ts=<valor>`; figuras `*_vs_ts`). `movie`: se borraron `criterio_E1` y `criterio_E2`, y aparecen `hibrido_ts_chico` (0.01 s) y `hibrido_ts_grande` (1 s). `scaling` y `schemes` sin R_s ni v_max. `animation` sin el círculo de la zona |

### Bug latente del CIM, corregido

- **Cómo apareció.** La escena `criterio_E3` (L = 10 m) se quedaba sin memoria. Con celdas de 0.5 m, el eje de la puerta (x = 5) cae en un borde de celda. Un agente sobre ese borde con v_x ≈ 10⁻¹⁷ (queda así tras choques casi simétricos) tenía su `CellCross` 10⁻⁹ **s** después: no se movía nada y se lo volvía a archivar en la misma celda, en un bucle de Zenón.
- **Ya estaba antes:** con L = 50 m, x = 25 era borde también con la grilla vieja (66 columnas).
- **Corrección:** el margen pasa a ser una **distancia**, 10⁻⁹ m pasado el borde (`CellGrid.EXIT_MARGIN`). Un agente con velocidad despreciable simplemente no cruza. Lo cubre el check `agentOnACellBorderMovesOn`, que con el código viejo falla (10 000 eventos, no termina) y con el nuevo pasa (14 eventos).

### Medido (N = 200, otra máquina: solo vale para comparar)

Mismas semillas; "HEAD" es el híbrido anterior (E1 + E2 + E3, R_s = 5 m) con el choque del TP3.

| | Híbrido nuevo | Híbrido anterior |
|---|---|---|
| Completas (50 semillas, dt₂ = 5 s) | 41/50 | 43/50 |
| t_evac (10 semillas, dt₂ = 0.1 s) | 75–93 s | 77–88 s |
| ⟨f̄_TS⟩ | ≈ 0.62 | ≈ 0.78 |
| T_ej de las completas | 1.37 ± 0.15 s | 1.13 ± 0.13 s |
| Saltos TSDMD → EDMD por corrida (cada 0.1 s, cota inferior) | ~2150 | 0 |

- **Idas y vueltas:** unas 11 por agente. En la parte suelta de la cola, quien pasa 57 ms sin contacto vuelve a EDMD. Es lo que dice la recíproca de C3. Antes nadie volvía, porque S1 pedía alejarse 6 m de la puerta. El control es t_s mismo: con t_s = 0.57 s hay ~185 vueltas y con t_s = 3 s, ninguna.
- **T_ej:** el híbrido nuevo es un 20 % más lento que el anterior, por las idas y vueltas y la revisión de contactos de todos los TSDMD en cada `Step`. Juntar `catchContacts` y `touching` en una sola pasada da resultados idénticos bit a bit y solo un 5–10 % menos: se dejaron separadas, porque se leen mejor.
- **Referencias en la misma máquina** (`runtime --quick`, 2 corridas): TSDMD 0.9 ± 0.1 s, EDMD 0.72 ± 0.07 s. Con el choque del TP3, **EDMD puro ya no es el más caro con N = 200**. Hay que confirmarlo en el ítem c antes de escribir las conclusiones.
- **Atascos:** 9/50 contra 7/50, que es lo mismo. Con las mismas 10 semillas, escribiendo cada 0.1 s terminan 10/10 y escribiendo cada 5 s, 7/10. La escritura cambia el redondeo y la dinámica es caótica (ver Etapa 8): solo se comparan estadísticas, nunca corridas sueltas.
- **EDMD puro con la celda nueva:** 124–127 mil eventos y t_evac de 91–98 s. Es el mismo rango que con la celda de 0.75 m.

### Cómo defenderlo

| Pregunta | Respuesta |
|---|---|
| ¿Cuántos criterios? | Uno: el tiempo entre contactos comparado con t_s. Con menos de t_s entre contactos, el choque instantáneo de EDMD deja de valer y el agente pasa a TSDMD; tras t_s sin contactos, vuelve |
| ¿Qué pasa cuando un EDMD toca a un TSDMD? | Pasa a TSDMD en el próximo paso. El contacto se ve como lo ve TSDMD: revisando superposiciones en cada paso. Se superpone unos mm durante a lo sumo un dt |
| ¿No es el contagio de antes? | No. No mira cercanía ni tiene parámetro, y solo actúa con contacto real. Un contacto blando dura más que t_s: es el mismo criterio |
| ¿Por qué ningún t_s da TSDMD puro? | Todos arrancan en EDMD y pasan en su segundo contacto. TSDMD puro va como banda de referencia, que es lo que pide el ítem c |

**No decir:** "nunca hay superposición entre EDMD y TSDMD". Hay hasta unos mm durante a lo sumo un dt, como en TSDMD.

### Pendiente

- **Volver a simular `schemes`, `runtime`, `scaling` y `animation`,** una sola vez para el choque del TP3 y el criterio único. `timestep` no hace falta: la celda nueva solo cambia el redondeo de TSDMD.
- **Elegir los t_s** de `hibrido_ts_chico` y `hibrido_ts_grande` mirando el barrido, y revisar las semillas de las escenas.
- **Actualizar `DESIGN.md`** (§2.4, §2.6, §2.7), `notes/defensa_TP5.md` y la presentación: los criterios, el ítem c contra t_s, borrar E1/E2 y renombrar `criterio_E3`.

### Presentación adaptada al criterio único (2026-11-25, sin volver a simular)

- **Introducción:** las cuatro diapositivas E1, E2, E3 y S1–S3 pasan a ser una sola, "Criterio de pasaje: tiempo entre contactos" (entrada, vuelta, t_s = π/ω y la animación `criterio_E3`). En la diapositiva del híbrido se sacó el círculo de R_s y la tabla dice el criterio en los dos sentidos.
- **Implementación:** el UML pierde `SwitchingCriterion` y `ZoneSkinFlightCriterion`, y los eventos `Zone` y `Skin`. La tabla de eventos pierde Zona y Piel. En la sincronización, el contacto EDMD–TSDMD se ve en el paso (reemplazado en la Etapa 11: ahora se predice). El algoritmo del híbrido sigue a `switchSchemes`.
- **Simulaciones:** la tabla de métodos deja solo t_s en el híbrido (sin δ_in, δ_out, h, ε ni v_max). En la de parámetros variables, R_s pasa a ser t_s.
- **Resultados:** R_s = 5 m pasa a ser t_s = π/ω. El ítem c usa `*_vs_ts` y las escenas `hibrido_ts_chico` y `hibrido_ts_grande`. **Los números de corridas viejas quedan marcados con `\pend{?}`** (completas, saltos, Δt_evac, EDMD con N = 700, Conclusiones) hasta volver a simular.
- **Anexo:** se borró "Chequeo de piel". En las predicciones, el contacto se ve en el paso. En la tabla de la IA, las filas de v_max y δ_out se reemplazan por el bug del CIM.
- **Pendiente:** figuras y links de YouTube nuevos (los links actuales son los viejos), y llenar los `\pend`.

## Etapa 11: contacto entre esquemas predicho, cero superposición (2026-11-25, decisión del grupo, ya en el motor)

### El problema

El enunciado pide explicar "cómo se garantiza que **no se pierdan contactos** ni haya **superposiciones** en el pasaje de un esquema a otro". Con la Etapa 10 no lo garantizábamos:

- **De TSDMD a EDMD:** sí. Un agente solo volvía si no tocaba a nadie.
- **De EDMD a TSDMD:** no. El motor no predecía el choque de un EDMD contra un TSDMD: lo veía recién en el paso siguiente, cuando ya se superponían. Eran hasta (|v_e| + |v_t|)·dt, 2.4 mm medidos.

Se podía defender ("esos mm son del orden del contacto blando de TSDMD"), pero no cumple la letra del enunciado. Preferimos resolverlo desde el principio.

### La idea: la trayectoria de un TSDMD se conoce un paso hacia adelante

Decíamos "no se puede predecir contra un TSDMD porque su trayectoria no se conoce". **Un paso hacia adelante, sí se conoce.** En Beeman, la posición nueva solo usa el estado en t [Teorica_4, p. 20]:

```
r_j(t+dt) = r_j(t) + v_j(t)·dt + (2/3·a_j(t) − 1/6·a_j(t−dt))·dt²
```

La corrección cambia la velocidad, no la posición. Entonces, entre dos pasos, cada TSDMD recorre un **tramo** recto conocido, de r_j(t) a r_j(t+dt), con velocidad

```
u_j = (r_j(t+dt) − r_j(t)) / dt
```

Contra ese tramo, un EDMD predice su contacto **con la misma cuadrática de siempre** [Teorica_3, diapo 14]: |Δr + Δv·t_c| = R_i + R_j, con Δv = u_j − v_i. En los instantes del paso el tramo coincide exactamente con la posición de Beeman. Entre dos pasos, TSDMD no define posiciones, así que el tramo recto es la interpolación natural.

### El criterio, como una sola regla (★21, sin cambios en su esencia)

Sigue siendo **uno solo**: el tiempo entre contactos Δt_i, comparado con t_s. Lo que se precisa es cuánto vale Δt_i según con qué es el contacto:

```
          ⎧ t − t_i^ant   si el contacto es con un agente del MISMO esquema, una pared o un borde
Δt_i  =   ⎨
          ⎩ 0             si el contacto es con un agente del OTRO esquema

EDMD → TSDMD :  Δt_i < t_s
TSDMD → EDMD :  t − t_i^ant ≥ t_s, y ningún contacto con el otro esquema antes del próximo paso
```

- **t_i^ant**: el instante del contacto anterior del agente i (`lastContact`).
- **¿Por qué 0 con el otro esquema?** Un TSDMD es blando: el contacto con él no es instantáneo, dura (≈ π/ω = t_s). Mientras dura, el próximo contacto ya está encima: 0 < t_s. El choque instantáneo de EDMD deja de valer, que es justo lo que mide el criterio.
- **La vuelta usa la misma regla.** Si un TSDMD volviera a EDMD justo cuando va a tocar a otro TSDMD, ese contacto sería con el otro esquema (Δt = 0) y tendría que pasar de nuevo. Por eso no vuelve.
- **No es E2 (el contagio).** E2 miraba cercanía (gap ≤ δ_in) y tenía tres parámetros (δ_in, v_max, horizonte). Esto mira un **contacto real**, predicho con la ecuación de choque, sin parámetros. Es lo mismo que hace EDMD con sus choques: los ve venir.

### Los tres casos del contacto entre esquemas

| Caso | Qué pasa | Superposición |
|---|---|---|
| **1. En el paso** (lo normal) | En el paso de t, cada EDMD vecino de un TSDMD que va a tocar su tramo antes de t+dt pasa a TSDMD **en t, antes de tocarlo**, en cadena (el que pasa también tiene su tramo). La fuerza blanda resuelve el contacto desde el paso siguiente, como en TSDMD puro | **0** |
| **2. A mitad del paso** (raro) | Un evento EDMD (choque, pared, fantasma, bounce back) desvía al agente después del chequeo del paso, y ahora va hacia un tramo. `predict()` lo ve y agenda un evento `TimeStepContact` en el instante exacto. Ahí **rebota elástico, como contra un borde de la puerta que se mueve** (espejo en el sistema del TSDMD), y queda marcado: pasa a TSDMD en el próximo paso | **0** |
| **3. La vuelta** | Un TSDMD vuelve a EDMD solo si no toca a nadie, pasó t_s, **y no va a tocar ningún tramo antes de t+dt** | **0** |

### ★ Caso 2: rebote exacto (opción B, elegida)

| Opción | A favor | En contra |
|---|---|---|
| A. Medir primero cuántas veces pasa | Menos código | Si pasa, la garantía no es total |
| **B. Evento exacto + rebote (elegida)** | Garantía total desde el principio, sin excepciones | Un tipo de evento más. Ese rebote no empuja al TSDMD: durante a lo sumo un dt, el TSDMD es "de masa infinita" para ese agente, como una pared |

**Por qué B:** no queremos explicar una excepción en la defensa. Medido: **4 rebotes en una corrida entera** (N = 200, t_s = π/ω, check `contactsWithTimeStepAreCaught`). El caso existe, es raro, y está cubierto.

**Por qué un rebote elástico:** es el mismo choque que ya explicamos para las paredes y los bordes (v' = v − 2(v·n̂)n̂, masa infinita), visto desde el TSDMD que se mueve. No agrega una regla de choque nueva.

### Detalle del CIM: margen de 2 cm en las celdas

Los TSDMD quedan registrados en la celda de r(t) mientras recorren su tramo. Con celdas de exactamente 2 r_max, un TSDMD que se mueve unos mm hacia el borde de su celda podría tener un contacto fuera de las 3×3 celdas del EDMD: un contacto perdido. Solución:

- **Celdas de 2 r_max + 2 cm** (`Scheme.CELL_MARGIN`), las mismas en los tres esquemas (sigue habiendo una sola grilla).
- El motor **corta con error** si un TSDMD se mueve más que el margen en un dt: haría falta ir a más de 20 m/s (el máximo medido es ~3 m/s).
- **No es un parámetro del modelo:** no cambia la dinámica, solo garantiza que el CIM no pierda vecinos. Con t_s = 0 el híbrido sigue siendo EDMD puro bit a bit (mismas celdas en los dos).

### Implementación

| Dónde | Qué |
|---|---|
| `Agent` | `stepVx`, `stepVy`: velocidad del tramo hasta el próximo paso |
| `HybridScheme.plan` | Calcula el tramo con la posición de Beeman; controla el margen de celda |
| `HybridScheme.step` | Beeman → tramos de todos los TSDMD → ventana [t, t+dt] → cambios de esquema |
| `HybridScheme.catchContacts` | Caso 1: antes "se superpone" (`gap < 0`); ahora "va a tocar el tramo antes de t+dt" |
| `HybridScheme.aboutToTouchTimeStep` | Caso 3: condición nueva para volver |
| `EventCore.timeStepContactTime` | La cuadrática de choque contra el tramo, dentro de la ventana |
| `EventCore.predict` | Antes salteaba a los TSDMD; ahora predice contra su tramo (caso 2) |
| `Event.TimeStepContact`, `EventCore.touchTimeStep` | El rebote del caso 2 y la marca |
| `Scheme.CELL_MARGIN`, `Scheme.cutoff` | Celdas de 2 r_max + 2 cm, usadas por `ExperimentRunner` y los checks |
| Checks | `contactsWithTimeStepAreCaught`: superposición al pasar a TSDMD **≥ −10⁻⁹ m** (antes, < 1 cm). Informa los rebotes del caso 2. `agentOnACellBorderMovesOn` fija celdas de 0.5 m (necesita que x = 5 sea un borde). **1 047 678 checks OK** |

Sin flags nuevas: t_s sigue siendo el único parámetro del híbrido.

### Medido (N = 200, 10 semillas, dt₂ = 5 s, esta máquina: solo para comparar)

| | Etapa 11 | Etapa 10 |
|---|---|---|
| Superposición al pasar a TSDMD (check) | **0** | 2.4 mm |
| Completas | 10/10 | 9/10 |
| T_ej de las completas | 1.08 s | 0.97 s |
| ⟨t_evac⟩ de las completas | 83.9 s | 83.4 s |
| Vueltas TSDMD → EDMD (check, un frame por paso) | 3783 | 3674 |
| Rebotes a mitad de paso (check) | 4 | — |

- **La dinámica no cambia:** mismo t_evac. Las completas no se comparan corrida a corrida (dinámica caótica, Etapa 8).
- **Cuesta ~10 % más:** una cuadrática por par EDMD–TSDMD vecino en cada paso, y otra por TSDMD vecino en cada evento EDMD.
- **Las idas y vueltas no bajan** con la condición nueva de vuelta: casi ningún agente que vuelve iba a tocar un tramo en el próximo milisegundo. Se siguen controlando con t_s.

### Cómo defenderlo

| Pregunta | Respuesta |
|---|---|
| ¿Cuántos criterios? | Uno: el tiempo entre contactos contra t_s. Con un agente del otro esquema vale 0, porque el contacto blando dura |
| ¿Cómo se calcula el evento de un EDMD con un TSDMD? | Con la misma ecuación de choque de EDMD, contra el tramo del TSDMD hasta el próximo paso. El tramo se conoce porque Beeman da r(t+dt) con el estado en t |
| ¿Cómo garantizan que no haya superposiciones? | El EDMD pasa a TSDMD en el paso anterior al contacto, antes de tocarlo. Si un evento lo desvía a mitad de paso, rebota contra el TSDMD como contra un borde y pasa en el paso siguiente. Al volver, nadie está tocando a nadie ni va a tocar a un TSDMD |
| ¿Cómo garantizan que no se pierdan contactos? | Se predicen todos: contra EDMD como siempre, contra TSDMD sobre su tramo. Y las celdas tienen 2 cm de margen para que el CIM siempre encuentre al TSDMD |
| ¿El rebote del caso 2 no es raro físicamente? | Dura a lo sumo un dt (1 ms) y pasó 4 veces en una corrida entera. Es el choque contra una pared, que ya usamos |
| ¿No es el contagio (E2) de nuevo? | No: E2 miraba distancia (gap ≤ δ_in) con parámetros. Esto es un contacto real predicho, sin parámetros |

**No decir:** "nunca se predice contra un TSDMD" (ahora sí, contra su tramo); "se superponen unos mm" (ya no).

### Presentación

- **"Un solo criterio: el tiempo entre contactos"** (`intro-criterio`): Δt_i como función por casos (mismo esquema / otro esquema), la regla en los dos sentidos en un recuadro, t_s = π/ω y la animación del criterio.
- **Diapositiva nueva "Contacto entre esquemas: se predice"** (`intro-convivencia`), en la Introducción: dibujo del tramo de j y del contacto de i antes de t+dt, la posición de Beeman, la ecuación de choque contra el tramo y la tabla de los tres casos. Responde las preguntas del enunciado sobre eventos EDMD–TSDMD, contactos perdidos y superposiciones.
- Implementación: `TimeStepContact` en el UML y en la tabla de eventos; la sincronización dice "nunca se superponen"; el algoritmo calcula los tramos y predice. El Anexo de predicciones, al día.

### Pendiente

- Volver a simular `schemes`, `runtime`, `scaling` y `animation` (ya estaba pendiente; ahora incluye esto).
- ~~`DESIGN.md` §2.7 y las notas de defensa al día~~: hecho el 2026-11-25. `DESIGN.md` reescrito entero con lo vigente; las notas están en `~/Downloads/defensa_TP5.md` (no en el repo; copia de la versión del 15/11 en `~/Downloads/defensa_TP5_backup_2026-11-15.md`). Sus números de resultados quedan con ⏳ hasta volver a simular.

### Presentación: Introducción más liviana (2026-11-25, pedido del grupo)

Revisión del PDF compilado (`RECUPERATORIO SDS-3.pdf`). El profesor pidió la idea general antes que un montón de fórmulas; la parte de EDMD ya estaba bien y quedó como modelo.

- **Se sacó "Dos formas de avanzar en el tiempo".** Era redundante. Lo único que aportaba pasa a dos renglones al pie del SFM: TSDMD (paso fijo, contacto blando, botón a Beeman) y EDMD (discos rígidos, de evento en evento).
- **Nada se usa antes de definirse.** En el choque, el impulso J aparece antes que las velocidades que lo usan. En el SFM se define m. En los fantasmas se define ε_b. En el criterio, t_s se define antes de la regla.
- **Híbrido, idea:** sin la tabla E/S, que se salía de la pantalla y repetía la diapositiva siguiente.
- **Híbrido, criterio:** Δt_i por casos, t_s en una línea y la regla en un recuadro. El porqué va al Anexo, en "Criterio de pasaje: por qué el tiempo entre contactos" (`anx-criterio`).
- **Híbrido, contacto entre esquemas:** dibujo, tres frases y una tabla centrada de tres renglones. La fórmula de Beeman, la velocidad del tramo, la cuadrática y el rebote van al Anexo, en "Contacto entre esquemas: fórmulas" (`anx-convivencia`).
- **Algoritmo del híbrido:** renglones más cortos, sin cortes de línea, para que no se salga por abajo.
- **Cuentas:** 50 diapositivas, 38 hasta "Muchas gracias" (antes 49 y 39). Las notas de defensa están renumeradas.

## Etapa 12: "puede tocarlo antes del próximo paso", sin suponer recta (2026-11-25, decisión del grupo, ya en el motor)

### El problema: con t_s chico, el motor se caía

Al volver a simular con la Etapa 11, el barrido del ítem c **se cortó con error** en t_s = 0.001, 0.003 y 0.01 s (semillas 7, 4 y 1). Los otros 8 casos terminaron. No es un atasco: el motor tira una excepción y la corrida no deja datos.

- "A time-step agent moves more than the cell slack in one step": un agente recién pasado a TSDMD con **|v| = 21–25 m/s**.
- "Overlapping agents approaching": un EDMD que quedó superpuesto a un tramo, con **|v| = 583 000 m/s**.

### La causa: un EDMD atrapado entre una pared y un TSDMD que se acerca

Lo reprodujimos con una copia del motor con trazas (en el scratchpad de la sesión, no en el repo):

```
 inicio del paso (t):   ║      ●↗          ◯←    "yendo derecho, no toca el tramo" → sigue EDMD
 mitad del paso:        ║   ●↘                   rebota en la pared (o en un borde)
 mismo paso:            ║    ●→  ←◯              ahora sí va hacia el tramo… y ya no puede pasar
```

1. El chequeo del paso (caso 1 de la Etapa 11) preguntaba "**yendo derecho**, ¿toca el tramo antes de t + dt?". Un evento a mitad de paso (pared, borde, otro EDMD) lo desviaba hacia el TSDMD.
2. Entonces entraba el caso 2: **rebote elástico contra el tramo**, como contra un borde que se mueve. El TSDMD no lo siente: es un pistón de masa infinita. Cada rebote le suma ~2u.
3. Pared → TSDMD → pared → TSDMD…: el hueco se achica, los rebotes se aceleran y la velocidad crece sin límite (aceleración de Fermi). En t_s = 0.003 hubo 19 rebotes en 0.08 ms. En t_s = 0.01, decenas en el mismo instante.

**Por qué solo con t_s chico:** con t_s ≤ 0.01 s los agentes van y vienen entre esquemas todo el tiempo, así que hay muchos EDMD pegados a TSDMD contra las paredes y la puerta. Con el t_s por defecto medimos 0 a 10 rebotes por corrida (10 semillas) y |v| ≤ 3.1 m/s: la trampa no se formaba, pero existía.

**No es un resultado válido de las consideraciones (iii).** Ahí lo válido es que la evacuación no termine por un atasco de la dinámica, con la corrida bien hecha hasta el tope (`complete false`). Esto es un artefacto del acople, no un atasco, y además contradecía lo que defendíamos ("el rebote dura a lo sumo un dt", "nunca hay superposiciones").

### Opciones

| Opción | A favor | En contra |
|---|---|---|
| A. Un rebote; si vuelve a tocar en el mismo paso, se pega al tramo hasta el paso | Cambio mínimo; a/b/d casi iguales | Hasta \|u\|·dt ≈ 1.5 mm de superposición con la pared durante menos de un dt |
| B. Siempre se pega al tramo | Fácil de explicar | Superposición igual que A; cambia todo |
| C. Que el TSDMD sienta el choque (choque duro EDMD–TSDMD) | Cero superposición | Rompe "un TSDMD solo siente TSDMD", re-predicción en cascada, impulso en medio de Beeman |
| D. Sacar t_s ≤ 0.01 del barrido | Sin tocar el motor | El bug queda latente |
| **E. El chequeo del paso pregunta "¿puede tocarlo?", no "¿lo toca yendo derecho?" (elegida)** | **Pasa antes de tocarlo de verdad**: desaparecen el rebote a mitad de paso, el bombeo y toda superposición. Sin parámetros nuevos | Pasan unos pocos agentes más, unos mm antes; cambia levemente la dinámica: se vuelve a simular todo |

Con el TSDMD fijo en su tramo, no hay forma de evitar la superposición **una vez que el EDMD ya está atrapado**: si el tramo deja menos lugar que el diámetro, el disco no entra. Por eso la solución es que **nunca llegue a esa situación**: que pase a TSDMD antes.

### La regla (★21, mismo criterio)

Sigue siendo **un solo criterio**: Δt_i contra t_s, con Δt_i = 0 para un contacto con el otro esquema. Lo único que cambia es **cuándo cuenta un contacto con un TSDMD**:

```
 un TSDMD solo existe en la grilla t, t+dt, t+2dt…  →  un EDMD solo puede pasar en un paso
 entonces: si i PUEDE tocar a j antes del próximo paso, el contacto ya cuenta (Δt = 0 < t_s)

 puede tocarlo  ⇔  gap_ij(t) ≤ (s_i + |u_j|)·dt
```

- **gap_ij(t)**: separación en el paso, |r_j − r_i| − R_i − R_j.
- **|u_j|·dt**: lo que el TSDMD recorre de su tramo en el paso.
- **s_i·dt**: lo máximo que el EDMD puede recorrer en el paso, **en cualquier dirección** (puede rebotar).
- **s_i = √2 · max(|v_i|, v_d, |v_k| de los EDMD vecinos k)**, porque:
  - paredes y bordes conservan |v|;
  - los fantasmas llevan la velocidad hacia v_d ê: a lo sumo max(|v|, v_d);
  - un choque elástico de masas iguales conserva |v_i|² + |v_k|²: a lo sumo √2 veces el más rápido de los dos.

Con dt = 1 ms son **3 a 6 mm**. No es un parámetro: sale de dt y de las velocidades.

**La vuelta usa la misma pregunta:** un TSDMD vuelve si pasó t_s sin contacto **y no puede tocar** a ningún TSDMD antes del próximo paso.

**El caso 2 de la Etapa 11 desaparece.** `predict()` sigue prediciendo contra los tramos, pero ahora como **guardia**: si un EDMD tocara un tramo a mitad de paso, el motor corta con error ("An event-driven agent touched a time-step one in the middle of a step") en lugar de superponer o rebotar. El motor también corta si (s_i + |u_j|)·dt supera el margen de 2 cm de las celdas (más de 20 m/s).

### ¿No es el contagio (E2) de nuevo?

No. E2 era una **condición aparte**, "gap ≤ δ_in", con un δ_in elegido a mano (y v_max y horizonte). Acá no se elige nada. La pregunta es la de la Etapa 11, "¿lo toca antes del próximo paso?", **sin suponer que va derecho**. La distancia es solo lo que se puede recorrer en un dt.

### Implementación

| Dónde | Qué |
|---|---|
| `HybridScheme.canTouch` | gap_ij(t) ≤ (s_i + \|u_j\|)·dt, con el control del margen de celda |
| `HybridScheme.reachSpeed` | s_i = √2 · max(\|v_i\|, v_d, \|v_k\| de los EDMD vecinos) (segundo buffer de vecinos, `around`) |
| `HybridScheme.catchContacts` | Caso 1: antes "va a tocar el tramo yendo derecho"; ahora `canTouch` |
| `HybridScheme.canTouchTimeStep` | Caso 3 (la vuelta): reemplaza a `aboutToTouchTimeStep` |
| `EventCore.touchTimeStep` | Antes, el rebote del caso 2; ahora la guardia que corta con error. Se borró el contador `timeStepContacts` |
| Checks | `contactsWithTimeStepAreCaught` agrega t_s = 0.001 s (el caso que bombeaba). Terminar prueba que no hubo contacto a mitad de paso. **1 047 682 checks OK** |

### Medido

- Los tres casos que se caían (t_s = 0.001, 0.003, 0.01, 10 semillas desde la 1): **terminan los 30**, sin error. Las `complete false` son atascos comunes al tope de 1000 s.
- Prueba de estrés: t_s ∈ {0.001, 0.003, 0.01, 0.03, π/ω}, 30 semillas cada uno (11–40): **150 corridas, 0 errores**. La guardia nunca saltó.
- Check (N = 200, t_s = π/ω): gap mínimo EDMD–TSDMD en cualquier paso **2.1 mm** (antes 0, se tocaban); 0 superposición al pasar.

### Re-corrida completa con la Etapa 12 (2026-11-25, `simulation all`)

Sesiones: `dt-timestep/2026-11-25_19-11-55` (TSDMD puro, no depende del cambio: mismos números que antes), `ab-schemes/2026-11-25_19-12-41`, `c-runtime/2026-11-25_19-12-58`, `d-scaling` y `acd-animation` del mismo día. Log en `scratchpad/log_all.txt`.

| Ítem | Resultado |
|---|---|
| dt | Pasan 10⁻⁴ a 10⁻³ s; **2.5·10⁻³ s queda justo afuera** (\|80.20 − 84.46\| = 4.26 s > σ = 4.18 s; antes pasaban los cinco). Se sigue usando 10⁻³ s |
| a | ⟨f̄_TS⟩ = 0.63 ± 0.02; saltos por corrida (cada dt₂): 560 ± 40 de EDMD a TSDMD y 540 ± 50 de vuelta |
| b | ⟨t_evac⟩: TSDMD 82 ± 4 s (9/10), EDMD 98 ± 4 s (10/10), híbrido 83 ± 4 s (9/10). Híbrido − TSDMD = +1.5 s (0.8 errores estándar; con la Etapa 11 era +4.0 s, 2.3). EDMD − TSDMD = +16.6 s (8.8) |
| c | **Los 11 t_s terminan** (antes se caían 3). T_ej: TSDMD 0.45 s, EDMD 0.43 s, híbrido 0.82–1.02 s. ⟨t_evac⟩ del híbrido dentro de la banda de TSDMD para todo t_s. ⟨f̄_TS⟩ de 0.48 (t_s ≤ 0.01) a 0.75 (t_s = 1 s) |
| d | Híbrido ≈ 2.2–2.4 × TSDMD en todo N (N = 1000: 16.3 contra 6.8 s). Pendientes log-log: EDMD ≈ 2.4 (N ≤ 500), TSDMD e híbrido ≈ 1.8 |
| Atascos | TSDMD y el híbrido, en todos los ítems. **EDMD no se trabó en ninguna corrida** de esta tanda |

**Ojo con "EDMD hasta N = 500" (ítem d, 2026-11-09):** se decidió con el choque inelástico, cuando N = 500 tardaba 32–190 s. Con el choque elástico, N = 500 tarda **4.5 s**: el motivo del tope ya no vale. Queda a decisión del grupo si se extiende EDMD a N = 700 y 1000 (proyectado ≈ 10–25 s por corrida).

### Presentación: el híbrido contado más simple (2026-11-25, pedido del grupo)

La explicación del criterio no se entendía: arrancaba con la fórmula por casos (Δt = 0 "con el otro esquema") sin decir por qué, y la diapositiva de convivencia juntaba Beeman, tramo, gap y s_i.

- **`intro-criterio`:** primero la idea ("EDMD vale si los choques son aislados") y una **línea de tiempo de los contactos de un agente**, por overlays: contactos separados más que t_s → EDMD; dos a menos de t_s → TSDMD; t_s sin contactos → vuelve. La regla en palabras en un recuadro, y t_s = π/ω. La fórmula por casos se fue al Anexo (`anx-criterio`, "La regla completa").
- **`intro-convivencia`** ("Un EDMD cerca de un TSDMD"): el mismo dibujo, contado en tres pasos numerados: (1) tocar a un TSDMD es un choque blando, que dura, así que pasa; (2) solo puede pasar en un paso, y en el medio puede rebotar; (3) por eso pasa en el paso en que **podría** tocarlo (2–6 mm). Al pie, la vuelta y la garantía. Beeman, el tramo, s_i y la guardia quedan en `anx-convivencia`.
- Misma cantidad de diapositivas (45 frames). Guion de las diapos 10 y 11 en las notas de defensa, al día.

### Revisión de los números de la presentación (2026-11-26)

Contra los CSV de las sesiones `dt-timestep/2026-11-25_19-11-55`, `ab-schemes/2026-11-25_19-12-41`, `c-runtime/2026-11-25_19-12-58` y `d-scaling/2026-11-25_19-17-34` (posteriores al último cambio del motor). Las figuras de `presentation/figuras/` son byte a byte las de esas sesiones.

- Coinciden: elección de dt (2.5·10⁻³ s queda afuera por 0.1 s), saltos 560 ± 40 y 540 ± 50, completas (9/10/9), Δt_evac (+16.6 s, 8.8 errores estándar; +1.5 s, 0.8), ⟨f̄_TS⟩ = 0.63 ± 0.02, T_ej con N = 200 (0.95, 0.45, 0.43 s), "EDMD no se trabó".
- Corregido: los exponentes N^2.4 y N^1.8 eran solo de la variante de densidad constante; con L = 50 m, EDMD da N^2.8 (TSDMD 1.8, híbrido 1.9, ajuste log-log). El cociente híbrido/TSDMD con N = 1000 es 2.4 con densidad constante (TSDMD con 1 de 3 completas) y 2.2 con L = 50 m: la conclusión dice "2.2 a 2.4".
- Agregado a los paneles lo que la conclusión citaba sin mostrar: ⟨f̄_TS⟩ (ítem a), T_ej de los tres con t_s = π/ω (ítem c) y los exponentes (ítem d).
- Alcance de `canTouch` en `intro-convivencia`: 3–6 mm → **2–6 mm**: el mínimo es √2·v_d·dt = 2.1 mm (TSDMD quieto en la multitud).

### Presentación: conclusiones, Anexo y SFM (2026-11-26, pedido del grupo)

- **Conclusiones:** cinco renglones de una idea cada uno (dt, t_evac, f̄_TS, costo, escalamiento). **Se saca la de atascos.**
- **Anexo:** después del Disclaimer y del Apéndice de la IA, tras `\appendix` y sin `\section`, así no aparece en la barra de arriba; lo abre una carátula "Anexo". Menos texto: cada fórmula con su nombre (`\nombre`) y la tabla de sus partes (`\partes`). Mismas etiquetas, botones de ida y vuelta intactos.
- **Diapositiva del SFM (`intro-sfm`):** sin los renglones TSDMD/EDMD de abajo ni el botón a Beeman. `anx-beeman` sigue en el Anexo, sin botón de ida.
- **GIFs** regenerados desde los MP4 de `acd-animation/2026-11-25_19-26-03` con `scratchpad/make_gifs.py` en `presentation/gifs/<ítem>/` (8, 540×540, 12.5 fps). Los incrusta el grupo.

### Presentación: Introducción del híbrido más liviana (2026-11-26, pedido del grupo)

- **`intro-fantasma`:** sin la fórmula del bounce back (sigue en `anx-patadas`).
- **`intro-criterio`:** solo la idea ("EDMD vale si los choques son aislados"), la línea de tiempo de los contactos rehecha (sin rótulo a la izquierda, que la desbordaba; las tres flechas a la misma altura; el tramo "< t_s" más ancho) y la animación.
- **`intro-criterio-ec` (nueva):** el recuadro de la regla pasa a una ecuación por casos con el esquema actual como dominio: TSDMD si es EDMD y t_i^(k) − t_i^(k−1) < t_s; EDMD si es TSDMD y t − t_i^(k) ≥ t_s; con t_s = π/ω, ω = √(2k_n/m), y la tabla de partes. El botón "Por qué" a `anx-criterio` queda acá.
- **`intro-convivencia`:** dos renglones (tocar a un TSDMD es un choque blando ⇒ pasa; pasa en el paso en que podría tocarlo, aunque rebote antes) y la garantía. El dibujo queda, en dos pasos. Lo demás, en `anx-convivencia`.
- **Tabla de eventos:** sin la fila "Contacto con TSDMD" (es una guardia que nunca ocurre). Sigue en el diagrama de clases, porque existe en el código (`Event.TimeStepContact`).
- **`intro-criterio-ec`:** el pasaje grande y central; t_s = π/ω = 5.7·10⁻² s y ω = √(2k_n/m) en dos renglones chicos abajo.
- **`intro-convivencia`, otra vez (2026-11-26):** contada como una **franja** alrededor de cada TSDMD, de ancho (s_i + |u_j|)·dt ≈ 2–6 mm (lo que recorren los dos en un paso). Un EDMD dentro de la franja en un paso pasa a TSDMD; así nunca se tocan. Sale el dibujo de la pared y el rebote: era un caso borde que tapaba la idea. El rebote (por qué la franja usa la rapidez máxima en cualquier dirección) queda en `anx-convivencia` y en el guion.
- **Reloj** ("Un solo reloj: la cola de eventos", eventos y pasos Δt en una línea de tiempo): de `intro-hibrido` a `impl-sincro`, arriba de los ítems. `intro-hibrido` queda con la idea sola (2 clics).
- **Guion de defensa** (`~/Downloads/defensa_TP5.md`, fuera del repo; backup del 25/11 en `defensa_TP5_backup_2026-11-26.md`) al día con la presentación del 26/11: numeración nueva (51 diapos, 39 hasta "Muchas gracias"), diapos 4, 6, 8–12, 16, 17, 36 y 38 reescritas, la diapo 11 agregada, y lo que salió de las diapos marcado "[Decirlo: no está escrito]".
- **`intro-criterio-ec`, otra vez:** sin el nombre arriba; la ecuación con Δt_i (tiempo entre los dos últimos contactos) y t_i^libre (tiempo desde el último), los dos tramos de la línea de la diapo 10, en lugar de t_i^(k); t_s = π√(m/2k_n) = 5.7·10⁻² s, sin ω. Guion al día.
- **`impl-sincro`:** sin el último ítem ("un paso cuesta según los TSDMD"), pedido del grupo. Guion al día.
- **`intro-criterio-ec`:** t_s "lo que dura un choque blando **entre dos agentes**". **`intro-convivencia`:** texto en tres renglones (problema: rígido contra blando; franja: lo más que pueden acercarse antes del próximo paso; regla: el que entra pasa antes de tocarlo), sin los mm ni la fórmula. Guion al día.
- **`impl-sincro`:** sin el ítem "Fuerzas solo entre TSDMD…" (repetía la franja). **Diapo 36 (T_ej vs N):** panel compacto (t_s y M; EDMD hasta N = 500; pendientes) en una columna de 0.20; el renglón del ajuste se salía de la hoja.
- **`intro-criterio-ec`, versión elegida por el grupo:** la condición en palabras dentro de la ecuación por casos (TSDMD si es EDMD y choca dos veces a menos de t_s; EDMD si es TSDMD y pasa t_s sin chocar), sin tabla de partes; abajo, en tamaño \small, t_s = π√(m/2k_n) = 5.7·10⁻² s, lo que dura un choque blando entre dos agentes. Guion al día.
- **Paneles de Resultados más cortos (pedido del grupo):** solo parámetros, M y el número que respalda cada conclusión. Salieron al guion: el rango de dt que cumple la tolerancia y la referencia 0.1√(m/k_n); "promedio sobre las completas con N_in > 0" (está en Observables); los errores estándar de Δt_evac; las corridas completas (las marcan los símbolos vacíos); dt₂ = 5 s (está en Parámetros).
- **Conclusiones, revisadas contra Resultados:** la 2 suma "con cualquier t_s" (diapo 34); la 3 usa la meseta ⟨f_TS(t)⟩ ≈ 0.95 en lugar de ⟨f̄_TS⟩ = 0.63, que promedia toda la evacuación y no mide la multitud; la 4 dice "unas 2 veces, con cualquier t_s y N" (medido 1.7–2.4); la 5 dice N^1.8 a N^1.9, como el panel. Guion al día.
- **`intro-convivencia`, en prueba (pedido del grupo):** como `intro-fantasma`, con titulitos (`\nombre`) y una relación por bloque. "Franja alrededor de un TSDMD j": g_ij ≤ (s_i + |u_j|)·Δt, "acercamiento máximo en un paso". "Pasaje" (clic 2): i EDMD en la franja ⇒ esquema_i ← TSDMD. "Garantía": sin superposiciones ni contactos perdidos. Salen los rótulos Problema/Franja/Regla en frase y la línea del pie. **Vuelve la fórmula de la franja**, que se había sacado el mismo día: se prueba así y se decide después. g_ij, s_i y u_j se nombran en voz alta y se definen en `anx-convivencia`. Guion al día.
- **Links de YouTube de las animaciones:** las 8 de `acd-animation/2026-11-25_19-26-03`, subidas por el grupo como "SdS TP5 G09 Recuperatorio – …" para no confundirlas con las de la entrega original. Cada id va como tercer argumento de `\figanim`, que muestra `youtu.be/<id>` (la URL de Shorts entera se partía en la columna). Ya no quedan "pendiente: link de YouTube".
- **`intro-convivencia` (2026-11-27, pedido del grupo):** debajo de la fórmula de la franja, sus partes en `\scriptsize` (g_ij: separación entre bordes; s_i: rapidez máxima de i en el paso; u_j: velocidad de j en el paso), porque la fórmula estaba en la diapo sin definir. Cambios del grupo en la misma tanda: "Pasaje" → "Transición" (también en los subtítulos de `intro-criterio` e `intro-criterio-ec`), sin los rótulos "franja" y "franja exagerada" del dibujo; títulos de `intro-cuando` e `intro-choque` → "EDMD: Choque entre Agentes"; subtítulo de paredes → "Antes y después del choque"; `intro-hibrido` sin subtítulo. Guion al día.
- **Anexo rehecho (2026-11-27, pedido del grupo: se desbordaba):** de 9 a 14 diapositivas, una idea cada una, con título y subtítulo como en el cuerpo ("Social Force Model / Ecuación de movimiento", "EDMD: Tiempo de Choque / Entre dos agentes", "Esquema Híbrido / Franja alrededor de un TSDMD", "TSDMD: Integrador de Beeman / Predictor-corrector") en lugar de "Anexo" en todas. Cada fórmula con su `\nombre`, una debajo de la otra (sin dos nombres en un renglón ni paréntesis al lado del nombre), y a lo sumo tres renglones de `\partes`. Nuevas: `anx-sfm-geo`, `anx-tc-pared`, `anx-choque-usado`, `anx-criterio-motor`, `anx-convivencia-tramo`; las etiquetas de ida no cambian y cada diapositiva tiene su botón de vuelta. "Pasaje" → "Transición" también acá. La guardia pasó de `anx-convivencia` a la tabla de `anx-predicciones`. Mismas fórmulas; se suman partes que faltaban (D < 0: no se cruzan; ξ > 0: hay contacto; la tangente; Δr en el contacto; Δt). Guion renumerado (43–56).
- **`intro-convivencia`:** sin "acercamiento máximo en un paso"; Transición y Garantía en `\small`; el botón "Fórmulas", bajo el dibujo. La columna derecha tocaba el título y el pie.

### Corrida con t_s = 0.3 s (2026-11-27, pedido del grupo)

Sesiones: `ab-schemes/2026-11-27_00-46-29`, `c-runtime/2026-11-27_00-46-43`, `d-scaling/2026-11-27_00-51-14`, `acd-animation/2026-11-27_01-59-03` (la de las 01-01-18 tenía la escena N1000 trabada, ver abajo). `dt-timestep/2026-11-25_19-11-55` no se repite (TSDMD puro). Motor compilado con `build --check`: 1 047 682 checks. Log en `scratchpad/log_all_ts03.txt`.

| Ítem | Antes (π/ω) | Ahora (0.3 s) |
|---|---|---|
| a | ⟨f̄_TS⟩ = 0.63 ± 0.02; meseta ⟨f_TS(t)⟩ 0.93; saltos 560 ± 40 / 540 ± 50; 9/10 | ⟨f̄_TS⟩ = 0.75 ± 0.01; meseta 0.999 ± 0.001 (35–70 s); saltos 232 ± 7 / 140 ± 10; 10/10 |
| b | Híbrido − TSDMD = +1.5 s (0.8 e.e.) | +0.6 s (0.3 e.e.); EDMD − TSDMD = +16.6 s, igual |
| c | T_ej: 0.95 / 0.45 / 0.43 s | 0.79 / 0.42 / 0.41 s; π/ω, ahora punto del barrido: 0.90 s |
| d | Pendientes: EDMD 2.4 y 2.8; TSDMD 1.8; híbrido 1.8–1.9; cociente 2.2–2.4 | EDMD 2.4 y 2.7; TSDMD 1.7–1.8; híbrido 1.8–2.0; cociente 1.3 (N = 100, L = 50 m) a 2.3 |

- Las escenas `tsdmd`, `edmd`, `hibrido_ts_chico` y `hibrido_ts_grande` salieron iguales byte a byte (datos de las corridas): no se resuben. Cambian `hibrido`, `criterio_E3`, `N100` y `N1000`.
- El T_ej medido varía entre sesiones: t_s = 0.1 s dio 0.82 el 25/11 y 0.92 hoy. Se habla de "0.3 a 1 s, los más baratos", no de un óptimo.
- **Conclusiones:** la 3 pasa a ⟨f_TS(t)⟩ ≈ 1; la 4, "de 1.3 a 2.3 veces lo que TSDMD puro, con cualquier t_s y N"; la 5, N^2.4–N^2.7 contra N^1.7–N^2.0.

### Leyendas que no tapan nada (2026-11-27, pedido del grupo)

`plotting.keep_legend_clear`, llamada desde `save_figure` en todas las figuras: si una línea, marcador, barra de error o banda de referencia cae debajo de la leyenda, sube el tope del eje y de a 8 % hasta despejarla; las líneas verticales de referencia (Δt y t_s elegidos) terminan debajo de la leyenda. La de `c/ts_fraction_vs_time` pasa a "lower center" (abajo a la derecha la tapaba la corrida con t_s = 0.01 s). Las figuras del ítem c marcan el t_s elegido con una línea punteada, como la de Δt.
- **Escena `N1000`, semilla 4 → 5:** con t_s = 0.3 s la semilla 4 se traba (salen 973/1000 y queda hasta el tope de 5000 s: 25 001 cuadros). Como dice `movie.py`, las semillas de las escenas se eligen para que terminen: de 4 a 14 terminan 5–10 y 14; se usa la siguiente, 5 (234 s). Se vuelve a correr el estudio de animaciones entero, para que la sesión quede completa.
- **Links nuevos (t_s = 0.3 s):** `anim_criterio_E3` → Rn3SziLxUTo, `anim_hibrido` → UqPq0FsEVNM, `anim_N100` → fWHws9SVspE, `anim_N1000` → LhZrnV5EboI. `anim_tsdmd`, `anim_edmd`, `ts_chico` y `ts_grande` siguen con los del 26/11.
- **Entrega del recuperatorio (2026-11-27):** en `entregaTmp/`, con los nombres del enunciado (GXX = G09, SS = S): `SdS_TP5_2026Q2G09CS_Presentación.pdf` (copia byte a byte del PDF compilado por el grupo, revisado: 8 links nuevos, t_s = 0.3 s, números del 27/11) y `SdS_TP5_2026Q2G09CS_Codigo.zip` (30 kB: solo `experiment/src/**/*.java`, 17 archivos; sin checks, como dice `DESIGN.md`; sin postprocesamiento, output ni figuras). El zip se probó solo: compila con `javac --release 21` y corre (`ts 0.3` por defecto).
- **Diapo 33 y Conclusiones (2026-11-27, decisión del grupo):**
  - **Diapo 33 (⟨T_ej⟩ vs t_s):** queda solo la figura con la banda de EDMD puro (`c/wall_clock_vs_ts.png`), en una columna de 0.66 con el panel al costado, como la diapo 26. Sale `c/wall_clock_vs_ts_no_edmd.png`. Opciones: (a) dejar las dos, como pidió el profesor el 13/11 («T_ej vs el parámetro también sin EDMD, para ver la escala»): cumple la nota, pero las dos figuras se ven casi iguales, porque las bandas de TSDMD (0.42 s) y EDMD (0.41 s) se superponen; (b) dejar solo la de la izquierda: una figura más grande y sin repetir. **Elegida: (b).** Con estos números la banda de EDMD ya no cambia la escala, que era el motivo de la nota. La figura sin EDMD se sigue generando, por si la piden.
  - **Conclusiones:** sale «⟨t_evac⟩ no depende de Δt hasta 10⁻³ s». La elección de Δt es una validación del método (diapo 26), no un resultado de los ítems a–d. Quedan cuatro.
