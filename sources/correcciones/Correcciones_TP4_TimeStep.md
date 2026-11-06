# Devolución de la cátedra — TP4 (Grupo 9)

**Tema del TP4: dinámica molecular regida por el paso temporal (Time Step Driven, TSDMD)**: integradores (oscilador) y billar con fuerzas, obstáculos, paredes y Cell Index Method.

Nota: **5**. "Canzonieri + 1 x experimento = 6."

Texto de la devolución recibida, con formato Markdown y erratas menores corregidas. "D*N*" se refiere a la diapositiva *N* de nuestra presentación del TP4. Al final hay una sección agregada por nosotros con lo que hay que aplicar en el TP5; no es parte de la devolución.

> Contexto para el TP5: probablemente se reutilice el motor TSDMD de este TP (integrador, fuerzas, Cell Index Method) para el esquema TSDMD y el híbrido, aplicando estas correcciones.

---

## Código

- **Integradores del oscilador:** Beeman (versión con 3/2 y −1/2), Euler predictor-corrector y Verlet original (f(t) con la última velocidad disponible), como en la teórica. Velocity Verlet evalúa f(t+Δt) con v(t+Δt/2), una opción válida porque la teórica no especifica qué v usar. Por eso Verlet y Velocity Verlet quedan en orden 1 (D2).
- **IMPORTANTE — observables fuera del motor:** Board y Oscillator implementan `getKineticEnergy()` y `getPotentialEnergy()` (declarados en la interfaz `Integrator.Model`), que nunca se llaman. El cálculo de observables no debe estar en el motor, ni siquiera como código sin usar: sacarlo, también del UML (D7, "Model: Energía"). Ver teóricas 0 y 1.
- **IMPORTANTE — estado en el archivo dinámico:** el estado de las partículas (fresca/usada) no se escribe en `dynamic.txt` (`OutputWriter.frame` escribe solo x, y, vx, vy), como pide el enunciado en cada Δt₂ y como indica su propio pseudocódigo (D8, línea 11). Se guarda aparte el instante de conversión de cada partícula (`conversions.txt`), del que se puede reconstruir el estado. La información está completa, pero el formato no es el pedido.
- **IMPORTANTE — integrador del billar:** el integrador del billar se elige por línea de comandos (`--method`) y el pseudocódigo de D8 corresponde a Velocity Verlet. El enunciado pide el esquema de Verlet: aclarar con cuál se corrió.
- **Billar:** fuerzas (pares, obstáculos, pared con partícula imagen) y Cell Index Method OK.
- **Zip:** OK (16 KB, solo motor).

---

## Por diapositiva

- **D2)** Falta la definición del ECM en la diapositiva. Al ser la única diapo del Sistema 1, conviene incluirla.
- **D7)** El UML incluye "Energía" en Model. La energía es un observable y no pertenece al motor (ver Código).
- **D8)** El pseudocódigo corresponde a Velocity Verlet, y el enunciado pide Verlet para el billar. Aclarar qué esquema se usó y por qué. Además, la línea 11 dice "guardar … estado de cada partícula", pero el archivo dinámico no incluye el estado (ver Código).
- **D11)** La tabla de estudios no va. Los rangos ya están en D10 y el resto no es esencial. El mapa de calor con M = 3 realizaciones tiene poca estadística.
- **D12)** Falta definir $F_u(t) = N_u(t)/N$ (se usa en D13 sin definirla). El ECM de la energía es absoluto (J²), así que depende de N. Uno relativo, $|E(t) - E(0)|/E(0)$, es más interpretable.
- **D12/D14/D24/D25)** La letra E se usa para dos cosas distintas: la energía total (D12, D16) y el error del ajuste de Maxwell-Boltzmann (D14, D24, y E*(t) en D25). Usar otro símbolo para el error del ajuste.
- **D17)** Falta el criterio con el que se eligió Δt = 10⁻⁴ s. Marcar el umbral en la figura.
- **D19/D20/D21)** La misma posición aparece como "x₀ = R/2" (D19), "2,55×10⁻¹ m" (D20) y en los ejes en ×10⁻¹ m (D21). Elegir una notación y mantenerla.
- **D23)** El panel de t = 0 tiene una escala vertical distinta (×10¹) a la de los otros tres (0–5). Así no se pueden comparar los instantes entre sí. Usar la misma escala en todos los paneles.
- **D24/D26)** Bien el ajuste con el método de la Teórica 0 y la comparación numérica. Falta indicar cómo se obtiene la incerteza ±0,02×10⁻² J.
- **D25)** Bien el criterio explícito para determinar el estacionario.
- **D30)** **IMPORTANTE:** ⟨t₁₀₀⟩ ≈ 28–30 s con t_f = 30 s. Los valores están acotados por el tiempo de simulación, y no se explica qué se hizo con las realizaciones que no alcanzan F_u = 1 (o 0,9 en N = 200, marcador vacío sin leyenda).

---

## Para el TP5 (agregado por nosotros, no es parte de la devolución)

**Código del motor**
- El motor no calcula observables: nada de N(t), tiempo de evacuación, fracción por esquema, caudal ni energía dentro del simulador, ni como métodos sin usar ni en el UML. El motor escribe archivos; el análisis los lee.
  - Excepción razonable a explicitar: el tiempo de ejecución (wall-clock) del ítem c solo se puede medir alrededor del loop de simulación.
- El **archivo dinámico** debe tener en cada Δt₂ todo el estado necesario por partícula: x, y, vx, vy y además el esquema (EDMD/TSDMD) en el híbrido, el radio en CPM y si ya salió o no. No reconstruirlo desde archivos aparte. El tiempo de salida de cada agente se puede guardar además con precisión dt (Clase 5, diapo 21).
- Un solo integrador para TSDMD, el que se presenta. Si el código permite elegir, decir en la presentación cuál se usó y por qué, y que el pseudocódigo coincida.
- Zip: solo el motor, pocos kB.

**Presentación**
- Definir matemáticamente cada observable antes de usarlo: N(t), t_evac, f_TS(t) = N_TS(t)/N(t), cómo se promedia y el número de realizaciones.
- No usar la misma letra para dos cosas (por ejemplo, E para error de ajuste y otra cosa; t para tiempo simulado y tiempo de ejecución).
- Justificar el valor de dt con un criterio y marcarlo en la figura correspondiente.
- Una sola notación para cada cantidad en todas las diapos.
- Paneles comparables con la misma escala.
- Si se informa una incerteza, decir cómo se obtiene.
- Sin tablas de estudios; los rangos de parámetros van en la sección Simulaciones. Con pocas realizaciones (M = 3) hay poca estadística: usar al menos las 10 que pide el enunciado.
- **El tiempo de evacuación no puede quedar acotado por el tiempo máximo de simulación**: simular hasta que salga el último agente, o explicitar qué se hace con las realizaciones que no terminan.
