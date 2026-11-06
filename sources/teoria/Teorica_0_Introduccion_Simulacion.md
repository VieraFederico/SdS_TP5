# Clase Teórica 0 — Introducción: Sistemas, Modelos y Simulación

Fuente: `Teorica_0.pdf` (Simulación de Sistemas, 83 páginas; muchas diapos se construyen en varias páginas, así que la numeración del pie no coincide con la página del PDF).

**Versión filtrada para el TP5, Problema 2**: contiene solo lo que aplica a cómo organizar la simulación, el análisis y los resultados. Los encabezados `## Diapo N` usan el **número de página del PDF**; cuando una diapo se arma en varias páginas se agrupan (`Diapo 7–10`). Se omitieron: portada y separadores (1, 2, 6, 11–12, 33, 65, 74, 83), definición de sistema/modelo y objetivos de la teoría de sistemas (13–18, 25), espacio de estados y de fases (27–32), clasificación lineal/no lineal/caos y estados discretos/continuos (34–39), Monte Carlo (42–45), teoría de colas (52–55), cinematógrafo (56–61).

Las líneas marcadas `> Nota TP5:` no están en la clase; son aclaraciones para el TP5 (Problema 2).

---

## Diapo 3 — Dictado de la Materia (1)

- Las teóricas son guías que deben ser complementadas con la bibliografía.
- Hay flexibilidad para profundizar en los temas de mayor interés. Método científico.
- En particular, el Trabajo Práctico Final será a elección entre todos los temas vistos o temas relevantes propuestos por los alumnos.
- Se puede usar lenguaje de programación a elección.

---

## Diapo 4 — Dictado de la Materia (2)

- Los T.P. se realizan en forma grupal (2 o 3 personas). Sumarse a uno de los grupos creados en campus AAAAQQGG. ¿Agrupación random?
- Recordar y usar este nombre del grupo para toda comunicación con la Cátedra.
- Las presentaciones de los T.P. son de asistencia obligatoria.
- Los T.P. se entregan como actividad en Campus, subiendo presentación en pdf con links explícitos a animaciones, código, e informe cuando se requiera.
- La implementación del código y demás documentación debe ser original. Campus cuenta con detección automática de plagios. Hacer uso responsable de la IA.

---

## Diapo 5 — Dictado de la Materia (3)

- Consultar Cronograma y Reglamento completo (lectura obligatoria): ".../Contenido del Curso/Bienvenida/".
- Para Formato de Presentaciones e Informes consultar (lectura obligatoria): ".../Contenido del Curso/Bienvenida/Guías_Formato/".

> Nota TP5: esa guía de formato está convertida en `Guia_Presentaciones.md`.

---

## Diapo 7–10 — De Sistema Real a Simulación

Cadena (se arma en 4 pasos):

Sistema Real → Modelo Físico-Matemático → Modelo / Implementación Computacional → Simulación

> Nota TP5: las secciones de la presentación siguen esta cadena (ver `Guia_Presentaciones.md`, sección 2).

---

## Diapo 19 — Caracterización de un Sistema: tomar datos

¿Qué es tomar DATOS de un sistema? Medir y registrar, para un cierto muestreo temporal, los valores de las variables de INPUT y OUTPUT.

---

## Diapo 20 — Simulación off-line (IMPORTANTE)

[Diagrama:]
- INPUT y Parámetros → **SIMULACIÓN** → Estado del sistema en función del tiempo: **OUTPUT "PRIMARIO"**.
- OUTPUT primario → **Herramienta de ANÁLISIS** → "Observables".
- OUTPUT primario → **Herramienta de ANIMACIÓN** → "Videos".

La simulación, el análisis y la animación son programas separados: la simulación solo produce el estado del sistema en función del tiempo; observables y videos se calculan después a partir de ese output.

> Nota TP5: el enunciado lo exige: "la simulación debe generar un output en formato de archivo de texto. Luego el módulo de animación se ejecuta en forma independiente". El formato de los archivos está en `Teorica_1_Reglas_de_Simulacion.md`, diapos 36–37.

---

## Diapo 21 — Caracterización de un Sistema: gráfico input vs observable (IMPORTANTE)

