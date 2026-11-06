# Guía para realizar presentaciones con diapositivas

Fuente: `GuiaPresentaciones.pdf` (cátedra de Simulación de Sistemas, 3 páginas). Texto completo, solo con formato Markdown.

Punto clave (1.4): el enunciado del T.P. tiene **máxima prioridad** sobre esta guía y sobre las teóricas.

---

## 1. Consideraciones Generales

**1.1** Los informes y las presentaciones sobre un mismo trabajo son documentos independientes y cada uno es autocontenido. No se pueden dejar ítems sin especificar en uno de ellos porque estarán en el otro.

**1.2** Numerar las diapositivas. Lo que facilita la discusión posterior al hacer referencia a una diapositiva identificada por su número.

**1.3** Respetar los tiempos designados. Para ello, ensayar las presentaciones con anterioridad. En cada T.P. se indicará el tiempo correspondiente. Para el T.P. final el tiempo de la presentación oral debe ser de 10 a 15 minutos según se indique en los enunciados de los T.P.

**1.4** Responder a todos los puntos pedidos en el enunciado del T.P. y leerlo completo. Este documento tiene máxima prioridad respecto de otros documentos como teóricas o guías genéricas.

**1.5** Respetar el formato solicitado para la entrega de los archivos y los nombres de los mismos.

**1.6** Las diapositivas no deben contener mucho texto (no redactar párrafos enteros o partes de bibliografía).

**1.7** Las figuras no llevan títulos dentro de las mismas ni leyendas explicativas debajo (captions, como en el caso de un informe o reporte redactado, que sí llevan captions). La información correspondiente a la configuración del sistema (parámetros fijos) debe estar descripta al costado de la figura. Es decir, cuáles fueron las condiciones particulares bajo las cuales se obtuvieron esos resultados.

**1.8** Las figuras sí llevan leyendas/títulos en los ejes vertical y horizontal, preferentemente en palabras (no símbolos) y, cuando corresponde, con las unidades entre paréntesis con las abreviaturas del sistema MKS, por ejemplo para "segundos" (s), para "metro" (m), etc. El tamaño de fuente de letras y números dentro de las figuras debe ser similar al tamaño del resto del texto en la diapositiva (por lo menos 20).

**1.9** Tanto en las figuras como en tablas de resultados o de parámetros utilizados se debe usar notación científica (potencias de 10, i.e.: ..., 10⁻¹, 10⁰, 10¹, 10², ...) y las cantidades que se indiquen deben expresarse con las unidades correspondientes. No se deberá usar notación del tipo 1E2, ni 10^2, ni otra que no sea la expresada arriba con la potencia como supraíndice.

**1.10** Al expresar tablas de resultados de observables promedio se deben utilizar las cifras significativas correspondientes al error asociado según se explicó en la Teórica 0. No expresar una cantidad de dígitos que dificulten la lectura.

**1.11** La presentación no lleva una sección de bibliografía o referencias (como sucede en el caso de informe o reporte escrito). De ser necesario, se incluye la cita abreviada en la diapositiva correspondiente (autor, revista y año de publicación).

**1.12** Estructurar de forma coherente títulos y subtítulos en las diapositivas. Sugerencia: se puede usar la clase de LaTeX Beamer (`\documentclass{beamer}`), con la opción `\useoutertheme{miniframes}` o el tema `\usetheme{Warsaw}`.

**1.13** Separar secciones con diapositivas que solo tengan el título de la sección que comienza. No numerar las secciones en la presentación.

---

## 2. Secciones

En concordancia con los conceptos explicados en las clases teóricas respecto a las relaciones entre Sistema Real, Modelo Matemático, Modelo Computacional y Simulación, las presentaciones deben tener las siguientes secciones:

**2.1 Introducción / Sistema Real / Fundamentos** (máximo 3 diapositivas): somera descripción del sistema real que se intenta simular y rápida pero completa descripción de las ecuaciones del modelo matemático general (sin especificar ningún sistema particular, ni métodos sobre cómo se resuelve el modelo).

**2.2 Implementación**: detalle de cómo se traduce el modelo matemático al modelo computacional, describiendo arquitectura del código implementado, pseudocódigo, diagrama UML, etc. Solo considerar el motor de simulación propiamente dicho, dejando fuera de esta descripción cómo se implementa el post-proceso, o el formato de los archivos input/output, etc.

**2.3 Simulaciones**: descripción del sistema particular que se va a simular y estudiar, como geometría, rango de parámetros fijos y variables, inputs y outputs a estudiar. Ilustrar con un esquema el sistema particular que se va a simular.
Además, se deben definir los observables que se calcularán a partir del output directo de la simulación (que presenta el estado del sistema para ciertos instantes de tiempo). Estas definiciones deben ser expresadas matemáticamente. Por ejemplo, si se va a promediar una cierta cantidad hay que explicitar cómo se promedia, qué se va a estar sumando y sobre qué se estará dividiendo.
Detallar el número de repeticiones y tiempos de las simulaciones realizadas.

**2.4 Resultados**: estructurar la sección de resultados de la siguiente manera.

