# Clase Teórica 6 — Simulación de Multitudes I

Fuente: `Teorica_6.pdf` (Simulación de Sistemas, 58 diapositivas).

**Versión filtrada para el TP5, Problema 2**: contiene solo las 21 diapositivas pertinentes al problema, con su numeración original. Se omitieron 37 diapos (portadas, separadores de sección y temas que no usa el Problema 2): 1–3, 6–19, 21, 24–27, 30, 32, 35–44, 54–55, 58.

Las fórmulas que en el PDF son imágenes están transcriptas en notación LaTeX. Las figuras se describen entre corchetes `[Figura: …]`.

Las líneas marcadas `> Nota TP5:` no están en la clase; son aclaraciones para el TP5 (Problema 2).

---

## Diapo 4 — Evacuación de un recinto (condiciones competitivas)

- Evacuación de un recinto.
- Simulaciones microscópicas (cuando se propuso: sin validación experimental).

[Figura: animación/instantánea en t = 0.1 s de ~200 partículas rojas distribuidas al azar en un recinto cuadrado de 20 × 20 m con una puerta en el centro de la pared inferior.]

---

## Diapo 5 — El SFM predice cualitativamente el efecto "Faster is Slower" (FIS)

[Figura: tiempo medio de evacuación (s) en función de la velocidad deseada (m/s), para 200 personas y para las primeras 180 y 160. El tiempo baja de ~145 s (v_d ≈ 0.8 m/s) a un mínimo de ~50–70 s cerca de v_d ≈ 1.5–2 m/s y luego crece hasta ~80–90 s para v_d = 8 m/s. Barras de error en cada punto.]

Texto del paper en la diapo: "…Further information can be gained if we look at the discharge curve, i.e., the number of particles that have left the room as a function of time. In this curve horizontal lines denote the time difference between two successive particles which leave the room. These time differences will be referred as 'delays'. […] It can be easily recognized that at larger values of v_d the occurrence of abnormally large delays becomes more probable particularly in the early stages of the evolution of the system." Fig. 1: "Mean evacuation time as function of the v_d for all the 200 people and for the first 180 and 160 persons."

---

## Diapo 20 — Principales observables en dinámica peatonal

- **Diagrama Fundamental**: densidad versus velocidad.
- **Caudal Específico** = Peatones / (tiempo × ancho puerta).

---

## Diapo 22 — Observable 2: Caudal Específico

- Regulaciones adoptan un caudal específico de **1.33 p/m/s**.
- Datos experimentales: **1.25 a 2.0 p/m/s**.
- Para condiciones normales se asume caudal de salida proporcional al ancho de salida.

---

## Diapo 23 — Caudal Específico: tabla de fuentes

| Caudal específico (pers./m/s) | Fuente |
|---|---|
| 1.33 | Department of The Environment and The Welsh Office, The Building Regulations — Approved Document B, Section B1, 1992. HMSO, London, 1991, pp. 9–40. |
| 1.37 | J. Fruin, Pedestrian Planning and Design. Metropolitan Association of Urban Designers and Environmental Planners, New York, 1971. |
| 1.82 | The Green Guide "Guide to Safety at Sports Grounds". HMSO, London, 1991. |
| 1.48 – 1.92 | B. D. Hankin and R. A. Wright, Passenger flow in subways, Operational Res. Quart. 9 (1958) 81-8. |
| 1.25 – 1.4 | D. Rasbash, G. Ramachandran, B. Kandola, J. Watts and M. Law, Evaluation of Fire Safety, Wiley (2004). |
| 1.77 | S. P. Hoogendoorn and W. Daamen, Pedestrian Behavior at Bottlenecks, Transportation Science 39(2) 147-155 (2005). |
| 1.33 | IMO Correspondence Group, "Interim guidelines for evacuation analyses for new and existing passenger ships", MSC/Circ.1033, IMO (2002). |
| 1.9 | T. Kretz, A. Grünebohm and M. Schreckenberg, Experimental study of pedestrian flow through a bottleneck, J. Stat. Mech. (2006) P10014. |
| 1.97 | A. Seyfried, T. Rupprecht, O. Passon, B. Steffen, W. Klingsch and M. Boltes, New insights into pedestrian flow through bottlenecks, arXiv:physics/0702004v2 (2007). |

---

## Diapo 28 — Social Force Model: ecuación de movimiento

Referencia: Helbing, Dirk, Illés Farkas, and Tamás Vicsek. "Simulating dynamical features of escape panic." Nature 407, no. 6803 (2000): 487-490.

Ecuación de Newton para cada peatón $i$:

$$m_i\, \ddot{\mathbf{r}}_i = \mathbf{F}_{GRANULAR} + \mathbf{F}_{SOCIAL} + \mathbf{F}_{DESEO}$$

