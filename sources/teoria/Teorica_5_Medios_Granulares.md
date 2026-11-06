# Clase Teórica 5 — Medios Granulares

Fuente: `Teorica_5.pdf` (Simulación de Sistemas, 31 diapositivas).

**Versión filtrada para el TP5, Problema 2**: contiene solo las 16 diapositivas pertinentes al problema, con su numeración original. Se omitieron 15 diapos (portadas, separadores de sección y temas que no usa el Problema 2): 1, 3–4, 6, 13, 22–31.

Las fórmulas que en el PDF son imágenes están transcriptas en notación LaTeX. Las figuras se describen entre corchetes `[Figura: …]`.

Las líneas marcadas `> Nota TP5:` no están en la clase; son aclaraciones para el TP5 (Problema 2).

---

## Diapo 2 — Definición

- Sistema constituido por muchas partículas macroscópicas (granos, pellets, arena, polvo, etc).
- Interacciones a través de fuerzas de contacto normales (deformación viscoelástica) y tangenciales (rozamiento).
- Interacciones altamente disipativas: el sistema llega al reposo si no recibe energía del exterior (gravitatoria, vibración del contenedor, etc.) o propia (autopropulsión).
- Características muy particulares. Comportamiento distinto al de sólidos, líquidos y gases (¿nuevo estado de la materia?). Ejemplo: la presión NO depende de la altura de la columna.

---

## Diapo 5 — Propiedades: Ley de Beverloo (1961)

Caudal $Q$ constante de un silo, obtenido por análisis dimensional.

$$Q \approx n_p \sqrt{g}\,(d - c\,r)^{2.5} = B\,(d - c\,r)^{2.5}$$

- $n_p$: número de partículas por unidad de volumen.
- $d$: diámetro de la apertura de salida.
- $r$: radio medio de las partículas.
- $c$: constante.
- Exponente 2.5 para silo 3D (para 2D es 1.5).

> Nota TP5: el caudal de salida aproximadamente constante es la razón por la que la curva de población N(t) tiene un tramo recto.

---

## Diapo 7 — Simulaciones

- Sistemas densos: $t_{choque} \gg t_{vuelo}$.
- Método de Elemento Discreto: dinámica molecular para granulares.
- Uno de los modelos pioneros: Cundall y Strack (1979).

---

## Diapo 8 — Notación y variables geométricas (partícula–partícula)

| Símbolo | Significado |
|---|---|
| $\mathbf{g}$ | Fuerza de gravedad, apunta en $-\hat{y}$ |
| $\dot{\mathbf{r}} = \dfrac{d\mathbf{r}}{dt}$ | velocidad |
| $\ddot{\mathbf{r}} = \dfrac{d^2\mathbf{r}}{dt^2}$ | aceleración |
| $\dot{\mathbf{r}}^{c}_{rel} = (\dot{\mathbf{r}}_i - \dot{\mathbf{r}}_j)$ | velocidad relativa entre partículas |
| $\xi_{ij} = R_i + R_j - \lvert \mathbf{r}_j - \mathbf{r}_i \rvert$ | superposición |

[Figura: dos discos de radios $R_i$ y $R_j$ con centros en $\mathbf{r}_i$ y $\mathbf{r}_j$ (vectores desde el origen), distancia entre centros $r_{ij}$, vector $\mathbf{R}$ y la superposición $\xi_{ij}$ marcada en la zona donde se solapan.]

---

## Diapo 9 — Notación y variables geométricas (partícula–pared)

| Símbolo | Significado |
|---|---|
| $\dot{\mathbf{r}} = (\dot{\mathbf{r}}_i)$ | velocidad relativa entre partícula y pared (la pared está quieta) |
| $\xi_{ij} = R_i - \lvert \mathbf{r}_{ip} \rvert$ | superposición con la pared ($\mathbf{r}_{ip}$: vector del centro de la partícula a la pared, perpendicular) |

[Figura: disco de radio $R_i$ superpuesto con una pared inclinada (verde); la superposición $\xi_{ij}$ marcada.]

