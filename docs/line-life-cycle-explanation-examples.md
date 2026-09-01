Aquí tienes la documentación en formato Markdown limpio, sin enlaces locales ni dependencias de rutas, lista para commitear directamente en el repositorio:

```markdown
# Pipeline de Validación y Ejecución en PrintScript

Este documento describe el flujo conceptual y técnico por el cual atraviesa una sentencia de código en **PrintScript**, desde su lectura inicial como texto plano hasta su eventual ejecución, análisis estático o reformateo.

La arquitectura se organiza en una serie de módulos desacoplados y secuenciales donde cada etapa asume una única responsabilidad bien definida.

## 2. Etapas del Pipeline

### 2.1. Análisis Léxico (Lexer)
* **Propósito:** Transformar la secuencia continua de caracteres del código fuente en una lista estructurada de unidades atómicas denominadas **Tokens** (palabras clave, identificadores, literales, operadores y símbolos de puntuación).
* **Consideraciones de Diseño:**
    * **Lectura por flujo (Streaming):** El lexer procesa la entrada carácter por carácter sin necesidad de cargar la totalidad del archivo en memoria, permitiendo un mecanismo de *lookahead* (inspeccionar el siguiente carácter sin consumirlo) para resolver ambigüedades (ej. distinguir números enteros de decimales).
    * **Trazabilidad de Posición:** Cada token registrado almacena su coordenada exacta (línea y columna de inicio y fin), indispensable para reportar diagnósticos precisos en etapas posteriores.
    * **Manejo funcional de fallos:** Los caracteres inesperados o literales mal delimitados se encapsulan como errores léxicos específicos en lugar de interrumpir abruptamente el sistema.

---

### 2.2. Análisis Sintáctico (Parser)
* **Propósito:** Validar que la secuencia de tokens cumpla con la gramática formal del lenguaje y estructurarla en un **Árbol de Sintaxis Abstracta (AST)**.
* **Consideraciones de Diseño:**
    * **Motor Declarativo por Reglas:** En lugar de codificar métodos rígidos por cada producción gramatical, la gramática se compone mediante combinadores de reglas (secuencias, alternativas, elementos opcionales y repeticiones).
    * **Construcción desacoplada del AST:** Cada regla gramatical exitosa mapea sus tokens a nodos del AST fuertemente tipados (`Program`, `VariableDeclaration`, `PrintStatement`, `BinaryExpression`, etc.).
    * **Precedencia de Operadores:** Las expresiones se estructuran jerárquicamente para garantizar que operaciones de mayor precedencia (multiplicación y división) se evalúen antes que las de menor precedencia (suma y resta).

---

### 2.3. Análisis Semántico (Semantic Analyzer)
* **Propósito:** Verificar la coherencia contextual, el alcance de las variables y el sistema de tipos estático sobre el AST generado.
* **Consideraciones de Diseño:**
    * **Tabla de Símbolos y Ámbitos (Environment):** Se registra y rastrea qué variables han sido declaradas, su tipo y su estado a lo largo del programa.
    * **Chequeo Estático de Tipos:** Garantiza que los valores asignados coincidan con el tipo declarado de la variable y valida las operaciones entre tipos compatibles (por ejemplo, permitir concatenación cuando interviene una cadena de texto o restringir la resta a tipos numéricos).
    * **Reglas Modulares e Independientes:** Las validaciones de declaraciones, variables y expresiones se dividen en reglas aisladas que facilitan la extensibilidad del lenguaje.

---

### 2.4. Linter (Análisis Estático de Código)
* **Propósito:** Evaluar el AST para detectar infracciones a guías de estilo, convenciones de nombres o patrones de código desaconsejados, sin alterar la ejecución del programa.
* **Consideraciones de Diseño:**
    * **Reglas Configurables:** Las reglas pueden activarse, desactivarse o parametrizarse (ej. exigir nomenclatura `camelCase` o `snake_case`, o prohibir expresiones complejas dentro de sentencias de impresión).
    * **Diagnósticos No Bloqueantes:** Genera notificaciones con niveles de severidad (`WARNING` o `ERROR`), permitiendo reportar sugerencias de calidad de código en entornos de integración continua (CI/CD) o editores.

---

### 2.5. Formateador (Formatter)
* **Propósito:** Reconstruir el código fuente a partir del AST siguiendo un conjunto de reglas estéticas y de espaciado estandarizadas.
* **Consideraciones de Diseño:**
    * **Reglas de Estilo Parametrizables:** Permite configurar de forma personalizada el espaciado alrededor de operadores, símbolos de asignación, dos puntos y saltos de línea entre bloques de sentencias.
    * **Idempotencia:** Formatear un código ya formateado produce exactamente el mismo resultado.

---

### 2.6. Intérprete (Interpreter / Runtime)
* **Propósito:** Recorrer y evaluar el AST validado para ejecutar las instrucciones y producir los efectos esperados del programa.
* **Consideraciones de Diseño:**
    * **Aislamiento de Entrada/Salida (I/O):** La salida estándar se abstrae tras una interfaz que permite redirigir el texto generado a la consola, a un buffer en memoria para pruebas automáticas o a un servicio externo.
    * **Evaluación de Expresiones y Tipado en Runtime:** Resuelve las operaciones matemáticas y la concatenación polimórfica de cadenas manteniendo el estado actualizado en el entorno de ejecución.

---

## 3. Ejemplos Prácticos y Casos de Uso

### Caso 1: Flujo Exitoso Completo
```printscript
let totalScore: number = 10 + 5;
println(totalScore);
```

1. **Lexer:** Emite los tokens: `LET`, `IDENTIFIER("totalScore")`, `COLON`, `IDENTIFIER("number")`, `EQUAL`, `NUMBER_LITERAL("10")`, `PLUS`, `NUMBER_LITERAL("5")`, `SEMICOLON`, `IDENTIFIER("println")`, `LEFT_PAREN`, `IDENTIFIER("totalScore")`, `RIGHT_PAREN`, `SEMICOLON`.
2. **Parser:** Construye el AST compuesto por un nodo de declaración de variable (`VariableDeclaration`) cuyo valor es una expresión binaria (`BinaryExpression`), seguido de un nodo de impresión (`PrintStatement`).
3. **Semantic Analyzer:** Verifica que `totalScore` no haya sido declarada previamente, valida que la suma de dos números dé como resultado `number`, y comprueba que `println` haga referencia a una variable existente.
4. **Linter:** Confirma que el identificador cumple con la convención de estilo configurada y que el argumento de `println` es una variable simple.
5. **Formatter:** Genera el texto estandarizado respetando las reglas de espaciado.
6. **Interpreter:** Evalúa la suma (`15.0`), asigna el valor en el entorno y envía `"15.0"` a la salida estándar.

---

### Caso 2: Detección Temprana de Error Léxico
```printscript
let message: string = "Texto sin cerrar;
```
* **Etapa de corte:** **Lexer**
* **Comportamiento:** Al encontrar el salto de línea o el final del archivo sin la comilla de cierre correspondiente, el lexer interrumpe el proceso emitiendo un error léxico indicando la línea y columna exactas donde inició la cadena.
* **Consideración:** El proceso se detiene inmediatamente sin intentar invocar al parser ni construir árboles sintácticos inconsistentes.

---

### Caso 3: Detección de Error Sintáctico
```printscript
let score = 20;
```
* **Etapa de corte:** **Parser**
* **Comportamiento:** La gramática del lenguaje exige la presencia explícita del tipo (`let <identificador> : <tipo> = <expresion>;`). Al recibir el token `=` en lugar de `:`, ninguna regla de declaración coincide y el parser emite un error de sintaxis reportando el token inesperado.
* **Consideración:** Garantiza que solo programas sintácticamente válidos avancen a la etapa de análisis semántico.

---

### Caso 4: Detección de Error Semántico (Incompatibilidad de Tipos)
```printscript
let count: number = "veinte";
```
* **Etapa de corte:** **Semantic Analyzer**
* **Comportamiento:**
    * El **Lexer** y el **Parser** procesan la línea con éxito ya que la estructura gramatical es correcta.
    * El **Semantic Analyzer** evalúa el tipo declarado (`number`) y el tipo deducido de la expresión (`string`). Al detectar la discrepancia, genera un error semántico de tipos incompatibles.
* **Consideración:** Previene comportamientos impredecibles o fallos en tiempo de ejecución garantizando seguridad de tipos previa.

---

### Caso 5: Aplicación de Reglas de Linter (Advertencias de Calidad)
```printscript
let user_name: string = "Alice";
println(10 + 20);
```
* **Etapa:** **Linter**
* **Comportamiento:**
    * El código supera satisfactoriamente las etapas léxica, sintáctica y semántica, siendo completamente apto para ejecutarse.
    * El **Linter** emite advertencias (*warnings*):
        1. Si la regla de nomenclatura está configurada en `camelCase`, señala que `user_name` no cumple con el formato esperado.
        2. Si la regla de impresión restringe expresiones complejas dentro de `println`, notifica que debe utilizarse una variable intermedia en lugar de una operación aritmética directa.
* **Consideración:** Permite fiscalizar buenas prácticas sin impedir la compilación o ejecución cuando no se requiere un corte estricto.

---

## 4. Matriz de Responsabilidades

| Etapa | Entrada | Salida | Manejo de Diagnósticos | Consideración Principal |
| :--- | :--- | :--- | :--- | :--- |
| **Lexer** | Flujo de caracteres | Lista de Tokens | Errores Léxicos | Lectura en streaming y cálculo de coordenadas de posición. |
| **Parser** | Lista de Tokens | AST (`Program`) | Errores Sintácticos | Motor declarativo y precedencia de operadores. |
| **Semantic** | AST (`Program`) | AST validado | Errores Semánticos | Verificación de alcance de variables y consistencia de tipos. |
| **Linter** | AST (`Program`) | Notificaciones | Advertencias / Errores | Reglas de estilo y buenas prácticas configurables. |
| **Formatter**| AST (`Program`) | Código formateado | Excepciones de formato | Estandarización idempotente de la presentación del código. |
| **Interpreter**| AST (`Program`) | Ejecución / Salida | Errores de Runtime | Evaluación en memoria y desacoplamiento de la salida mediante interfaces. |
```