- Sistema de N ecuaciones diferenciales acopladas.
- Métodos de Dinámica Molecular.

> Nota TP5: el enunciado pide SFM **sin fuerza social**: $\mathbf{F}_{SOCIAL} = 0$.

---

## Diapo 29 — SFM: Fuerza de Contacto (Granular)

$$\mathbf{F}_{Gi} = \sum_{j=1,\, j\neq i}^{N_p} \left[ \left(-\varepsilon_{ij}\, k_n\right) \mathbf{e}^n_{ij} + \left(v^t_{ij}\, \varepsilon_{ij}\, k_t\right) \mathbf{e}^t_{ij} \right] g(\varepsilon_{ij})$$

$$\varepsilon_{ij} = r_{ij} - (R_i + R_j)$$

- $r_{ij}$: distancia entre centros. $R$: radio.
- $\varepsilon_{ij} < 0$ cuando hay superposición. $g(x) = 1$ si hay contacto (superposición) y $0$ si no.
- $v^t_{ij}$: velocidad relativa tangencial.

[Figura: dos discos superpuestos; $r_{ij}$ es la distancia entre centros y la superposición está marcada en rojo.]

> Nota TP5: es la misma fuerza que N.2 + T.3 de la Clase 5 (diapo 15) con $\xi = -\varepsilon$. En Helbing (2000), $\mathbf{e}^n_{ij}$ apunta de $j$ a $i$ y $v^t_{ji} = (\mathbf{v}_j - \mathbf{v}_i)\cdot \mathbf{e}^t_{ij}$.

---

## Diapo 31 — SFM: Fuerza de autopropulsión (driving force)

$$\mathbf{F}_{Di} = m_i \frac{\left(v_{di}\, \mathbf{e}^{target}_i - \mathbf{v}_i\right)}{\tau_i}$$

[Figura: partícula con velocidad actual $\mathbf{v}$ (azul) y versor $\mathbf{e}^{target}$ (verde) apuntando al Desired Target X.]

---

## Diapo 33 — SFM: Evacuación de un recinto, "Faster is Slower"

El SFM predice cualitativamente el efecto "Faster is Slower".

| Parámetro | Valor |
|---|---|
| Nro. de peatones | 200 |
| Habitación | 20 m × 20 m |
| Diámetro de peatones | 0.50 – 0.58 m |
| Ancho puerta | 1.2 m |
| Velocidad deseada (variada) | entre 0.8 y 6 m/s |
| A | 2000 N |
| B | 0.08 m |
| $k_n$ | $1.2\cdot10^5$ N/m |
| $k_t$ | $2.4\cdot10^5$ kg/m/s |
| $\tau$ | 0.5 s |

[Figura: recinto con una puerta; partículas rojas formando un semicírculo apretado frente a la puerta y algunas ya afuera.]

> Nota TP5: la masa en Helbing (2000) es $m = 80$ kg. En el TP: radios $r \sim U[0.18, 0.25]$ m y $v_d = 1.5$ m/s.

---

## Diapo 34 — SFM: curva "Faster is Slower"

[Figura: Mean Evacuation Time (s) vs Desired Velocity (m/s), para 200, 180 y 160 personas. Mínimo cerca de 1.5–2 m/s (~50–70 s); a v_d = 0.8 m/s ≈ 90–145 s; a v_d = 8 m/s ≈ 77–89 s.]

---

## Diapo 45 — CPM: radio variable y velocidad deseada

Referencia: Baglietto & Parisi (2011).

- Partículas con radio variable ($r$):
  - $r_{max}$: espacio personal para desplazarse.
  - $r_{min}$: máxima compresión corporal.
- Velocidad deseada es función del radio $r$:

$$\lvert \mathbf{v}_d \rvert = f(r) \quad \text{tal que} \quad \begin{cases} v_d(r_{min}) = 0 \\ v_d(r_{max}) = v_{d\,max} \end{cases}$$

[Figura: círculo de radio $r$ entre los círculos punteados $r_{min}$ y $r_{max}$, con la flecha $\mathbf{V}_d$.]

---

## Diapo 46 — CPM: sin contacto

Sin contacto: avanzan con la $\mathbf{v}_d$ correspondiente.

[Figura: una partícula con $r$ intermedio entre $r_{min}$ y $r_{max}$ y su $\mathbf{v}_d$; otra con $r = r_{max}$ y su $\mathbf{v}_d$ (más larga).]

---

## Diapo 47 — CPM: cuando entran en contacto (1)

Cuando entran en contacto, en ese paso temporal…

[Figura: una partícula de radio $r$ y otra de radio $r = r_{max}$ superponiéndose.]

---

## Diapo 48 — CPM: cuando entran en contacto (2)

