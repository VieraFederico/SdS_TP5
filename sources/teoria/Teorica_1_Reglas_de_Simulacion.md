# Clase Teórica 1 — Sistemas Físicos, Cell Index Method y Reglas Generales de Simulaciones

Fuente: `Teorica_1.pdf` (Simulación de Sistemas, 47 diapositivas).

**Versión filtrada para el TP5, Problema 2**: contiene solo lo que aplica al problema. Los encabezados `## Diapo N` usan el número de diapositiva original. Se omitieron: portada, sistemas de muchas partículas y materia activa (ejemplos de galaxias, granulares, bacterias, aves, peatones, hormigas, comportamiento emergente, cine) (1–17), el enunciado del TP de lista de vecinos y su visualizador (28–30), la caricatura "practicar la charla" y el texto del artículo de Garland (43) y el FIN (47).

Las líneas marcadas `> Nota TP5:` no están en la clase; son aclaraciones para el TP5 (Problema 2).

---

## Diapo 18 — Muchas Partículas Interactuantes

- Todos los sistemas vistos hasta ahora consisten en partículas que interactúan entre sí de a pares y en función de las distancias.
- Para interacciones de largo alcance se deben calcular las distancias entre todas las partículas.
- Para interacciones de corto alcance solo son relevantes las distancias a los vecinos cercanos.

---

## Diapo 19 — Detección de vecinos: Cell Index Method (CIM)

Referencia: "Computer simulation of liquids", Allen & Tildesley, 1987.

- El método de fuerza bruta mide las distancias de todas las partículas con todas las partículas. La complejidad crece $\sim N^2$.
- Usando el CIM la complejidad crece lineal con $N$ (a densidad constante). (Si se aumenta la densidad, crece cuadrático con un prefactor menor.)

> Nota TP5: relevante para el ítem d (escalamiento con N a densidad constante vs a L constante).

---

## Diapo 20 — ¿Qué se quiere averiguar?

La identidad de las partículas que están a distancia menor a $r_c$.

[Figura: una partícula roja con un círculo de radio $r_c$; algunas partículas quedan dentro y otras fuera.]

---

## Diapo 21 — Idea general del CIM

Consiste en dividir el espacio en celdas, asignar las partículas a las celdas según su ubicación y calcular distancias solo entre partículas de celdas vecinas, y la propia.

---

## Diapo 22 — Tamaño de celda incorrecto

Si el dominio tiene lados de longitud $L$ y $M \times M$ celdas, entonces $L/M$ es la longitud del lado de cada celda.

[Figura: grilla de 5×5 celdas numeradas 1 a 25 con un círculo de radio $r_c$ que se extiende más allá de las celdas vecinas.] Este $M$ sería incorrecto para el radio $r_c$. ¿Por qué? (Porque el círculo de radio $r_c$ alcanza celdas que no son vecinas inmediatas.)

---

## Diapo 23 — Tamaño de celda correcto

Si disminuyo $M$:

$$L / M > r_c \quad (\text{radio de interacción de las partículas})$$

[Figura: grilla de 4×4 celdas (numeradas 6–9, 11–14, 16–19, 21–24) donde el círculo de radio $r_c$ centrado en la celda 12/13 queda contenido en las celdas vecinas.] Este $M$ sería correcto para el radio $r_c$.

> Nota TP5: con contacto entre discos, $r_c$ debe cubrir la mayor distancia a la que puede haber interacción (p. ej. $2 r_{max}$, más cualquier margen que use el criterio de pasaje del esquema híbrido).

---

## Diapo 24 — Asignación de celdas

Identificar a qué celdas pertenecen todas las moléculas es rápido y se podría hacer en todos los pasos temporales.

---

## Diapo 25 — Simetría

Simetría ($d_{ij} = d_{ji}$). Para cada celda (por ejemplo la 13) mirar solo las 4 celdas vecinas (9, 14, 19 y 18). Esto reduce a la mitad el tiempo de cálculo.

[Figura: grilla 5×5; la celda 13 resaltada y sus vecinas 18, 19, 14 y 9 (patrón en "L": arriba, arriba-derecha, derecha, abajo-derecha).]

---

## Diapo 26 — Condiciones Periódicas de Contorno

Por ejemplo, la partícula en celda 10 es vecina de la que está en la celda 6 y su distancia es del orden de $L/M$.

[Figura: grilla 5×5 con flechas que conectan bordes opuestos.]

> Nota TP5: en el Problema 2 hay paredes, **no** contorno periódico.

---

## Diapo 27 — Lista de Vecinos: ejemplo de formato

| id de la partícula "i" | ids de las partículas cuyas distancias son menores que $r_c$ |
|---|---|
| 1 | 5, 17, 32 |
| 2 | – |
| 3 | 8, 12 |
| 4 | – |
| 5 | 1, 6, 25, 104, 67 |
| ... | ... |

---

## Diapo 31 — Sección: Reglas Generales de Simulaciones

(Diapositiva de título de sección.)

---

