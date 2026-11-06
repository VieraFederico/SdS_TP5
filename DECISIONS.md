# DECISIONS — registro de decisiones del TP5

Qué decidimos, qué otras opciones había y por qué. Se actualiza con cada decisión nueva, de diseño o teórica. `DESIGN.md` dice cómo queda el sistema; este archivo dice por qué.

En cada decisión, **Por qué** es el argumento de la recomendación que aceptaste. Si tuviste otra razón, agregala ahí. Los números (★9, 21, …) son los de la sección 5 del prompt inicial y de `DESIGN.md`.

## Resumen

| # | Tema | Elegido | Estado |
|---|---|---|---|
| ★1 | Modelo | SFM sin fuerza social | Confirmada |
| ★3 | Integrador | Beeman predictor-corrector | Confirmada |
| 6 | Target | (L/2, R_i), luego (L/2, −2 m) | Confirmada |
| ★9 | Restitución | e derivado del SFM: 0.94 agentes, 0.92 paredes | Confirmada |
| ★10 | Recuperación de v_d | Eventos de relajación | Confirmada |
| 16 | Avance en EDMD | Perezoso | Confirmada |
| 18 | Vecinos en EDMD | CIM con evento de cruce de celda | Confirmada |
| ★21 | Criterio de pasaje | Zona R_s + contagio por piel + tiempo de vuelo, con histéresis | Confirmada |
| ★21b | Criterio por tiempo | Tiempo de vuelo entre choques < t_s = 0.057 s | Confirmada (umbral propuesto por Claude) |
| ★22 | Parámetro principal | R_s, con t_s fijo | Confirmada |
| ★23 | Sincronización | Reloj dt + horizonte seguro | Confirmada |
| 2 | Parámetros | m = 80 kg, τ = 0.5 s, k_n = 1.2·10⁵ N/m, k_t = 2.4·10⁵ kg/(m·s) | Confirmada |
| 4, 5, 8, 11, 12, 19, 24, 25, 29, 32 | Ver "Aprobadas en bloque" | Como se propuso | Confirmada |
| 7, 13, 14, 15, 17, 20, 26, 27, 28, 30, 31 | Ver "Por precedente" | Como en el TP3 o el TP4 | Por precedente |

---

## Confirmadas

Todas el 2026-11-06.

### ★1 — Modelo peatonal

**Pregunta:** ¿qué modelo peatonal usamos?

| Opción | A favor | En contra |
|---|---|---|
| **SFM sin fuerza social (elegida)** | Un agente a v_d hacia un target fijo tiene fuerza total nula: va recto a velocidad constante, que es lo que supone EDMD. Parámetros en T6, diapo 33 | dt chico, por la rigidez del contacto |
| CPM | Más rápido; dt fijo por el modelo | El radio crece y la velocidad cambia sin contacto: el vuelo libre deja de ser recto y uniforme entre eventos, y hay que predecir con radios variables |

**Por qué:** es el modelo donde EDMD es exacto entre choques. Era tu inclinación desde el prompt.

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

**Pregunta:** ¿qué criterio de pasaje y qué parámetro principal usamos?

| Opción | A favor | En contra |
|---|---|---|
| **Zona R_s + contagio (elegida)** | R_s = 0 da EDMD puro exacto y R_s ≥ 56 m da TSDMD puro exacto. No hay contacto duro–blando | Espero salto entre R_s = 0 y 1 m, y meseta hasta unos 7–8 m |
| Solo zona R_s | Más simple, sin meseta | Si la multitud supera R_s, quedan duros contra blandos |
| Solo piel δ | Local, sin zona fija | Ningún valor reproduce EDMD puro, que pide el ítem c |
| Densidad local o número de vecinos (descartada en `DESIGN.md`) | Mide directamente lo denso | Hay que calcularla para todos en cada paso: EDMD pierde su ventaja |

**Por qué:** zona y contagio dan extremos exactos sin dejar contactos duro–blando. Pediste aclarar mejor el criterio y sumar uno por tiempo: ver ★21b y `DESIGN.md`, 2.6.

**Criterio:**

```
EDMD → TSDMD  si  r_i ≤ R_s,  o  g_ij ≤ δ_in para algún j en TSDMD,  o  t_vuelo,i < t_s
TSDMD → EDMD  si  r_i ≥ R_s + h,  y  g_ij ≥ δ_out para todo j,  y  |v_i − v_d ê_i| ≤ ε v_d
```

**Valores iniciales:** δ_in = 0.05 m, δ_out = 0.25 m, h = 1 m, ε = 0.05, t_s = 0.057 s. Barrido: R_s ∈ {0, 1, 2, 3, 5, 8, 12, 20, 35, 56} m con t_s fijo.

**Riesgo aceptado:** la meseta, que con ★21b se extiende hasta R_s = 0. Es una predicción sin medir; si aparece, es un resultado del ítem c.

**Fuente:** enunciado, viñetas del híbrido e ítem c; decisión nueva.

### ★21b — Criterio por tiempo entre choques

**Pedido del usuario (al aprobar `DESIGN.md`):** aclarar mejor el criterio de pasaje y agregar otro para pasar de EDMD a TSDMD, comparando el tiempo de los choques con el step "o algo así".

**Qué tiempo medir:**

