# Simulación de Sistemas — Trabajo Práctico Nro. 5: Dinámica Peatonal

Fuente: `TP5_Enunciado.pdf` (enunciado publicado en CAMPUS el 30/10/2026).
Este archivo contiene la parte general del enunciado y el **Problema 2** completo. El Problema 1 (calibración de una capa de elusión) fue omitido a propósito. El texto es literal; solo se le dio formato Markdown.

---

## Parte general

Elegir entre los dos problemas detallados más abajo para implementar, estudiar y presentar.

Ambos problemas utilizan alguno de los siguientes modelos de dinámica peatonal vistos en las clases teóricas: SFM [1] sin fuerza social o CPM [2].

Las simulaciones tendrán un dt fijo e intrínseco de la simulación (en el caso de dinámica molecular regida por el paso temporal). En caso de ser necesario (según sea el modelo elegido, número de partículas, tamaño de archivos de output, etc.) considerar un dt2 para imprimir el estado del sistema (posiciones, velocidades, radio en caso de CPM, de las partículas) para luego realizar análisis y animaciones con una velocidad adecuada. Notar que en el caso de CPM el dt forma parte de las reglas del modelo y por lo tanto no se lo debe cambiar arbitrariamente.

Se recuerda que la simulación debe generar un output en formato de archivo de texto. Luego el módulo de animación se ejecuta en forma independiente tomando estos archivos de texto como input. De esta forma la velocidad del análisis y de la animación no quedan supeditados a la velocidad de la simulación.

Para cada uno de los estudios que se realicen, se debe mostrar una animación característica, la evolución temporal del observable primario, y explicitar cómo se calcula el observable escalar que se usará luego al mostrar input vs. observable escalar.

### Entregables

Los entregables del T.P. son:

- **a-** Presentación oral tipo Powerpoint o similar de 13 minutos de duración con las secciones indicadas en el documento ".../Formato_Presentaciones.pdf".
- **b-** El documento de la presentación en formato *.pdf (sin animaciones embebidas, solo links explícitos a youtube o vimeo, por favor no a google drive).
- **c-** El código fuente implementado en un archivo *.zip. Solo versión final del motor de simulación (tamaño del archivo del orden de unos pocos kB). No adjuntar en este archivo código de postprocesamiento, ni historial de versiones, ni output de simulaciones, ni figuras, ni resultados, ni otros documentos.

### Fecha y forma de entrega

La presentación en pdf (b) y el código fuente (c) deberán ser presentados a través de campus, antes del día **16/11/2026 a las 13 hs**. Los archivos deben nombrarse de la siguiente manera:

- `SdS_TP5_2026Q2GXXCSS_Presentación`
- `SdS_TP5_2026Q2GXXCSS_Codigo`

donde XX es el número de grupo y SS es la comisión ("S" o "S2"). Las presentaciones orales (a) se realizarán durante la clase del mismo día.

---

## Problema 2: Evacuación con esquema híbrido Event Driven + Time Step Driven

Elegir entre SFM sin fuerza social [1] o CPM [2].

Simular la evacuación de **N = 500** agentes desde una habitación cuadrada de lado **L = 50 m** con una única puerta de ancho **d = 0.8 m** ubicada en el centro de una de sus paredes, como se muestra en la Fig. 1. Inicialmente los agentes se generan al azar, sin superponerse entre ellos ni con las paredes, en toda la habitación (densidad baja). El target de todos los agentes es la puerta y el módulo de la velocidad deseada es **vd = 1.5 m/s**. Las paredes son rígidas y deben interactuar con las partículas si alguna la choca. Los agentes que atraviesan la puerta se retiran de la simulación una vez que se alejan de ella una distancia de **1 m** (esto requiere un segundo target una vez alcanzada la puerta). Para SFM usar como radio de las partículas una distribución uniforme **r ∊ [0.18 m, 0.25 m]**; para CPM usar **rmin = 0.10 m** y **rmax = 0.25 m**. Tomar los parámetros no especificados aquí de los papers correspondientes.

