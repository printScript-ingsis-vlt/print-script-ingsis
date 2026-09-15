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
    * **Configuración por versión:** El consumidor provee una `LexerConfiguration` correspondiente a la versión de PrintScript que desea procesar. La configuración define qué palabras se reconocen como keywords y qué símbolos están habilitados. Por ejemplo, `const` se tokeniza como `IDENTIFIER` en 1.0 y como `CONST` en 1.1.

---

### 2.2. Análisis Sintáctico (Parser)
* **Propósito:** Validar que la secuencia de tokens cumpla con la gramática formal del lenguaje y estructurarla en un **Árbol de Sintaxis Abstracta (AST)**.
* **Consideraciones de Diseño:**
    * **Motor Declarativo por Reglas:** En lugar de codificar métodos rígidos por cada producción gramatical, la gramática se compone mediante combinadores de reglas (secuencias, alternativas, elementos opcionales, repeticiones y referencias perezosas para reglas recursivas).
    * **Construcción desacoplada del AST:** Cada regla gramatical exitosa mapea sus tokens a nodos del AST fuertemente tipados (`Program`, `VariableDeclaration`, `PrintStatement`, `BinaryExpression`, etc.).
    * **Precedencia de Operadores:** Las expresiones se estructuran jerárquicamente (`primary → term → expression`) para garantizar que operaciones de mayor precedencia (multiplicación y división) se evalúen antes que las de menor precedencia (suma y resta), soportando además paréntesis para alterar ese orden.
    * **Configuración por lista de reglas:** El parser recibe una `GrammarConfiguration` con la lista de sentencias que acepta (declaraciones, asignaciones, `println`, ...), en vez de tener una gramática fija. Sumar una construcción nueva del lenguaje es agregar una regla a esa lista, sin modificar las reglas existentes ni el motor de combinadores.

---

### 2.3. Análisis Semántico (Semantic Analyzer)
* **Propósito:** Verificar la coherencia contextual, el alcance de las variables y el sistema de tipos estático sobre el AST generado.
* **Consideraciones de Diseño:**
    * **Configuración por versión e handlers:** El consumidor inyecta una `SemanticConfiguration`, sin configuración por defecto. `v1_0` y `v1_1` registran listas distintas de handlers de statements y expresiones; por eso un nodo de 1.1, como `IfStatement` o `ReadInputExpression`, no puede analizarse accidentalmente como 1.0. Cada `StatementSemanticHandler` valida y, solo si no hay errores, actualiza el contexto.
    * **Tabla de símbolos y ámbitos:** `SemanticContext` contiene una `SemanticSymbolTable` local al módulo semantic, separada del `Environment` del interpreter. Registra tipo, inicialización, mutabilidad y, cuando es deducible, un valor numérico conocido. Los handlers de `if` analizan cada rama sobre una copia con scope propio y luego conservan en el contexto padre únicamente el estado garantizado por ambas ramas.
    * **Chequeo Estático de Tipos:** Garantiza que los valores asignados coincidan con el tipo declarado de la variable, impide reasignar símbolos inmutables y valida operaciones entre tipos compatibles (por ejemplo, permitir concatenación cuando interviene una cadena de texto o restringir la resta a tipos numéricos).
    * **Diagnósticos y contexto de expresiones:** `ExpressionAnalysis` concentra el tipo inferido, los errores y el valor numérico conocido. El statement también comunica un `expectedType` al análisis de la expresión: una declaración aporta su tipo, una asignación el tipo del símbolo destino, `printLn` espera `string` e `if` espera `boolean`. Esto permite que `readInput` y `readEnv` infieran su tipo de retorno sin realizar I/O durante el análisis.

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
3. **Semantic Analyzer:** El handler de declaración analiza la suma, infiere `number` y registra `totalScore` como inicializada en la tabla de símbolos. El handler de impresión resuelve el identificador y confirma que existe y puede leerse.
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