| Opción | A favor | En contra |
|---|---|---|
| **Tiempo de vuelo entre choques consecutivos (elegida)** | Solo es corto si el agente choca seguido. Se evalúa al procesar el choque, sin costo extra | Necesita un choque para detectar: el primero siempre lo resuelve EDMD |
| Tiempo al próximo choque | Anticipa el contacto | Tiende a cero antes de todo choque: pasaría todos a TSDMD y no quedaría ningún choque duro |

**Con qué umbral compararlo:**

| Opción | A favor | En contra |
|---|---|---|
| **t_s = duración de un choque blando, π/ω ≈ 0.057 s (elegida)** | Es la condición de validez de EDMD: t_vuelo ≫ t_choque [Teorica_3, diapo 5] | Ajustable, no es un límite exacto |
| t_s = dt | Es lo más literal del pedido | Casi nunca dispara: el piso v_sep separa los choques por más que un paso |
| t_s = κ·dt | Liga el umbral al costo de un paso | Agrega un parámetro sin base teórica, y depende del dt elegido |

**Por qué:** la comparación con el step sigue presente, porque t_s ≈ 57 pasos a dt = 10⁻³ s. El umbral lo propuse yo sobre tu idea: si preferís otro, se cambia con un parámetro.

**Consecuencias:**

- **Cambia el extremo de EDMD:** EDMD puro ahora es R_s = 0 **y** t_s = 0. Con t_s > 0, R_s = 0 es un híbrido válido.
- **Pasaje en cadena:** al pasar a TSDMD se revisan los vecinos con el contagio, para mantener la garantía de no perder contactos.
- Evita que una multitud fuera de la zona quede en EDMD con tasa de eventos enorme.

**Fuente:** Teorica_3, diapo 5; pedido del usuario. t_s sale de ω en ★9.

### ★23 — Sincronización

**Pregunta:** ¿cómo sincronizamos los dos esquemas?

| Opción | A favor | En contra |
|---|---|---|
| **Reloj dt + horizonte seguro (elegida)** | Cada EDMD agenda su chequeo: los lejanos no cuestan nada por paso | Un tipo de evento más; hay que acotar v_max |
| Reloj dt + piel por paso | Más simple | Costo N_ED por paso: resta la ventaja que mide el ítem c |

**Por qué:** conserva la ventaja del híbrido en la zona diluida y mantiene la garantía de no perder contactos.

**Consecuencia:** en cada intervalo se procesan los eventos hasta t + dt, después un paso TSDMD; los pasajes se aplican al final del paso. Horizonte: t + (g − δ_in)/(v_i + v_max). Garantía: (v_i + v_j)·dt < δ_in, con v_max = 3 m/s (valor inicial); si se supera, el motor corta con error.

**Fuente:** enunciado; decisión nueva.

---

### 2 — Parámetros no especificados

**Pregunta:** ¿de dónde salen la masa y los demás parámetros? El usuario recordaba que el enunciado fijaba la masa.

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

Aprobadas el 2026-11-06 junto con `DESIGN.md`, como se propusieron. El usuario no pidió cambios en ninguna.

| # | Tema | Propuesta | Alternativas | Por qué la propuesta |
|---|---|---|---|---|
| 4 | dt y dt₂ | dt por estudio con M = 10: el mayor con ⟨t_evac⟩ a menos de un σ del de menor dt. dt₂ = 0.5 s, 0.1 s en animaciones | Fijar dt = 2.6·10⁻³ s de T5, diapo 19 | C4, D17 pide criterio y umbral marcado |
| 5 | Velocidad inicial | v₀ = v_d ê_target | Reposo: suma un transitorio y deja de ser vuelo libre | Arrancan en vuelo libre |
| 8 | Coordenadas | Sala [0, L]², puerta en y = 0 | — | Simple, sin alternativa relevante |
| 11 | Colapso inelástico | Piso v_n,sal = max(e·v_n,ent, v_sep), v_sep = 0.02 m/s | e chico: no alcanza. Tiempo mínimo entre choques: puede dejar pasar contactos. Separación mínima: mueve posiciones | Acota la tasa de eventos con cualquier e, sin tocar posiciones |
| 12 | Tipos de evento | Choque, pared, borde, target, puerta, retiro, relajación, cruce de celda, escritura, zona, piel | Fusionar algunos | Salen de las decisiones ya tomadas |
| 19 | Escritura en EDMD | Evento de escritura cada dt₂ | Cada k eventos, como el TP3: no cumple dt₂ | El enunciado pide dt₂ |
| 24 | Colisiones en el híbrido | ED–ED: evento duro; pasa a TSDMD si es el segundo choque en menos de t_s. ED–TS: no hay, pasa a TSDMD antes | Pasar a TSDMD también ante ED–ED: no da EDMD puro en R_s = 0 | Consecuencia de ★21 |
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
| Solo el usuario commitea | Lo pediste. Claude frena y avisa |
| Solo el usuario instala cosas | Lo pediste |
| `venv`, no `uv` | Lo pediste |
| `scratchpad/` ignorado por git | Para retomar en otra sesión |
| `DECISIONS.md` con todo lo decidido | Para saber qué se eligió y por qué |
| Compilar con `javac --release 21`, sin Maven | Como el TP4; `mvn` no está instalado |
| Entrega: `SdS_TP5_2026Q2G09CSS_Presentación` y `SdS_TP5_2026Q2G09CSS_Codigo` | Grupo 9, comisión S |
