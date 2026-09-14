# ADR-00X: Uso del patrón Handler + canHandle en el Interpreter

## Estado
Aceptado

## Contexto
El `Interpreter` original manejaba todos los tipos de `Stmt` y `Expr` mediante un `when` central.
Esto generaba varios problemas:

- Cada vez que se agregaba un nuevo statement o expresión había que modificar el `Interpreter`.
- El archivo crecía rápidamente y se volvía difícil de mantener.
- Violaba el principio de Open/Closed (abierto a extensión, cerrado a modificación).
- Dificultaba el testing unitario de cada caso por separado.

El mismo problema ya se había identificado en los semantic validators (issue #75), donde se pidió dejar de manejar cada tipo dentro de un `when` y validar previamente qué casos son aptos.

## Decisión
Adoptar el patrón **Handler + canHandle** tanto para statements como para expresiones.

### Interfaces

```kotlin
interface StatementHandler {
    fun canHandle(stmt: Stmt): Boolean
    fun execute(stmt: Stmt, environment: Environment, evaluate: (Expr) -> Value, output: Output)
}

interface ExpressionHandler {
    fun canHandle(expr: Expr): Boolean
    fun evaluate(expr: Expr, environment: Environment, evaluate: (Expr) -> Value): Value
}
```

## Uso en el interpreter

```kotilin
private val statementHandlers: List<StatementHandler> = listOf(
    VariableDeclarationHandler(),
    AssignmentHandler(),
    PrintStatementHandler(),
    // nuevos handlers se agregan acá
)

private val expressionHandlers: List<ExpressionHandler> = listOf(
    NumberLiteralHandler(),
    StringLiteralHandler(),
    IdentifierHandler(),
    BinaryExpressionHandler(),
)

private fun execute(stmt: Stmt) {
    val handler = statementHandlers.find { it.canHandle(stmt) }
        ?: error("No statement handler found for: ${stmt::class.simpleName}")
    handler.execute(stmt, environment, ::evaluate, output)
}

fun evaluate(expr: Expr): Value {
    val handler = expressionHandlers.find { it.canHandle(expr) }
        ?: error("No expression handler found for: ${expr::class.simpleName}")
    return handler.evaluate(expr, environment, ::evaluate)
}
```

### Consecuencias Positivas

Agregar un nuevo statement/expresión = crear una clase + sumarla a la lista.
Cada handler tiene una única responsabilidad.
El Interpreter queda delgado y solo orquesta.
Facilita el testing unitario de cada caso.
Consistencia con el enfoque pedido en los semantic validators.

### Negativas / Trade-offs

Se introduce un nivel más de indirección (búsqueda del handler).
Hay un pequeño costo de performance por el find (irrelevante en este contexto).
Se pierde el exhaustiveness check del when de Kotlin (se compensa con el error explícito).

### Alternativas consideradas

Visitor pattern clásico
Más ceremonia y menos flexible para agregar nuevos tipos sin tocar la interfaz.
Mantener el when
Más simple a corto plazo, pero no escala y viola Open/Closed.
Sealed class + when exhaustivo
Bueno mientras la jerarquía esté cerrada, pero el objetivo del proyecto es poder extender fácilmente.

Se eligió Handler + canHandle por ser el más simple, explícito y alineado con el pedido del issue #75.