Sólo durante un paso $dt$:
- Aparece velocidad de escape ($\mathbf{v}_e$).
- El radio colapsa al mínimo.

[Figura: las dos partículas con $r = r_{min}$ y velocidades de escape $\mathbf{v}_e$ en sentidos opuestos, alejándose una de la otra.]

---

## Diapo 49 — CPM: evolución temporal, sin contacto

Si la partícula NO está en contacto:

$$\mathbf{x}(t + dt) = \mathbf{x}(t) + \mathbf{v}_d\, dt$$

$$\lvert \mathbf{v}_d \rvert = v_d = v_d^{max} \left[\frac{r - r_{min}}{r_{max} - r_{min}}\right]^{\beta}$$

$$r(t + dt) = r(t) + \frac{r_{max}}{\tau / \Delta t} \qquad (\text{si } r < r_{max})$$

---

## Diapo 50 — CPM: Desired Target

$$\mathbf{v}_d = v_d\, \mathbf{e}_{target}$$

$$\mathbf{e}_{target} = \frac{\mathbf{x}_{target} - \mathbf{x}}{\lvert \mathbf{x}_{target} - \mathbf{x} \rvert}$$

[Figura: partícula con $\mathbf{v}_d$ apuntando al Desired Target X.]

---

## Diapo 51 — CPM: evolución temporal, con contacto

Si la partícula SÍ está en contacto:

$$\mathbf{x}(t + dt) = \mathbf{x}(t) + \mathbf{v}_e\, dt$$

$$r = r_{min}$$

$$\lvert \mathbf{v}_e \rvert = v_e = v_d^{max}$$

> Nota TP5: según el paper, la dirección de $\mathbf{v}_e$ es la suma normalizada de los versores $\mathbf{e}_{ij} = (\mathbf{x}_i - \mathbf{x}_j)/\lvert\mathbf{x}_i - \mathbf{x}_j\rvert$ de todos los contactos (incluidas paredes). Valores del paper a verificar: $\beta \approx 0.9$, $\tau \approx 0.5$ s, $\Delta t = r_{min}/(2 v_e)$. En el TP: $r_{min} = 0.10$ m, $r_{max} = 0.25$ m. El enunciado aclara que en CPM el dt es parte de las reglas del modelo.

---

## Diapo 52 — CPM: Evacuación de un recinto

El modelo reproduce los datos experimentales de caudal específico.

[Figura: instantánea en t = 1 s de partículas del CPM en un recinto rectangular con una puerta en la pared inferior; cada partícula dibujada con su radio actual.]

---

## Diapo 53 — CPM: Pista de carrera

Variando el número de partículas se estudian distintas densidades y se mide la velocidad promedio para construir el diagrama fundamental.

[Figura: pista circular (radio interior 2 m, ancho 4 m) con partículas y flechas de velocidad deseada tangenciales.]

Texto del paper en la diapo (Baglietto & Parisi, PRE 83, 056117, 2011) — algoritmo por paso:
1. Iteración 1 sobre todas las partículas: encontrar contactos y calcular $\mathbf{v}_e$.
2. Iteración 2 sobre todas las partículas: ajustar radios según la regla de variación del radio.
3. Iteración 3 sobre todas las partículas: calcular la dirección y sentido de $\mathbf{v}_d$ considerando posiciones actuales y ubicación de los targets; calcular la magnitud de $\mathbf{v}_d$ según el radio (Ec. 1).
4. Iteración 4 sobre todas las partículas:
$$\mathbf{v}_i(t) = \begin{cases} \mathbf{v}_d^i & \text{si } r_i > r_{min} \\ \mathbf{v}_e^i & \text{si } r_i = r_{min} \end{cases} \qquad \mathbf{x}(t) = \mathbf{x}(t - \Delta t) + \mathbf{v}(t)\,\Delta t$$
5. Iteración sobre el tiempo.

"…the variable radius is, in principle, a property of the model not directly related to any physical property of the real system… The parameters of the model are $r_{min}$, $r_{max}$ and $\beta$, which can be tuned in order to reproduce empirical data."

---

## Diapo 56 — Bondades del CPM

- Reproduce valores experimentales de Caudal Específico y Diagrama Fundamental.
- Aumenta ~50× la velocidad de cómputo respecto del SFM.

---

## Diapo 57 — Desired Target (SFM y CPM)

Modelos: Social Force Model, Contractile Particle Model.

El Desired Target puede ser:
- Único.
- Una lista.
- Dinámico: función de posiciones y velocidades de vecinos y obstáculos.

[Figura: partícula con $\mathbf{v}_d$ apuntando a un target X.]

> Nota TP5: el Problema 2 necesita una lista de dos targets (la puerta, y luego un punto afuera).
