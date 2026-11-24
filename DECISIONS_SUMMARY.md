# Decisiones TP5: resumen rápido

> Qué está decidido, en una pantalla. El detalle y el porqué: `DECISIONS.md`.
> Última actualización: **2026-11-24** (híbrido con un solo criterio; choque del TP3 desde el 2026-11-20).

---

## Modelo

- **SFM sin fuerza social**
- **Sin fricción tangencial: k_t = 0**
- Fuerza = resorte normal + deseo: `−k_n ξ ê_n + m (v_d ê − v)/τ`

| Parámetro | Valor | De dónde |
|---|---|---|
| m | **80 kg** | Helbing 2000 |
| τ | **0.5 s** | Helbing 2000 |
| k_n | **1.2·10⁵ N/m** | Helbing 2000 |
| k_t | **0** | Consideraciones de la cátedra (iii) |
| v_d | **1.5 m/s** | Enunciado |
| Radios | **0.18–0.25 m** | Enunciado |
| v₀ | **v_d ê** (arrancan en vuelo libre) | Decisión 5 |

---

## Sala y targets

- Sala **[0, L]²**, puerta de **0.8 m** en y = 0, centrada en x = L/2
- **Target 1:** (L/2, **R_i**), para que la recta nunca toque la pared
- **Target 2:** (L/2, **−2 m**), cuando |x − L/2| < d/2 **e** y ≤ R_i
- **Retiro:** y = **−1 m**
- **N = 200** en los ítems a–c

---

## TSDMD

- Integrador: **Beeman predictor-corrector** (uno solo)
- **dt = 10⁻³ s** (elegido con el estudio de dt: pasan los cinco dt)
  - Estudio: dt ∈ {2.5·10⁻³, 10⁻³, 5·10⁻⁴, 2.5·10⁻⁴, 10⁻⁴}, M = 10
  - Criterio: el mayor dt con ⟨t_evac⟩ a menos de 1σ del de menor dt
- **dt₂ = 0.5 s** en estudios · **0.1 s** en animaciones · grande para medir wall-clock

---

## EDMD

**Choques** (recuperatorio, 2026-11-20: reemplaza e = 0.94 / 0.92 y v_sep)
- **Elástico, c_n = 1, masas iguales**: el choque del TP3. J = m (Δv·n̂); se intercambian las componentes normales, las tangenciales quedan
- Pared y bordes de la puerta: espejo, v' = v − 2(v·n̂)n̂. Bordes = discos de radio 0

**Recuperación de v_d: eventos fantasmas**
- Cada **Δτ = R_i / max(|v_i|, v_d)** (≈ 0.12–0.17 s), "lo que tarda en recorrer su radio"
- Entre fantasmas la velocidad **queda fija**
- En cada fantasma: solución exacta de la fuerza de deseo
  - la velocidad anterior decae todo el intervalo
  - cada impulso de choque decae **solo desde su instante**
- Un choque **no** reinicia el fantasma pendiente
- **Bounce back:** si |v − v_d ê| < **0.01·v_d**, se fija v = v_d ê y se cortan los fantasmas

**Motor**
- Avance perezoso (solo se mueven los agentes del evento) + **CIM con evento de cruce de celda**
  - El cruce se agenda **10⁻⁹ m** pasado el borde (una distancia, no un tiempo): un agente sobre un borde con v ≈ 0 no queda en un bucle (bug corregido el 2026-11-24)
- Escritura con evento cada dt₂
- Corte: `--max-time` / `--max-events` → `complete false`. **Que no termine es un resultado válido**

---

## Híbrido

**Un solo criterio** (corrección de la cátedra, 2026-11-24): el **tiempo entre contactos** comparado con **t_s**, en los dos sentidos. Reemplaza zona (E1), contagio (E2) y S1–S3.