[Figura: gráfico tomado de Soria et al., Safety Science 50 (2012). Eje vertical: "Observable (unidades)", de 30 a 100. Eje horizontal: "input o parametro (unidades)", de 20 a 110. Cuatro puntos (≈ 25, 86), (50, 58), (75, 47), (100, 60), cada uno con su barra de error vertical, unidos por segmentos rectos.]

Este es el formato del resultado final de un estudio: el observable escalar (promedio con barra de error) en función del input o parámetro, con ambos ejes nombrados y con unidades.

---

## Diapo 22 — OUTPUT primario → Observable primario → Observable escalar

- **OUTPUT primario**: serie temporal (ej.: velocidades).
- **Observable primario**: evoluciona en el tiempo (ej.: temperatura).
- **Observable escalar**: no depende del tiempo (ej.: temperatura en el equilibrio).

[Figura: "Observable* (u.a.)" vs "tiempo (u.a.)", para inputs y parámetros dados.]

---

## Diapo 23 — Ejemplo: curva de descarga y caudal

- **OUTPUT primario**: serie temporal (ej.: posiciones).
- **Observable primario**: evoluciona en el tiempo (ej.: curva de descarga).
- **Observable escalar**: no depende del tiempo (ej.: caudal $Q$).

$$Q = \frac{\Delta N}{\Delta t}$$

[Figura: "Nro. de Partículas (Observable*)" vs "tiempo (u.a.)": recta creciente; un triángulo marca $\Delta N$ y $\Delta t$ para calcular la pendiente $Q$. Para inputs y parámetros dados.]

> Nota TP5: en el Problema 2 el OUTPUT primario son las posiciones y velocidades; el observable primario es la curva de población N(t) (equivalente a la curva de descarga); los observables escalares son el tiempo de evacuación (y opcionalmente el caudal Q = pendiente de N(t)) y el tiempo de ejecución.

---

## Diapo 24 — Gráfico input vs observable (repetido)

Misma figura que la diapo 21 (IMPORTANTE): observable escalar con barras de error vs input o parámetro, con unidades.

---

## Diapo 26 — El Estado de un Sistema

Es la información necesaria tal que $y(t)$ queda unívocamente determinada por esta información y por $u(t)$, $t \geq t_0$.

Definimos esta información como el estado $x(t)$, donde sus componentes se denominan **variables de estado**.

La "Dinámica de un Sistema" está dada por las relaciones matemáticas del modelo entre los input ($u(t)$), los output ($y(t)$) y el estado ($x(t)$).

> Nota TP5: lo que se guarda en el archivo de output tiene que alcanzar para reconstruir el estado (posiciones, velocidades y, en CPM, radios) y así calcular cualquier observable o animación después.

---

## Diapo 40 — Modelos Deterministas y Estocásticos

- **Determinismo**: Demonio de Laplace. Conoce todas las condiciones iniciales y todas las leyes de la naturaleza, entonces puede determinar la evolución futura del universo (sistema).
- **Estocástico**: si al menos uno de los inputs es random. Se considera al azar (ignorancia sobre algunos procesos). Se plantea el modelo en términos de probabilidades.

---

## Diapo 41 — Modelo Estocástico

- Algoritmos que involucran números aleatorios: Monte Carlo ("Casino de Monte-Carlo", Mónaco).
- **Se toman promedios para reportar observables.**

> Nota TP5: las posiciones y radios iniciales son aleatorios; por eso el enunciado pide al menos 10 realizaciones y reportar promedios con barras de error.

---

## Diapo 46–49 — Simulaciones basadas en Tiempo Discreto

- El sistema evoluciona cada un cierto tiempo discreto fijo ($dt$), generalmente pequeño respecto del tiempo total de la evolución del sistema.
- Ejemplo: integración numérica de una ecuación diferencial.

> Nota TP5: es el TSDMD del Problema 2.

---

## Diapo 50–51 — Simulaciones basadas en Eventos

- El sistema evoluciona en la medida que suceden "eventos", los cuales son instantáneos y producen un cambio en el estado del sistema.

> Nota TP5: es el EDMD del Problema 2. El ejemplo de la clase (teoría de colas, diapos 52–55) se omitió.

---

## Diapo 62–64 — Simulación vs. Animación

- **Simulación computacional**: un programa que reproduce el comportamiento de un sistema (sistema físico, ecuaciones diferenciales, interacción de agentes, algoritmos, heurística).
- La simulación produce una **evolución temporal** (con la que se podría hacer una animación) u **otro tipo de output**.

