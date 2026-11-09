# Módulo de simulación (Python)

Orquesta el motor Java (`experiment/src`), lee sus archivos de texto y calcula los observables, las figuras y las animaciones. Python nunca integra física y el motor no calcula ningún observable: el motor escribe el estado, el análisis lo lee [Teorica_0, diapo 20; Correcciones_TP4, Código]. La única medida que toma el motor es el wall-clock del ítem c.

## Uso

Requiere Python 3.13+ y un JDK 21+ (`javac` y `java` en el PATH o en `JAVA_HOME`). Desde `simulation/`, una sola vez:

```sh
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements.txt
```

Después, siempre con el Python del venv:

```sh
.venv/bin/simulation                          # menú interactivo
.venv/bin/simulation <estudio>                # sesión nueva con los parámetros del TP
.venv/bin/simulation <estudio> --quick        # prueba corta
.venv/bin/simulation <estudio> --analyze-only # re-analiza la última sesión
.venv/bin/simulation <estudio> --session <nombre>   # retoma una sesión (completa lo que falte)
.venv/bin/simulation <estudio> --help         # parámetros y valores por defecto
.venv/bin/simulation all [--quick] [--no-animations] [--studies timestep,schemes]
.venv/bin/simulation animate <run-NNN>        # MP4 y PNG de una corrida existente
.venv/bin/simulation figures [--studies …]    # copia las figuras a presentation/figuras/<ítem>/
.venv/bin/simulation build --check            # recompila el motor y corre EngineChecks
```

Cada parámetro de un estudio es un flag (`--dts 1e-3,1e-4`, `--zone-radii 0,5,56`, `--max-time 1000`). `--simulate-only` y `--analyze-only` separan las etapas. `--jobs` fija cuántos procesos Java van en paralelo; `runtime` y `scaling` usan siempre uno, para no contaminar los tiempos. El motor se recompila solo cuando cambia alguna fuente Java.

| Estudio | Ítem | Qué hace | Figuras |
|---|---|---|---|
| `timestep` | dt | TSDMD puro, k_t = 0, N = 200, dt ∈ [10⁻⁴, 2.5·10⁻³] s, M = 10 | ⟨t_evac⟩ vs dt con la tolerancia (±σ del menor dt) y el dt elegido; ⟨Q⟩ vs dt; N(t) de una realización por dt |
| `schemes` | a, b | Los tres esquemas con las mismas M = 10 semillas, híbrido con R_s = 5 m | a: fracción de agentes en cada esquema (una realización) y ⟨f_TS⟩(t); b: N(t) de una realización y ⟨N(t)⟩ ± σ por esquema, ⟨t_evac⟩ y ⟨Q⟩ por esquema, con la diferencia respecto de TSDMD puro |
| `runtime` | c | Wall-clock de TSDMD puro, EDMD puro y el híbrido con R_s ∈ {0, …, 56} m; en serie, con calentamiento y dt₂ = 5 s | wall-clock, ⟨t_evac⟩ y ⟨f_TS⟩ vs R_s con bandas de los esquemas puros; wall-clock de los tres esquemas |
| `scaling` | d | N ∈ {100, …, 1000} a densidad inicial constante (L = √(N/0.2)) y a L = 50 m; M = 3, N = 500 una sola vez (misma sala), EDMD hasta N = 500, v_max = 8 m/s en el híbrido | wall-clock vs N (log-log) y ⟨t_evac⟩ vs N, una figura por variante |
| `animation` | a | Una realización por esquema con dt₂ = speed/fps | MP4 (para YouTube), fotograma PNG y PDF de fotogramas para `\figanim` en vivo |

**Corridas que no terminan.** Los atascos son un resultado válido [Consideraciones, iii]. El motor corta en el tope de seguridad (1000 s con N = 200 en a–c; 1.5 s × N en d) y escribe `complete false`. Cada estudio lista esas corridas con cuántos agentes salieron y cuándo fue el último cruce, las cuenta en las tablas (columna `complete`) y las deja en `runs.csv`; los promedios usan las completas y dicen cuántas son.

## Resultados

```
output/<ítems>-<estudio>/<AAAA-MM-DD_HH-MM-SS>[_quick]/   (dt-timestep, ab-schemes, c-runtime, d-scaling, a-animation)
  config.json                  parámetros de la sesión
  runs/<caso>/case.json        parámetros con los que se simuló el caso
  runs/<caso>/run-NNN/         una realización (semillas consecutivas)
  figures/<ítem>/*.png         figuras de la presentación (ítem: dt, a, b, c, d)
  figures/anim_lite/*.pdf      fotogramas para la versión en vivo
  animations/*.mp4             videos
  *.csv                        observables detrás de cada figura y una fila por corrida
```

`simulation figures` copia `figures/` de la última sesión completa (nunca una `_quick`) de cada estudio a `presentation/figuras/`, así `\figres{<ítem>/<nombre>.png}` no cambia.

## Formato de los archivos del motor

Ver `DESIGN.md`, sección 4. Por realización: `static.txt` (N, L, d y `r c` por agente), `dynamic.txt` (cada dt₂: `t` y las N filas `id x y vx vy scheme location`), `exits.txt` (`id t_door t_retire`, NaN si no ocurrió) y `params.txt` (`clave valor`, con `complete`, `steps`, `events` y `elapsed_ns`).

## Observables

| Observable | Definición | De |
|---|---|---|
| N_in(t) | N − #{i : t_door,i ≤ t} | `exits.txt` |
| t_evac | max_i t_door,i (no existe si alguien no cruzó) | `exits.txt` |
| f_TS(t) | N_TS(t) / N_in(t) entre los agentes adentro | `dynamic.txt` |
| f̄_TS | promedio temporal de f_TS sobre los cuadros con agentes adentro, por realización | `dynamic.txt` |
| Q | (k₉₀ − k₁₀) / (t₍k₉₀₎ − t₍k₁₀₎) sobre los cruces ordenados | `exits.txt` |
| T_ej (wall-clock) | `elapsed_ns` del motor | `params.txt` |

Promedios y σ con M − 1 sobre las realizaciones completas [Teorica_0, diapo 69].

## Código

| Módulo | Responsabilidad |
|---|---|
| `engine.py` | Compila e invoca el motor; casos, caché y ejecución en paralelo |
| `sessions.py` | Carpetas de sesión, `config.json`, `latest` |
| `reader.py` | Lectura y validación de `static`, `dynamic`, `exits` y `params` |
| `observables.py` | N(t), t_evac, f_TS, Q, superposición máxima |
| `stats.py` | μ ± σ, cifras significativas, ajuste de un parámetro |
| `plotting.py` | Estilo común de las figuras (guía y devoluciones por defecto) |
| `animation.py` | Video, fotograma y PDF de una corrida guardada |
| `export.py` | Copia de figuras a la presentación |
| `options.py` | Parámetros de cada estudio como flags y preguntas interactivas |
| `studies/` | Un módulo por estudio: casos a simular y análisis |
| `cli.py`, `batch.py` | Menú interactivo, subcomandos y corrida completa |
