# ADR 00X: Soporte de Immutabilidad (`const`) y Expresiones de Lectura (`readInput`, `readEnv`)

* **Estado:** Aceptado
* **Fecha:** 2026-09-14
* **Módulos Afectados:** `ast`, `parser`, `semantic-analyzer`, `interpreter`, `linter`

## Contexto y Problema

Se requiere incorporar el soporte para la declaración de constantes (`const`) y la lectura dinámica de datos mediante las expresiones `readInput` y `readEnv`.

El diseño debe evitar la duplicación innecesaria en el Árbol de Sintaxis Abstracta (AST) y garantizar que la inmutabilidad y compatibilidad de tipos se validen correctamente en las etapas de análisis semántico, interpretación y linting.

## Decisiones Tomadas

### 1. Inmutabilidad mediante flag en `VariableDeclaration`
En lugar de introducir un nodo `ConstDeclaration` independiente en el AST, se agrega la propiedad `val mutable: Boolean` al nodo `VariableDeclaration` existente.
* `let` produce `VariableDeclaration(..., mutable = true)`.
* `const` produce `VariableDeclaration(..., mutable = false)`.

**Justificación:** Estructurar el AST reutilizando `VariableDeclaration` reduce la complejidad del Parser, Simplifica los *Visitors/Handlers* y desacopla la semántica de declaración de la estructura sintáctica básica.

### 2. Nuevos Nodos AST para Expresiones de Entrada
Se introducen los nodos `ReadInputExpression` y `ReadEnvExpression` implementando el sellado `Expr`:
* `ReadInputExpression(val prompt: Expr?, override val position: Position)`
* `ReadEnvExpression(val envVariableName: Expr, override val position: Position)`

### 3. Separación de Responsabilidades para `readInput`
* **Semantic Analysis:** Infiere el tipo resultante y valida que las expresiones de argumentos tengan tipos compatibles.
* **Interpreter:** Maneja el I/O real (leyendo de `stdin` o variables del sistema) y realiza el *casting* o parseo de tipos en tiempo de ejecución.
* **Linter:** Aplica las reglas restrictivas de formato (por ejemplo, exigir que el argumento de `readInput` sea estrictamente un `StringLiteral` o `Identifier`).

## Consecuencias

* **Positivas:**
    * No hay proliferación de nodos redundantes en el paquete `ast`.
    * La inmutabilidad aprovecha la propiedad `mutable` que ya soportaba la tabla de símbolos (`SemanticSymbol`).
    * Excelente separación de concerns entre Sintaxis (Parser), Semántica (SemanticHandler), Ejecución (Interpreter) y Estilo/Calidad (Linter).
* **Negativas:**
    * Cualquier procesador AST que trabaje con `VariableDeclaration` debe considerar el campo `mutable`.