¿Cuál es la diferencia?
1. Una ANIMACIÓN puede estar generada a mano o puede estar generada a partir de una SIMULACIÓN.
2. Una SIMULACIÓN puede generar una sucesión temporal de datos susceptible de ser animados.

---

## Diapo 66 — Consejos previos (lenguajes)

- Para simulaciones usar Java, C(++), o similar.
- Para análisis / postprocesamiento de datos salidos de la simulación usar Python, Matlab, R, Octave, ...
- No recomendable analizar datos con planillas (Excel o similar).

---

## Diapo 67–68 — Histograma, Distribución de Probabilidad y PDF

- **Histograma**: $y_i = N_i$. Los $y_i$ no están acotados. $dx_i$: ancho del bin.
- **Distribución de Probabilidad**: $y_i = N_i / N$. Todos los $y_i$ son menores a uno.
- **Densidad de Probabilidad (PDF: Probability Density Function)**: $y_i = N_i / (dx_i\, N)$.
  - La PDF es continua; a partir de datos finitos se la puede aproximar.
  - Su integral es igual a uno. Algún $y_i$ particular podría ser mayor a uno.

[Figura: histograma de tiempos de viaje (minutos) con bins de ancho variable.]

---

## Diapo 69 — Error Muestral o de Medición

Para simulaciones estocásticas se repiten corridas (con distintas semillas) un cierto número de realizaciones y luego se reporta el observable como el promedio ($\mu$) de los observables obtenidos. Su error asociado usualmente es el desvío estándar ($\sigma$) (si se trata de una distribución de Gauss):

$$\mu \pm \sigma$$

---

## Diapo 70–72 — Cifras significativas (IMPORTANTE)

Ejemplo de output promedio:

$$L = 45.4 \pm 0.3 \text{ cm}$$

Si el error es 0.3 cm no tiene sentido informar mayor precisión en el valor de L. Por ejemplo, ~~$L = 45.423457 \pm 0.323428$ cm~~ **no** sería un formato correcto.

---

## Diapo 73 — Barras de error

[Figura: la misma de la diapo 21: "observable (unidades)" vs "input o parametro (unidades)", puntos promedio con barras de error.]

---

## Diapo 75 — Regresiones: ajustar con modelos teóricos

Ajuste de datos con modelos teóricos (no con polinomios, "splines", o funciones arbitrarias...).

---

## Diapo 76–78 — Ejemplos de ajuste

[Figuras: tres gráficos "output [unidades]" vs "input o parametro [unidades]", con "Datos (promedios de simulaciones)" como puntos azules y el ajuste como curva roja, con leyenda:]
- Modelo exponencial (datos que decaen de ~21 a 0).
- Modelo senoidal.
- Modelo lineal (5 puntos de x = 0 a 4).

---

## Diapo 79–80 — Error del ajuste

Dados los datos $(x_i, y_i)$ (promedios de simulaciones) y un modelo teórico $f(x_i, c)$ (lineal u otro cualquiera), se puede definir el error del modelo en función de un coeficiente del mismo:

$$E(c) = \sum_i \left[y_i - f(x_i, c)\right]^2$$

El valor del coeficiente $c$ que minimiza el error $E$ es el que mejor ajuste del modelo a los datos produce.

[Figura: curva $E(c)$ con forma de parábola; el mínimo $E(c^*)$ está en $c = c^*$.]

---

## Diapo 81 — Ejemplo con el modelo lineal

[Figura: datos del modelo lineal con varias rectas de prueba punteadas (distintas pendientes) y la recta de mejor ajuste sólida.]

Por ejemplo, en el modelo lineal se prueban valores del coeficiente (la pendiente) y se elige el que minimiza $E(c)$.

> Nota TP5: la guía de presentaciones (2.4.5) pide mostrar cómo se halló el mejor ajuste en estos términos, por ejemplo graficando $E(c)$. Aplica si se ajusta una recta a N(t) para obtener el caudal, o una ley de potencias al escalamiento del ítem d.

---

## Diapo 82 — Reiteramos

Los datos se ajustan con funciones que provienen de algún modelo teórico. No con funciones arbitrarias (no con polinomios de grado N, "splines", etc.).