| | Regla | Cuándo se evalúa |
|---|---|---|
| EDMD → TSDMD | Dos contactos (agente, pared o borde) a menos de **t_s** | Al procesar cada choque; pasa en el próximo `Step` |
| EDMD → TSDMD | Se superpone con un TSDMD (en cadena). No es otro criterio: un contacto blando dura, así que el próximo contacto está a 0 < t_s | En cada `Step` |
| TSDMD → EDMD | **t_s sin ningún contacto** (ni agentes ni paredes) | En cada `Step` |

- **t_s = π/ω = 0.057 s** (lo que dura un choque blando) por defecto · es el **único parámetro** del híbrido
- Una sola variable por agente: `lastContact` (cada choque en EDMD, cada `Step` con superposición en TSDMD). No se resetea al volver
- **Todos arrancan en EDMD.** Vuelven sin tocar a nadie, por construcción; los fantasmas relajan lo que quede de desvío

**Parámetro principal: t_s**
- Barrido del ítem c: **{0.001 (= dt), 0.003, 0.01, 0.03, 0.1, 0.3, 1, 3} s** + el por defecto, eje x log
- **EDMD puro** = t_s 0 (bit a bit, testeado)
- **Ningún t_s da TSDMD puro:** todos arrancan en EDMD y pasan en su segundo contacto. TSDMD puro va como banda de referencia (lo que pide el ítem c)

**Sincronización y contacto EDMD–TSDMD**
- **Reloj global = la cola de eventos.** El paso Beeman de los TSDMD es un evento `Step` en n·dt, solo mientras haya TSDMD
- Mismo instante: **eventos de agentes → Step → escritura**
- Los pasajes se aplican **en el Step**
- Nunca se predice un choque contra un TSDMD. **El contacto EDMD–TSDMD se ve en el Step**, como TSDMD ve sus propios contactos: el EDMD se superpone a lo sumo (|v_e| + |v_t|)·dt, unos mm (máximo medido 2.4 mm), y pasa a TSDMD
- Fuerzas TSDMD **solo entre TSDMD**. Al entrar: **a(t) = a(t − dt) = deseo**, aplicando antes la relajación pendiente → la fuerza de contacto arranca hasta 1 dt más tarde que en TSDMD puro
- **Una sola grilla** para los tres esquemas: celdas ≥ 2 r_max = **0.5 m**
- Ya no existen: zona R_s, h, piel δ_in, δ_out, ε, horizonte seguro, eventos Zona y Piel, **v_max**

---

## Archivos de salida

- `static.txt`: N, L, d y radio + esquema inicial por agente
- `dynamic.txt`: cada dt₂, **las N filas siempre**: `id x y vx vy scheme location`
  - scheme: 0 EDMD · 1 TSDMD
  - location: 0 adentro · 1 pasó la puerta · 2 retirado
- `exits.txt`: `id t_door t_retire`
- `params.txt`: parámetros, `complete`, `steps`, `events`, `elapsed_ns`
- **El motor no calcula observables** (la única excepción es el wall-clock)

---

## Análisis

- **t_evac** = cruce de la puerta del **último** agente
- **N(t)** = agentes adentro · **f_TS(t)** = N_TS / N adentro
- **M = 10** realizaciones, mismas semillas en los tres esquemas, σ con M − 1
- Ítem d: N ∈ **{100, 200, 300, 500, 700, 1000}**, a densidad constante (L = √(N/0.2)) y a L = 50 m
- **Informar siempre las corridas trabadas**, en los tres esquemas
- Tope de seguridad: 10⁵ s en el motor; **1000 s en los estudios con N = 200** (confirmado; 1.5 s × N en el ítem d): una corrida trabada llega siempre al tope
- **Q** = cruces por segundo entre el 10 % y el 90 % de los cruces (Q/d = caudal específico)
- Una corrida trabada es un resultado: el CLI no la repite; se informa con salidos, último cruce y tope
- **Todos los promedios (t_evac, T_ej, f̄_TS) usan solo las corridas completas**, con marcador hueco si alguna se trabó. f̄_TS de una trabada lo fija el tope; hasta trabarse evoluciona igual que las completas (medido, etapa 8)

---

## Datos para tener en cuenta

