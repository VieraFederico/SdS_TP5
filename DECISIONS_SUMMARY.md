# Decisiones TP5: resumen rápido

> Qué está decidido, en una pantalla. El detalle y el porqué: `DECISIONS.md`.
> Última actualización: **2026-11-08**.

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
- **dt = 10⁻³ s** (**provisorio**), el valor final sale del estudio de dt
  - Estudio: dt ∈ {2.5·10⁻³, 10⁻³, 5·10⁻⁴, 2.5·10⁻⁴, 10⁻⁴}, M = 10
  - Criterio: el mayor dt con ⟨t_evac⟩ a menos de 1σ del de menor dt
- **dt₂ = 0.5 s** en estudios · **0.1 s** en animaciones · grande para medir wall-clock

---

## EDMD

**Choques**
- Restitución sacada del SFM: **e = 0.94** entre agentes, **0.92** con paredes
- Piso de separación: **v_sep = 0.02 m/s** (evita el colapso inelástico)
- Bordes de la puerta = discos de radio 0

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
- Escritura con evento cada dt₂
- Corte: `--max-time` / `--max-events` → `complete false`. **Que no termine es un resultado válido**

---

## Híbrido

**EDMD → TSDMD: alcanza con una**

| | Condición | Valor |
|---|---|---|
| E1 | Entra en la zona: r_i ≤ **R_s** | se barre |
| E2 | A ≤ **δ_in** de un agente TSDMD | **0.05 m** |
| E3 | Dos choques seguidos en menos de **t_s** | **0.057 s** (= π/ω, lo que dura un choque blando) |

**TSDMD → EDMD: tienen que cumplirse las tres**

| | Condición | Valor |
|---|---|---|
| S1 | r_i ≥ R_s + **h** | **1 m** |
| S3 | \|v − v_d ê\| ≤ **ε·v_d** | **ε = 0.05** |
| S2 | Gap ≥ **δ_out** con todos y sin tocar paredes | **0.25 m** |

**Parámetro principal: R_s**
- Barrido: **{0, 1, 2, 3, 5, 8, 12, 20, 35, 56} m**, con t_s fijo · default **5 m**
- **EDMD puro** = R_s 0 **y** t_s 0
- **TSDMD puro** = R_s 56 m
- Los dos extremos dan **bit a bit** lo mismo que los esquemas puros (testeado)

**Sincronización y garantía**
- Reloj global de a dt: **primero eventos, después un paso Beeman** de los TSDMD
- Los pasajes se aplican **al final del paso**, con **pasaje en cadena** por E2
- Fuerzas TSDMD **solo entre TSDMD**: nunca hay contacto EDMD–TSDMD
- Chequeo de piel con **horizonte seguro**: `(g − δ_in) / (|v_i| + v_max)`
- **v_max = 5 m/s** (el pico medido fue 3.29 m/s). Si se supera, el motor corta con error
- **Una sola grilla** para los tres esquemas: celdas ≥ 2 r_max + δ_out = **0.75 m**
- Al entrar a TSDMD: **a(t) = a(t − dt) = deseo**, aplicando antes la relajación pendiente

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
- Tope de seguridad: 10⁵ s

---

## Datos para tener en cuenta

- Con **k_t = 0 igual hay atascos** (N = 200, 50 semillas): TSDMD 10/50 · EDMD 8/50 · híbrido 5/50
- Con k_t = 2.4·10⁵ se trababan 5 de 6
- Casi nadie vuelve de TSDMD a EDMD: 0–5 agentes por corrida
- Con t_s > 0, la multitud pasa a TSDMD aunque R_s sea chico → meseta esperada en el ítem c
- Wall-clock con N = 200: TSDMD ≈ 0.5 s · híbrido ≈ 0.5–0.6 s · EDMD ≈ 1 s
- La Introducción tiene que explicar **cómo choca EDMD y cómo recupera v_d** (consideración i de la cátedra)

---

## Cómo trabajamos

- Los commits y las instalaciones los hace el grupo a mano
- Python solo desde `simulation/.venv` · Java con `javac --release 21`, sin Maven
- LaTeX se compila online
- El zip lleva solo el motor y se arma al final
- Entregas: `SdS_TP5_2026Q2G09CS_Presentación` y `SdS_TP5_2026Q2G09CS_Codigo`

---

## Pendiente

- [ ] Estudio de dt (fija el dt final)
- [ ] CLI de Python (etapa 5)
- [ ] Presentación (etapa 6)