**Figura 1:** Esquema del sistema a simular en un momento arbitrario. Las partículas azules están en la zona diluida (candidatas a Event Driven) y las rojas en el bottleneck cerca de la puerta (candidatas a Time Step Driven). Las flechas indican las velocidades.

[Descripción de la figura: sala cuadrada con la puerta en el centro de una pared; partículas azules dispersas en la mayor parte de la sala y partículas rojas amontonadas en semicírculo frente a la puerta, cada una con una flecha de velocidad.]

Mientras los agentes están dispersos, se mueven en línea recta hacia la puerta con velocidad constante y prácticamente no interactúan, por lo que su dinámica puede resolverse con dinámica molecular regida por eventos (EDMD), avanzando el sistema directamente hasta el próximo evento (contacto entre agentes, contacto con paredes, llegada a la puerta, etc.). A medida que se forma el bottleneck cerca de la puerta, las interacciones se vuelven continuas y esas partículas requieren dinámica molecular regida por el paso temporal (TSDMD).

Proponer un esquema híbrido que permita separar, tanto por etapas de la simulación como por cada partícula, la utilización de uno u otro esquema. Describir matemáticamente:

- Los criterios de pasaje de una partícula de EDMD a TSDMD y viceversa (por ejemplo, en base a densidad local, número de vecinos, tiempo al próximo contacto, distancia a la puerta, etc.).
- Cómo se resuelve la convivencia de ambos esquemas: sincronización entre los eventos y los pasos dt, cómo se calculan los eventos de una partícula EDMD con una partícula TSDMD, y cómo se garantiza que no se pierdan contactos ni haya superposiciones en el pasaje de un esquema a otro.
- En los casos EDMD puro e híbrido, explicar cómo será considerada la colisión entre agentes y entre agentes y paredes, y el mecanismo para recuperar la velocidad deseada después de dicha colisión.

### Puntos a resolver

**a)** Implementar los tres esquemas: TSDMD puro, EDMD puro e híbrido. Mostrar animaciones características de cada uno; en el caso híbrido colorear las partículas según el esquema con el que se resuelve en cada instante. Graficar la evolución temporal de la fracción de partículas en cada esquema. Durante la presentación dedicarle a la descripción matemática-computacional del esquema híbrido más tiempo del que usualmente se dedica a la introducción matemática del TP.

**b)** Para los tres esquemas, medir las curvas de población (número de agentes dentro de la habitación en función del tiempo) y el tiempo de evacuación (tiempo en que sale el último agente). Realizar al menos 10 realizaciones y reportar los tiempos de evacuación promedio con sus barras de error. Tomando TSDMD puro como referencia, discutir las diferencias de los otros dos esquemas.

**c)** Medir el tiempo de ejecución (wall-clock) de la simulación completa para los tres esquemas, en la misma computadora y con su barra de error sobre las realizaciones. Para el esquema híbrido, variar el parámetro principal del criterio de pasaje entre esquemas y graficar el tiempo de ejecución y el tiempo de evacuación en función de dicho parámetro, indicando los valores de los casos TSDMD puro y EDMD puro como referencia.

**d)** Estudiar cómo escalan los tiempos de ejecución de los tres esquemas con el número de agentes, variando N (por ejemplo N ∊ [100, 1000] y en dos variantes a) aumentando L de forma tal que la densidad inicial se mantenga constante; y b) a L constante, aumentando la densidad.

---

## Referencias

[1] Helbing, D., Farkas, I., & Vicsek, T. (2000). Simulating dynamical features of escape panic. Nature, 407(6803), 487-490.

[2] Baglietto, G., & Parisi, D. R. (2011). Continuous-space automaton model for pedestrian dynamics. Physical Review E, 83(5), 056117.