- Con **k_t = 0 igual hay atascos** (N = 200, 50 semillas, choque anterior): TSDMD 10/50 · EDMD 8/50 · híbrido 5/50
  - Híbrido de un criterio contra el anterior, con el choque del TP3: **9/50 contra 7/50** (es lo mismo)
- Con k_t = 2.4·10⁵ se trababan 5 de 6
- **Las corridas son caóticas:** cualquier cambio de redondeo (hasta dt₂) cambia qué semillas se traban. Solo se comparan estadísticas
- **Idas y vueltas:** con el criterio único, unas **2150 vueltas TSDMD → EDMD por corrida** (≈ 11 por agente, en el borde de la multitud). Antes, 0. Se controlan con t_s (t_s = 3 s: ninguna). Volver tras 2·t_s las reduce a la mitad pero no baja T_ej (descartado)
- Medido el 2026-11-24 (N = 200, otra máquina, solo para comparar): híbrido nuevo **1.37 ± 0.15 s**, anterior 1.13 ± 0.13 s · TSDMD ≈ 0.9 s · **EDMD ≈ 0.7 s** (con el choque del TP3 deja de ser el más caro). A confirmar con el ítem c
- ⟨f̄_TS⟩ ≈ 0.62 (antes ≈ 0.78) · t_evac del híbrido 75–93 s
- La Introducción tiene que explicar **cómo choca EDMD y cómo recupera v_d** (consideración i de la cátedra)

---

## CLI de Python

- `simulation/`, reutilizando el del TP4; `requirements.txt` idéntico
- Figuras en `<sesión>/figures/<ítem>/`; `simulation figures` las copia a `presentation/figuras/<ítem>/`
- Estilo común en `plotting.py` (guía y devoluciones por defecto)
- Carpetas: `output/dt-timestep`, `ab-schemes`, `c-runtime`, `d-scaling`, `a-animation`
- Figuras al estilo del TP4: ejes "palabras + símbolo + (unidad)", ⟨·⟩ en promedios, barras con capsize 3, marcador vacío + leyenda "Con atascos"
- Notación: **N_in(t)** agentes adentro (N es el total), **T_ej** tiempo de ejecución, f̄_TS fracción media en TSDMD
- Estudios: `timestep` (dt), `schemes` (a, b), `runtime` (c), `scaling` (d), `animation` (a)
- `runtime` barre **t_s** (`--flight-thresholds`); figuras `*_vs_ts` con eje log; `scaling` ya no usa v_max
- Wall-clock: en serie, 2 calentamientos, dt₂ = 5 s; las trabadas quedan fuera del promedio y se listan
- Animaciones: azul EDMD, rojo TSDMD; semilla 2 en el híbrido (la 1 se traba). Escenas: `tsdmd`, `edmd`, `hibrido`, `hibrido_ts_chico` (0.01 s), `hibrido_ts_grande` (1 s), `N100`, `N1000`, `criterio_E3` (se borraron E1 y E2; las semillas hay que revisarlas al volver a simular)

---

## Cómo trabajamos

- Los commits y las instalaciones los hace el grupo a mano
- Python solo desde `simulation/.venv` · Java con `javac --release 21`, sin Maven
- LaTeX se compila online
- El zip lleva solo el motor y se arma al final
- Entregas: `SdS_TP5_2026Q2G09CS_Presentación` y `SdS_TP5_2026Q2G09CS_Codigo`

---

## Pendiente