---

## Diapo 10 — Ecuaciones de movimiento

Se integran numéricamente las ecuaciones de movimiento acopladas, $i = 1, \dots, N$:

Traslaciones:
$$m_i \ddot{\mathbf{r}}_i = \sum_{j = \text{vecinas en contacto}} \mathbf{F}_{ij} + \mathbf{F}_g$$

Rotaciones:
$$I_i \ddot{\theta}_i = \sum_{j = \text{vecinas en contacto}} \mathbf{M}_{ij}$$

donde $I$ es el momento de inercia y $\mathbf{M}$ es el torque, $\mathbf{M} = \mathbf{R} \times \mathbf{F}$.

> Nota TP5: los peatones son discos sin rotación y el piso es horizontal: no hay rotaciones ni gravedad en el plano de movimiento.

---

## Diapo 11 — Versores durante la colisión partícula–partícula

- $\hat{\mathbf{e}}^t$: versor tangencial al contacto.
- $\hat{\mathbf{e}}^n$: versor normal al contacto.

$$\hat{\mathbf{e}}^n = (e^n_x,\ e^n_y) \qquad \hat{\mathbf{e}}^t = (-e^n_y,\ e^n_x)$$

donde

$$e^n_x = \frac{x_j - x_i}{\lvert \mathbf{r}_j - \mathbf{r}_i \rvert} \qquad e^n_y = \frac{y_j - y_i}{\lvert \mathbf{r}_j - \mathbf{r}_i \rvert}$$

[Figura: partícula $i$ y partícula $j$ en contacto; $\hat{\mathbf{e}}^n$ apunta de $i$ hacia $j$ y $\hat{\mathbf{e}}^t$ es perpendicular.]

---

## Diapo 12 — Versores durante la colisión partícula–pared

**Versores constantes para cada pared.**

- $\hat{\mathbf{e}}^t$: versor tangencial a la pared.
- $\hat{\mathbf{e}}^n$: versor normal a la pared.

$$\hat{\mathbf{e}}^n = (e^n_x,\ e^n_y) \qquad \hat{\mathbf{e}}^t = (-e^n_y,\ e^n_x)$$

$\alpha$ es el ángulo entre el versor normal y el eje $x$.

[Figura: partícula $i$ en contacto con una pared $j$ inclinada; $\hat{\mathbf{e}}^n$ perpendicular a la pared, $\hat{\mathbf{e}}^t$ a lo largo de la pared.]

---

## Diapo 14 — Fuerzas de contacto (esquemas)

[Figura: esquemas mecánicos de las fuerzas de contacto.]
- Ejemplo en la dirección normal ($\hat{\mathbf{e}}^n$): resorte de constante $k_n$ en paralelo con amortiguador $\gamma$ entre dos partículas.
- Ejemplos en la dirección tangencial ($\hat{\mathbf{e}}^t$):
  - Solo rozamiento de Coulomb ($\mu$).
  - Resorte tangencial $k_t$ en paralelo con amortiguador $\gamma$, en serie con rozamiento $\mu$.

---

## Diapo 15 — Opciones de fuerzas de contacto

### Dirección normal ($\hat{\mathbf{e}}^n$)

- **N.1)** $\mathbf{F}_N = \left[-k_n \xi - \gamma \dot{\xi}\right] \hat{\mathbf{n}}$
- **N.2)** $\mathbf{F}_N = \underbrace{-k_n \xi}_{F_N}\ \hat{\mathbf{n}}$

### Dirección tangencial ($\hat{\mathbf{e}}^t$)