- **2.4.1** Para cada input o parámetro a estudiar, primero mostrar una animación característica del sistema (pueden ser dos, con dos valores extremos del input para ver ejemplos de distintos comportamientos). La idea de esto es ilustrar la dinámica del sistema para situar el contexto de los resultados a mostrar.
- **2.4.2** Luego mostrar una figura del observable (que se calcula a partir de los outputs directos de la simulación) en función del tiempo. Explicar entonces cuál será el escalar que caracteriza ese proceso (por ejemplo el promedio de la evolución temporal en el estado estacionario, la tasa de crecimiento, etc.). Solo mostrar evoluciones típicas, de valores extremos del rango de parámetros, para validar las definiciones de los observables. Las evoluciones en sí no son los resultados definitivos, por lo tanto no debe ser extenso lo que se muestre de las mismas.
- **2.4.3** Después presentar la figura del input vs. observable, con promedio y barras de error.
- **2.4.4** Repetir los pasos desde 2.4.1 hasta 2.4.3 para los otros parámetros o inputs que se estudiaron.
- **2.4.5** Cuando se ajuste una función teórica a los resultados de las simulaciones se debe mostrar cómo se halló el mejor ajuste en los términos de lo explicado en la Teórica 0.
- **2.4.6** Las figuras deben tener los datos promedios obtenidos claramente identificados con un símbolo (o la barra de error). No se pueden unir los puntos con líneas sin destacar cuáles son los puntos. Estos datos pueden ser opcionalmente unidos por líneas rectas como "guía para el ojo". En ningún caso se puede interpolar los datos con funciones arbitrarias (polinomios, splines) que no provengan de ninguna teoría sobre el sistema que se estudia.
- **2.4.7** Cuando los datos varían en distintos órdenes de magnitud (incluso con potencias negativas, por ejemplo para una función que tiene una asíntota en cero) una escala lineal no permite ver las diferencias; en esos casos se debe graficar con escala doble logarítmica, o semilogarítmica en el eje que corresponda.
- **2.4.8** Durante la presentación en vivo (presencial o virtual) las animaciones deben estar embebidas en la diapositiva correspondiente (no salir de la presentación para mostrarlas con otro programa). Pero en el archivo pdf de la presentación que se debe entregar no debe haber animaciones embebidas, ni tampoco se deben entregar archivos de animaciones. La presentación en formato pdf debe tener una imagen fija de un fotograma representativo de la animación y debajo de este debe estar explícitamente escrito un link a youtube o similar.

**2.5 Conclusiones** (1 diapositiva): basadas solo en los resultados mostrados. No son conclusiones resultados observados pero no mostrados en resultados, ni hipótesis de posibles explicaciones de lo observado que no hayan sido estudiadas explícitamente. Cualquier hipótesis que potencialmente explique lo observado debe ser probada explícitamente y mostrada en resultados; caso contrario no es válida como conclusión. Tampoco es una conclusión algo que haya quedado por hacer o algo que se haya hecho mal y se descartó.

**2.6** Puede haber una diapositiva de cierre con "Muchas Gracias" o "Gracias por su Atención", pero no se estila en la misma escribir la palabra "preguntas?".

---

## 3. Otras consideraciones

**3.1** La presentación debe estar previamente distribuida en las partes que expondrán las/os distintas/os presentadores. Todos los miembros del grupo deben estar en condiciones de realizar la presentación completa, es decir, cualquiera podría exponer cualquier parte.

**3.2** Estas partes deben estar balanceadas en cuanto a tiempo, contenido y participación de los distintos integrantes del grupo. Los miembros del grupo no deben superponerse explicando o agregando a lo que recién explicó el/la compañero/a. De igual forma, si hay preguntas al finalizar, las deben responder en orden.

**3.3** Antes de entregar un T.P., todos los miembros del grupo deben corregir el documento, tanto para verificar que se cumpla con la presente guía como para no repetir errores corregidos en T.P.s anteriores. Los errores que se reiteren de un T.P. al siguiente darán lugar a una penalización en la nota del último T.P. entregado.

**3.4** Si algún punto de esta guía no queda claro, consultar con los docentes para su explicación.

**3.5** Vectores en negrita sin itálica (**x**). Escalares en itálica, sin negrita (*t*). Unidades sin negrita y sin itálica (8 m).
- metros: m
- segundos: s
- kilogramo: kg
- etc.

---

## Checklist rápido para figuras (resumen de 1.7–1.9, 2.4.6–2.4.7)

> Resumen agregado para el TP5; no es parte del documento original.

- Sin título dentro de la figura y sin caption debajo; los parámetros fijos de la corrida van al costado de la figura.
- Ejes nombrados en palabras, con unidades MKS entre paréntesis: "tiempo (s)", "número de agentes", "tiempo de evacuación (s)".
- Fuente dentro de la figura de tamaño similar al texto de la diapo (≥ 20).
- Notación científica con supraíndice (10³), nunca 1E3 ni 10^3.
- Promedios marcados con símbolo y barra de error; líneas rectas entre puntos solo como guía para el ojo; nunca splines ni polinomios.
- Si los datos abarcan varios órdenes de magnitud, escala log-log o semilog (por ejemplo, tiempo de ejecución vs N en el ítem d).
- Cifras significativas acordes al error.