- [x] Estudio de dt: dt = 10⁻³ s
- [x] CLI de Python (etapa 5): los cinco estudios escritos y corridos (scaling: tope 1.5 s × N, M = 3, N = 500 compartido, EDMD hasta N = 500; el v_max = 8 m/s del híbrido ya no existe)
- [ ] Presentación (etapa 6): `presentation/main.tex` revisada (etapa 8) y conclusiones corregidas; faltan los links de YouTube y compilar online
- [x] Ítem d re-analizado sin simular (sesión 2026-11-13_15-23-29): mismo eje y en las dos variantes
- [x] Ítem c re-analizado sin simular (sesión 2026-11-13_15-09-41): ⟨f̄_TS⟩ solo sobre las completas, meseta 0.73–0.74
- [x] Documento de defensa: `notes/defensa_TP5.md` (teoría, decisiones, preguntas y guion)
- [x] Motor: choque del TP3 (2026-11-20) y criterio único (2026-11-24); 1 045 078 checks OK
- [ ] **Volver a simular** `schemes`, `runtime`, `scaling` y `animation` (una vez, para los dos cambios; `timestep` no hace falta)
- [ ] Elegir los t_s de las escenas de c y revisar semillas; `simulation figures`; subir los MP4 y actualizar los links
- [ ] Presentación: choque del TP3, un solo criterio, ítem c contra t_s, borrar E1/E2 y renombrar `criterio_E3`
- [ ] `DESIGN.md` (§2.4, §2.6, §2.7) y `notes/defensa_TP5.md` al día

## Recuperatorio: presentación (2026-11-20)

- Toda la teoría de EDMD (cuándo chocan, cómo chocan, paredes, fantasmas) pasa a la **Introducción**
- Pocas ecuaciones por diapo, **con dibujos que se revelan paso a paso**; mismo color para cada vector en dibujo y ecuación
- Fórmulas largas (SFM completo, t_c, J general, choques durante la recuperación, Beeman) al **Anexo**, después de "Muchas gracias", con botones de ida y vuelta
- Correcciones 2026-11-23: **híbrido en la Introducción**, **52 → 41 páginas** (fusiones y detalles al Anexo), **animaciones más grandes**
- Criterios de pasaje (2026-11-24): **uno solo**, el tiempo entre contactos con su recíproca (ver Híbrido). Detalle en `DECISIONS.md`, Etapa 10

## Notas del profesor (2026-11-13)

- Variables definidas donde aparecen; una diapo por condición (**hoy: un solo criterio**)
- **Saltos:** con los tres criterios, 197 ± 1 de EDMD a TSDMD y ninguna vuelta. Con el criterio único, ~2150 en cada sentido por corrida
- T_ej vs el parámetro también **sin EDMD** (escala)
- Paso TSDMD como evento de la cola: **misma dinámica byte a byte**, cambia solo T_ej
- `--initial` + escena `criterio_E3` con pocos agentes, sala de 10 m (E1 y E2 borradas)

## Cómo defender el híbrido (IMPORTANTE)

**En cada paso de TSDMD (`Step`)**
- Solo Beeman de los **TSDMD** (fuerzas entre TSDMD)
- Los **EDMD no se mueven**: su posición sale de la recta, en su próximo evento o en la escritura
- Se revisan los vecinos de cada TSDMD: el EDMD superpuesto pasa a TSDMD; el TSDMD sin contacto desde hace t_s vuelve
- Un paso cuesta según **N_TS**, no según N

**Lo predicho hacia un agente que pasa a TSDMD**
- j pasa a TSDMD → **se invalidan todos sus eventos predichos**; nunca se predice contra un TSDMD
- El EDMD i que iba a chocar con j **conserva el resto** de sus eventos
- **Si i toca a j, se ve en el próximo Step** (a lo sumo unos mm de superposición) y i pasa a TSDMD

**Preguntas probables**
- ¿Cuántos criterios? → **Uno**: tiempo entre contactos contra t_s, en los dos sentidos
- ¿Lo de tocar a un TSDMD no es el contagio? → No: no mira cercanía ni tiene parámetro; un contacto blando dura más que t_s
- ¿Por qué ningún t_s da TSDMD puro? → Todos arrancan en EDMD y pasan en su segundo contacto; TSDMD puro es la referencia

**No decir**
- ❌ "En cada Δt se actualizan todas las posiciones" → solo los TSDMD
- ❌ "Nunca hay superposición EDMD–TSDMD" → hay unos mm durante a lo sumo un dt, como en TSDMD
- ❌ "Mucho más rápido que TSDMD" → con N = 200 el híbrido **no** le gana a TSDMD (medido 1.37 s contra ≈ 0.9 s). Números finales: los del ítem c