### Caso 3: Token no habilitado en la versión seleccionada
```printscript
{
```
* **Etapa de corte:** **Lexer**
* **Comportamiento:** Al procesar el símbolo con la configuración de PrintScript 1.0, el lexer no encuentra un matcher que pueda reconocer `{` y emite un error léxico de carácter inesperado.
* **Consideración:** La configuración de 1.1 sí habilita este símbolo y lo tokeniza como `LEFT_BRACE`. Las keywords nuevas, en cambio, se tokenizan como `IDENTIFIER` en 1.0 y requieren que parser o análisis semántico reporten su uso no permitido según el contexto.

---

### Caso 4: Detección de Error Sintáctico
```printscript
let score = 20;
```
* **Etapa de corte:** **Parser**
* **Comportamiento:** La gramática del lenguaje exige la presencia explícita del tipo (`let <identificador> : <tipo> = <expresion>;`). Al recibir el token `=` en lugar de `:`, ninguna regla de declaración coincide y el parser emite un error de sintaxis reportando el token inesperado.
* **Consideración:** Garantiza que solo programas sintácticamente válidos avancen a la etapa de análisis semántico.

---

### Caso 5: Detección de Error Semántico (Incompatibilidad de Tipos)
```printscript
let count: number = "veinte";
```
* **Etapa de corte:** **Semantic Analyzer**
* **Comportamiento:**
    * El **Lexer** y el **Parser** procesan la línea con éxito ya que la estructura gramatical es correcta.
    * El **Semantic Analyzer** evalúa el tipo declarado (`number`) y el tipo deducido de la expresión (`string`). Al detectar la discrepancia, genera un error semántico de tipos incompatibles.
* **Consideración:** Previene comportamientos impredecibles o fallos en tiempo de ejecución garantizando seguridad de tipos previa.

---

### Caso 6: Aplicación de Reglas de Linter (Advertencias de Calidad)
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

### Caso 7: Lectura de variable sin inicializar
```printscript
let count: number;
println(count);
```
* **Etapa de corte:** **Semantic Analyzer**
* **Comportamiento:** El handler de declaración registra `count` con tipo `number` e `initialized = false`. Al analizar el argumento de `println`, el handler de identificador encuentra el símbolo pero informa que la variable no está inicializada.
* **Consideración:** El semantic no inventa un valor como `0`. El valor real solo existe durante la ejecución y pertenece al `Environment` del interpreter.

---

### Caso 8: Validación contextual de `readEnv`
```printscript
let port: number = readEnv("PORT");
```
* **Etapa:** **Semantic Analyzer**
* **Comportamiento:** El handler de declaración comunica `expectedType = "number"` al handler de `readEnv`. Este valida que el nombre de la variable de entorno sea una expresión de tipo `string` e informa que el resultado de la expresión es `number`.
* **Consideración:** El semantic no consulta `PORT` ni intenta convertir su contenido. El interpreter deberá leer el valor real y convertirlo a número; si no puede hacerlo, será un error de runtime. `readInput` sigue el mismo contrato, usando su prompt como argumento string.

---

## 4. Matriz de Responsabilidades

| Etapa | Entrada | Salida | Manejo de Diagnósticos | Consideración Principal |
| :--- | :--- | :--- | :--- | :--- |
| **Lexer** | Flujo de caracteres | Lista de Tokens | Errores Léxicos | Lectura en streaming y cálculo de coordenadas de posición. |
| **Parser** | Lista de Tokens | AST (`Program`) | Errores Sintácticos | Motor declarativo y precedencia de operadores. |
| **Semantic** | AST (`Program`) + `SemanticConfiguration` | Diagnósticos semánticos | Errores Semánticos | Handlers configurables por versión; tipos, inicialización, mutabilidad, scopes y análisis contextual de expresiones. |
| **Linter** | AST (`Program`) | Notificaciones | Advertencias / Errores | Reglas de estilo y buenas prácticas configurables. |
| **Formatter**| AST (`Program`) | Código formateado | Excepciones de formato | Estandarización idempotente de la presentación del código. |
| **Interpreter**| AST (`Program`) | Ejecución / Salida | Errores de Runtime | Evaluación en memoria y desacoplamiento de la salida mediante interfaces. |
```
