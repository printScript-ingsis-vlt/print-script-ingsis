# ADR-007: Análisis semántico extensible mediante handlers y tabla de símbolos

* **Estado:** Implementado
* **Fecha:** 2026-09-13
* **Decisores:** Equipo de desarrollo del lenguaje

---

## Contexto

El análisis semántico concentraba la validación de `Stmt` y expresiones en validadores generales. Agregar un nodo del AST obligaba a modificar condicionales centrales y varios validadores. Esto dificulta la evolución a PrintScript 1.1, que incorporará `const`, booleanos, condicionales y expresiones de entrada.

Además, semantic reutilizaba `Environment` y valores del interpreter para registrar variables. Ese entorno representa valores de ejecución, mientras que el semantic solo necesita hechos estáticos: tipo, inicialización, mutabilidad y, opcionalmente, un valor numérico conocido para diagnósticos.

## Decisión

Adoptar el patrón **Handler + canHandle** para statements y expresiones, coordinado por analizadores pequeños, y reemplazar el uso del entorno runtime por una tabla de símbolos local al módulo semantic.

### Handlers de statements

```kotlin
interface StatementSemanticHandler {
    fun canHandle(statement: Stmt): Boolean
    fun validate(statement: Stmt, context: SemanticContext): List<SemanticError>
    fun updateEnvironment(statement: Stmt, context: SemanticContext)
}
```

`SemanticAnalyzer` selecciona exactamente un handler por statement. Primero invoca `validate`; solamente si no hay errores invoca `updateEnvironment`, que actualmente actualiza el estado semántico contenido en el contexto. El nombre se conserva por compatibilidad con la interfaz introducida durante el refactor; no utiliza el `Environment` del interpreter.

Los handlers actuales son `VariableDeclarationSemanticHandler`, `AssignmentSemanticHandler` y `PrintStatementSemanticHandler`.

### Handlers de expresiones

```kotlin
interface ExpressionSemanticHandler {
    fun canHandle(expression: Expr): Boolean
    fun analyze(expression: Expr, context: SemanticContext, analyzeChild: (Expr) -> ExpressionAnalysis): ExpressionAnalysis
}
```

`ExpressionSemanticAnalyzer` resuelve el handler y entrega `analyzeChild` a los handlers compuestos. Así `BinaryExpressionSemanticHandler` analiza sus operandos sin acoplarse a los handlers concretos de literales o identificadores.

El resultado común es:

```kotlin
data class ExpressionAnalysis(
    val type: String?,
    val errors: List<SemanticError>,
    val knownNumberValue: Double? = null,
)
```

`knownNumberValue` no es un valor runtime: permite detectar una división por cero cuando el divisor es un literal, una expresión constante o un identificador cuyo valor numérico se conoce estáticamente.

### Configuración y despacho seguro

`DefaultSemanticConfiguration` construye la composición vigente de handlers. Para admitir una construcción nueva se crea su handler y se lo agrega a esa configuración, sin cambiar la lógica de orquestación.

Ambos analizadores fallan explícitamente si no existe handler o si hay más de uno que acepte el mismo nodo. Así una configuración incompleta o ambigua no queda oculta por el orden de una lista.

### Tabla de símbolos

`SemanticContext` contiene una `SemanticSymbolTable`. Cada `SemanticSymbol` guarda:

| Campo | Uso semántico |
| --- | --- |
| `type` | Comprobar tipos en asignaciones y expresiones. |
| `initialized` | Rechazar lecturas de variables declaradas pero sin valor. |
| `mutable` | Preparar la validación de reasignación de `const`. |
| `knownNumberValue` | Detectar propiedades numéricas conocidas, como división por cero. |

La tabla mantiene una pila de scopes. `lookup` busca desde el scope más interno hacia el global; `enterScope` y `exitScope` quedan preparados para los bloques de `if` de 1.1. El interpreter mantiene su propio `Environment` con valores reales y efectos de ejecución.

## Consecuencias

### Positivas

* Cada handler tiene una responsabilidad acotada y pruebas unitarias directas.
* Agregar un nodo semántico nuevo requiere extender la configuración, no modificar un `when` central en varios validadores.
* Semantic deja de depender del estado de ejecución del interpreter. Reutiliza
  `OperationType` como vocabulario compartido, sin usar `Environment` ni valores runtime.
* La tabla de símbolos prepara scope léxico, `const`, booleanos y expresiones de entrada.
* Los diagnósticos de expresiones se propagan de forma uniforme mediante `ExpressionAnalysis`.

### Negativas / trade-offs

* Hay más clases y un nivel adicional de despacho.
* Los errores de configuración de handlers se detectan en ejecución mediante validaciones explícitas, en lugar del chequeo exhaustivo de un `when` sobre una jerarquía sellada.
* El tipo se conserva como `String` para mantener compatibilidad con el AST actual; una futura migración a un tipo semántico dedicado deberá hacerse de forma coordinada.

## Alternativas consideradas

### Mantener validadores centrales con `when`

Es más directo para la versión actual, pero cada extensión del AST obliga a modificar varios puntos y no escala con PrintScript 1.1.

### Reutilizar `Environment` y valores del interpreter

Evita crear estructuras nuevas, pero mezcla datos estáticos con valores runtime y llevaba a representar valores desconocidos mediante valores ficticios como `0` o `""`.

### Duplicar `OperationType` dentro de semantic

Evita la dependencia de compilación con `common-runtime`, pero duplica el vocabulario de
operadores. Se conserva el enum compartido actual; una mejora futura puede trasladarlo a
`common-ast` o a un módulo común de lenguaje.

### Visitor clásico

Ofrece doble despacho, pero agregar una operación o variante implica más cambios coordinados en interfaces. Handler + `canHandle` es más consistente con el refactor reciente del interpreter y permite componer capacidades por configuración.
