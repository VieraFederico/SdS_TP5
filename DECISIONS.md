# DECISIONS — registro de decisiones del TP5

Qué decidimos, qué otras opciones había y por qué. Se actualiza con cada decisión nueva, de diseño o teórica. `DESIGN.md` dice cómo queda el sistema; este archivo dice por qué.

En cada decisión, **Por qué** es el argumento de la elección. Los números (★9, 21, …) son los de la sección 5 del prompt inicial y de `DESIGN.md`.

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
| ★21b | Criterio por tiempo | Tiempo de vuelo entre choques < t_s = 0.057 s | Confirmada |
| ★22 | Parámetro principal | R_s, con t_s fijo | Confirmada |
| ★23 | Sincronización | Reloj dt + horizonte seguro | Confirmada |
| H1 | v_max de la garantía de la piel | 5 m/s (antes 3) | Confirmada (2026-11-08) |
| H2 | Grilla | Celdas ≥ 2 r_max + δ_out = 0.75 m en los tres esquemas | Confirmada (2026-11-08) |
| 2 | Parámetros | m = 80 kg, τ = 0.5 s, k_n = 1.2·10⁵ N/m; k_t = 0 (ver "Fricción tangencial") | Confirmada |
| F | Fricción tangencial en TSDMD | k_t = 0, sin T.3 | Confirmada (2026-11-08) |
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

**Idea:** agregar un criterio para pasar de EDMD a TSDMD que compare el tiempo entre choques con un tiempo característico del paso.

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

**Por qué:** la comparación con el step sigue presente, porque t_s ≈ 57 pasos a dt = 10⁻³ s. El umbral es un parámetro del motor.

**Consecuencias:**

- **Cambia el extremo de EDMD:** EDMD puro ahora es R_s = 0 **y** t_s = 0. Con t_s > 0, R_s = 0 es un híbrido válido.
- **Pasaje en cadena:** al pasar a TSDMD se revisan los vecinos con el contagio, para mantener la garantía de no perder contactos.
- Evita que una multitud fuera de la zona quede en EDMD con tasa de eventos enorme.

**Fuente:** Teorica_3, diapo 5; decisión del grupo. t_s sale de ω en ★9.

### ★23 — Sincronización

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

**Pregunta:** el criterio S2 pide g ≥ δ_out con **todos** los agentes, y el CIM solo ve las 3×3 celdas. Con celdas de 0.5 m, un agente fuera de las 3×3 puede estar a gap ≈ 0.

| Opción | A favor | En contra |
|---|---|---|
| **Celdas de 2 r_max + δ_out en los tres esquemas (elegida)** | Fuera de las 3×3 celdas el gap es ≥ δ_out: S2 y el horizonte de piel quedan completos. Misma grilla en los tres: el ítem c compara esquemas, no grillas, y los extremos dan bit a bit lo mismo que los puros | Más vecinos por celda (5 m² contra 2.25 m²) también en los puros |
| 0.75 m solo en el híbrido | Los puros quedan como estaban | Los extremos del híbrido no coinciden con los puros (caos con distinta grilla) y el ítem c mezcla grilla con esquema |
| Mirar 5×5 celdas en S2 | Grilla de 0.5 m | Más código; el horizonte de piel igual necesita la cota |

**Por qué:** es la única que da extremos exactos y una comparación limpia en el ítem c. El motor valida que el lado de celda alcance (`HybridScheme` corta si no).

### Cómo se implementa el chequeo de piel (E2)

| Pieza | Decisión | Por qué |
|---|---|---|
| Posición de los TSDMD durante los eventos | Están al inicio del paso: se resta v_max·(t − t_n) al gap | Los eventos se procesan antes del paso TSDMD |
| Agentes EDMD sin ningún TSDMD en sus 3×3 celdas | Sin chequeo de piel | Un EDMD lejos no cuesta nada |
| TSDMD que entra a una celda nueva, o agente que pasa a TSDMD | Al final del paso, sus vecinos EDMD a ≤ δ_in pasan a TSDMD (cadena) y los demás reprograman su chequeo | Antes de entrar estaba fuera de las 3×3: gap ≥ δ_out, y en un paso se mueve v_max·dt |
| Varios chequeos pendientes | Uno solo vivo por agente (sello `skinStamp`); no toca `revision` | El chequeo no cambia la velocidad: no debe invalidar los choques predichos |
| Verificación | Al pasar a TSDMD, el motor corta con error si el agente toca a un TSDMD | La garantía se comprueba, no se supone |

### Otras

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
