# Devolución de la cátedra — TP3 (Grupo 9)

**Tema del TP3: dinámica molecular regida por eventos (Event Driven, EDMD).**

Nota: **6**

Texto de la devolución recibida, con formato Markdown y erratas menores corregidas. "D*N*" se refiere a la diapositiva *N* de nuestra presentación del TP3. Al final hay una sección agregada por nosotros con lo que hay que aplicar en el TP5; no es parte de la devolución.

---

## Observaciones generales

- No repetir en todas las diapositivas (abajo) el nombre de los integrantes y el número de grupo (el cual se confunde con el número de la diapositiva).
- El tamaño de fuente de todas las figuras es ilegible.
- Leer la guía de presentaciones y seguirla.

---

## Por diapositiva

### D4 y D5 — Introducción con información del sistema particular (IMPORTANTE)

**IMPORTANTE: D4 y D5 incorrectas.** Esa información es parte del sistema particular. Acá va la teoría general, sin importar ninguna configuración particular que se pueda simular con ella.

### D6 y D7 — Teoría general

D6 y D7 serían las diapos correctas para esta sección, pero sin evaluar en $m_i = m_j$ (eso es particular del sistema que se estudia).

### D13 — Esquema del sistema

Acá iría un esquema del sistema a simular (el que está en la D9).

### D19 — Ítem 1.1

Para el ítem 1.1 faltó llegar a saturar el sistema con la ubicación de las partículas en la red hexagonal, como dijimos en clase.

### D20 — Orden de los resultados (IMPORTANTE)

¿Qué son estas curvas? ¿De dónde salieron?

**IMPORTANTE:** está en las guías de formato y ya lo corregimos y repetimos varias veces. La estructura para cada input que se estudia es mostrar en este orden:

1. Animación.
2. Evolución temporal.
3. Input vs observable escalar.

No podemos ir directo a 3 curvas superpuestas de inputs vs observable sin haber explicado nada de las configuraciones estudiadas.

### D21 — Animaciones del caso estudiado

Acá parece que se va a estudiar un caso simple, un obstáculo central de radio variable. Se pueden poner 2 animaciones aquí, con radio chico y grande, así queda claro qué estamos estudiando.

### D21 y D22 — Nombres arbitrarios y evoluciones temporales

- No usar nombres arbitrarios (de fantasía): A1, A2, ... si se lo puede caracterizar por el valor del radio, que es un parámetro entendible.
- En D22 se podría aclarar al costado si es una realización por radio.
- Tal vez son muchas curvas y no se distinguen. Recuerden que la evolución temporal es solo demostrativa: **no** tienen que mostrar las evoluciones temporales de todos los puntos estudiados, que sí se van a mostrar en la figura input vs observable escalar.

### D23, D26 y D27 — Nombres, valores sobre los puntos y leyendas

- Repetimos lo de no llamar con nombres arbitrarios: no agrega información; es más, confunde.
- La información útil ya está en los ejes (D26). De la misma forma, no agregar valores numéricos sobre los puntos.
- No hay que poner "Promedio ± desvío (M = 100)" en la leyenda. Ya sabemos qué representan el punto y la barra de error. El número de realizaciones se puede poner al costado.

### D24

Ídem D21.

### D25

Ídem D22.

### D26 — Nombre del eje

Acá llaman $\langle t_{90} \rangle$ al eje y, pero en la otra figura lo llamaron con todas las palabras. Elegir una.

### D28 y otras — Animaciones

- Los videos deben estar embebidos en la presentación oral.
- No más de dos animaciones por diapositiva; si no, quedan pequeñas.
- Por otro lado: "Caso nulo – Óptima", ¿y la de más a la derecha? ¿Qué quieren mostrar con eso?

### D29 — Semilla, leyendas y tamaño

- No decir qué número de semilla usaron: eso es random y es interno de ustedes. Sí está bien aclarar "una realización".
- Las leyendas van adentro de la figura.
- La figura podría ser más grande.

### D30 — Ajuste lineal

- La figura podría ser mucho más grande.
- Se pedía hacer el ajuste lineal con el método de la Teórica 0; para eso hay que mostrar que se minimiza el error del ajuste.

---

## Conclusiones

Las 3 primeras conclusiones no se entienden; la segunda no está basada en datos mostrados.

---

## Para el TP5 (agregado por nosotros, no es parte de la devolución)

- Pie de diapositiva: solo el número de diapositiva. No poner nombres ni número de grupo en cada diapo.
- Fuente de las figuras del mismo tamaño que el texto (≥ 20, Guía 1.8).
- **Introducción = modelo general.** Las ecuaciones de SFM/CPM y de EDMD/TSDMD en forma general. Nada de N = 500, L = 50 m, d = 0.8 m, radios ni masas iguales: eso va en la sección Simulaciones.
- En Simulaciones, un esquema de la sala (la Figura 1 del enunciado o una propia).
- Para cada estudio: **animación → evolución temporal (N(t), fracción por esquema) → input vs observable escalar**, en ese orden. Ver Guía 2.4.1–2.4.3.
- Rotular por el valor del parámetro (por ejemplo "R_s = 2 m", "N = 500", "TSDMD puro"), nunca con nombres como A1, A2 o "caso óptimo".
- Evoluciones temporales: solo ejemplos típicos o extremos. Aclarar al costado si es una realización.
- Sin valores numéricos sobre los puntos. Sin "promedio ± desvío (M = …)" en la leyenda; el número de realizaciones va al costado.
- Un solo nombre para cada eje en todas las figuras (por ejemplo, siempre "tiempo de evacuación (s)").
- Videos embebidos en la presentación oral; como mucho dos por diapositiva. Cada animación tiene que tener un propósito claro.
- No mencionar semillas. Leyendas dentro de la figura. Figuras grandes.
- Si se hace un ajuste (caudal, ley de potencias del ítem d), mostrar la minimización del error E(c) (Teórica 0).
- Conclusiones claras y basadas solo en resultados mostrados (Guía 2.5).