## Diapo 32 — Esquema simulación / análisis / animación (IMPORTANTE)

[Diagrama:]
- INPUT y Parámetros → **SIMULACIÓN** → Estado del sistema en función del tiempo: **OUTPUT**.
- OUTPUT → **Herramienta de ANÁLISIS** → "Observables".
- OUTPUT → **Herramienta de ANIMACIÓN** → "Videos".

---

## Diapo 33 — Animaciones

- La animación es un resultado (postproceso) **separado** de la simulación.
- El simulador genera como outputs **archivos con posiciones y velocidades**.
- Luego el visualizador levanta esos datos y genera la animación (exportar a un avi).

---

## Diapo 34 — Visualizadores recomendados

- Matlab / Octave.
- Matplotlib (Python).
- Ovito (www.ovito.org). Admite formatos de archivo similares a los descriptos.
- Otro.

---

## Diapo 35 — Resultados (IMPORTANTE)

[Diagrama:] Herramienta de ANÁLISIS → "Observables" y Herramienta de ANIMACIÓN → "Videos"; juntos forman los **Resultados**.

---

## Diapo 36 — Formato de archivo: Info Dinámica

Formato de archivos para guardar simulaciones y su posterior visualización.

**Info Dinámica** (el nro. de fila es la identidad de la partícula 1, 2, ..., N):

```
t1
x1 y1 vx1 vy1      (partícula 1)
x2 y2 vx2 vy2      (partícula 2)
....
xN yN vxN vyN      (partícula N)
t2
x1 y1 vx1 vy1      (partícula 1)
x2 y2 vx2 vy2      (partícula 2)
....
xN yN vxN vyN      (partícula N)
```

> Nota TP5: en el Problema 2 la cantidad de partículas dentro de la sala cambia (se retiran a 1 m de la puerta) y en CPM el radio varía en el tiempo; hay que decidir cómo reflejarlo (por ejemplo, agregar columnas de id, radio y esquema EDMD/TSDMD por fila, o mantener la fila con un estado "afuera"). Los tiempos de salida de cada agente se guardan aparte con precisión dt (Clase 5, diapo 21).

---

## Diapo 37 — Formato de archivo: Info Estática

**Info Estática** = constante en el tiempo. (El nro. de fila es la identidad de la partícula 1, 2, ..., N.)

```
N        (Heading con el Nro. total de Partículas)
L        (Longitud del lado del área de simulación)
r1 c1    (radio y color de la partícula 1)
r2 c2    (radio y color de la partícula 2)
....
rN cN    (radio y color de la partícula N)
```

> Nota TP5: con SFM los radios son fijos (U[0.18, 0.25] m) y van en este archivo; también la geometría (L, ancho y posición de la puerta) y los parámetros de la corrida.

---

## Diapo 38 — Entregables de los Trabajos Prácticos (TP2 en adelante)

- Código fuente de las simulaciones implementadas.
- Soporte de la presentación oral en formato *.PDF (solo con imagen ilustrativa de las animaciones y un link visible).
- Las animaciones solo se muestran durante la presentación oral, pero **no** deben ser entregadas ni como archivo independiente, ni insertadas en el *.PDF.

---

## Diapo 39 — Sección: Presentaciones, Formato

(Diapositiva de título de sección.)

---

## Diapo 40 — Presentaciones: tiempos y partes

Tiempo ≤ 15 minutos. 3 personas → 3 partes ... ¡Random!

- **Intro** (< 1 min). Descripción del sistema y modelo matemático.
- **Implementación** (~3 min). Arquitectura, UML, pseudocódigo.
- **Simulaciones** (~2 min). Configuración del sistema particular a simular, parámetros fijos y variables, definición de outputs y observables.
- **Resultados** (~8 min).
  - Animaciones.
  - Estudio paramétrico / estadístico.
- **Conclusiones** (< 1 min).

> Nota TP5: el TP5 dura 13 minutos y pide dedicarle más tiempo de lo habitual a la descripción matemático-computacional del esquema híbrido.

---

## Diapo 41 — Presentaciones: formato

- Numerar las diapositivas.
- Usar carátulas / separadores para cada sección.
- Estructurar de forma coherente títulos y subtítulos.

---

## Diapo 42 — Algunos consejos (Garland)

James C. Garland, "Advice to beginning physics speakers", Physics Today (1999).

- Cumplir con el tiempo establecido.
- Usar un mínimo de ecuaciones.
- No escribir mucho texto.

---

## Diapo 44 — Interactuar con la audiencia

- Hablar alto.
- Mirar a los ojos a personas en distintos sectores de la sala.
- No mirar el piso o las paredes...

---

## Diapo 45 — Preguntas de la audiencia

- Permitir que quien pregunta termine la pregunta.
- Repetir la pregunta en voz alta para que todos la oigan.
- Respuestas breves, no hablar de otros temas relacionados.

---

## Diapo 46 — Varios expositores

- Cada uno tiene un tiempo definido.
- No superponerse.
- Responder preguntas por orden.