- **T.1)**
$$\mathbf{F}_T = \min \begin{cases} -\mu\,\lvert \mathbf{F}_N \rvert\ \mathrm{sgn}(\dot{\mathbf{r}}_{rel} \cdot \hat{\mathbf{t}})\ \hat{\mathbf{t}} \\ -k_T\, \xi_T\, \hat{\mathbf{t}} \end{cases} \qquad \text{donde } \xi_T = \sum_t \Delta t\ \dot{\mathbf{r}}^{c}_{rel}(t)$$
- **T.2)** $\mathbf{F}_T = -\mu\,\lvert \mathbf{F}_N \rvert\ \mathrm{sgn}(\dot{\mathbf{r}}_{rel} \cdot \hat{\mathbf{t}})\ \hat{\mathbf{t}}$
- **T.3)** $\mathbf{F}_T = \underbrace{-k_T\, \xi\, [\dot{\mathbf{r}}_{rel} \cdot \hat{\mathbf{t}}]}_{F_T}\ \hat{\mathbf{t}}$

> Nota TP5: la fuerza granular del SFM (Helbing 2000) corresponde a N.2 + T.3, con $k_n = 1.2\cdot10^5$ N/m y $k_t = 2.4\cdot10^5$ kg/(m·s). Ver Clase 6, diapo 29.

---

## Diapo 16 — Suma de fuerzas (esquema)

[Figura: partícula $i$ (azul) en contacto con partículas $j$ y $k$ (rojas). Cada una ejerce una fuerza sobre $i$ a lo largo de la línea que une los centros: $\mathbf{F}_{ij}$ y $\mathbf{F}_{ik}$. La resultante es $\mathbf{F}_i = \mathbf{F}_{ik} + \mathbf{F}_{ij}$.]

---

## Diapo 17 — Proyección a componentes cartesianas

Antes de sumar fuerzas, para cada partícula conviene proyectar las fuerzas normal y tangencial (generadas por cada una de las demás partículas en contacto) en las componentes cartesianas $(x, y)$:

$$F_x = F_N\, e^n_x + F_T\, (-e^n_y)$$
$$F_y = F_N\, e^n_y + F_T\, (e^n_x)$$

Dado que $\hat{\mathbf{e}}^n = (e^n_x, e^n_y)$ y $\hat{\mathbf{e}}^t = (-e^n_y, e^n_x)$.

---

## Diapo 18 — Fuerza total sobre cada partícula

Finalmente la fuerza sobre cada partícula $i$ debido al contacto con las demás partículas, paredes u obstáculos ($j$) resulta:

$$\mathbf{F}^{Tot}_i = m_i \mathbf{g} + \sum_j \mathbf{F}_{N\,ij} + \sum_j \mathbf{F}_{T\,ij}$$

$$F^{Tot}_{i\,x} = \sum_j F_{N\,ij}\ e^n_{x\,ij} + \sum_j F_{T\,ij}\ (-e^n_{y\,ij})$$

$$F^{Tot}_{i\,y} = m_i g + \sum_j F_{N\,ij}\ e^n_{y\,ij} + \sum_j F_{T\,ij}\ e^n_{x\,ij}$$

---

## Diapo 19 — Criterio para elegir el paso temporal

Dependerá del método de integración, pero como referencia puede tomarse:

$$\Delta t \approx 0.1 \sqrt{\frac{m}{k_n}}$$

> Nota TP5: con $m = 80$ kg y $k_n = 1.2\cdot10^5$ N/m da $\Delta t \approx 2.6\cdot10^{-3}$ s.

---

## Diapo 20 — Δt de simulación vs Δt₂ de guardado

Diferenciar entre:
- Paso $\Delta t$ de la simulación.
- Paso $\Delta t_2$ para guardar el estado del sistema: $\Delta t_2 = k\,\Delta t$, con $k \in \mathbb{N}$.

Ejemplo ($t = \sum \Delta t$, el tiempo total):

```
if (t / Δt2) == round(t / Δt2)
    guardo estado sistema.
end
```

---

## Diapo 21 — Guardar con la máxima precisión

Diferenciar entre paso $\Delta t$ de la simulación y paso $\Delta t_2$ para guardar el estado del sistema.

Con la máxima precisión ($\Delta t$): guardar una lista con los tiempos de salida de cada partícula:

```
t1
t2
t3
t4
.....
```
