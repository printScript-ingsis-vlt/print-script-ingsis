# ADR: Motor de Parsing Configurable basado en Reglas Declarativas

* **Estado:** pr creada
* **Fecha:** 2026-08-20
* **Decisores:** Equipo de desarrollo del lenguaje

---

## Contexto

Estamos construyendo un parser para un lenguaje de programación propio.

Inicialmente se implementó un *Recursive Descent Parser* clásico, donde cada producción de la gramática estaba hardcodeada en un método (`variableDeclaration()`, `expression()`, `term()`, etc.).

Este enfoque presenta varios problemas a medida que el lenguaje crece:
* Agregar o modificar una construcción requiere tocar el código del parser.
* La precedencia de operadores y nuevas sentencias se vuelven difíciles de mantener.
* El acoplamiento entre la gramática y la lógica de parsing es alto.
* Se desea poder configurar qué construcciones se reconocen sin modificar el motor.

Además, el intérprete consume un AST. Se necesita que el AST sea estable y solo crezca cuando aparece una nueva semántica, no por cambios de sintaxis.

---

## Decisión

Adoptar un motor de parsing genérico basado en reglas declarativas.
* La gramática se define como datos (combinando reglas primitivas).
* Un motor interpreta esas reglas y construye el AST mediante `Actions`.

### Componentes principales

| Componente | Responsabilidad |
| :--- | :--- |
| **`Rule`** | Interfaz base. Toda regla sabe parsearse a sí misma. |
| **`TokenRule`** | Reconoce un token por su `TokenType`. |
| **`Seq`** | Secuencia de reglas (todas deben tener éxito). |
| **`Choice`** | Alternativas (primera que tenga éxito). |
| **`Opt`** | Regla opcional (devuelve `null` si no aparece). |
| **`Many`** | Cero o más repeticiones. |
| **`Action`** | Transforma el resultado de una regla en un nodo AST. |
| **`Grammar`** | Definición declarativa de todas las producciones. |
| **`ConfigurableParser`** | Orquesta el parseo usando las reglas de `Grammar`. |

---

## Flujo de parseo

1. El lexer entrega `List<Token>`.
2. `ConfigurableParser` inicia en la posición 0.
3. Invoca la regla de más alto nivel (`statement` / `declaration`…).
4. Las reglas consumen tokens según su definición.
5. Los `Action` construyen los nodos del AST.
6. Se acumulan statements o errores.
7. Se retorna `Program` o una lista de `SyntaxError`.

---

## Consecuencias

### Positivas
* **Alta configurabilidad:** Agregar, quitar o modificar una construcción solo implica cambiar la definición en `Grammar`.
* El motor (`Seq`, `Opt`, `TokenRule`…) se mantiene estable.
* El AST permanece desacoplado de los detalles de parsing.
* Facilita la evolución del lenguaje de forma incremental.
* Permite experimentar con la gramática sin tocar la lógica de bajo nivel.

### Negativas
* Mayor nivel de indirección (más abstracto que un *recursive descent* clásico).
* Los errores de tipos (`Any?`) requieren cuidado en los casts dentro de los `Action`.
* El rendimiento es ligeramente inferior a un parser escrito a mano (aceptable para este proyecto).
* La curva de aprendizaje inicial es un poco más alta.

### Neutrales
* El manejo de errores y sincronización se mantiene similar al enfoque anterior.
* Se puede evolucionar hacia *Pratt parsing* para expresiones sin cambiar el resto del motor.

---

## Cómo configurar

### 1. Eliminar una construcción
Quitar la regla del `choice` correspondiente:

```kotlin
val statement = choice(
    // Grammar.declaration,  // ← se elimina
    Grammar.assignment,
    Grammar.call
)
Agregar una nueva construcción

Definir la regla en Grammar usando seq, opt, choice, etc.
Agregarla al choice de statement (o donde corresponda).
Si requiere un nuevo nodo en el AST → crearlo.
Si el intérprete debe evaluarlo → agregar el caso correspondiente.

Modificar una regla existente
Solo se cambia la definición de esa regla dentro de Grammar.
El resto del sistema no se ve afectado.

Ejemplo de regla
Kotlinval declaration: Rule = action(
    seq(
        token(TokenType.LET, "let"),
        token(TokenType.IDENTIFIER, "identifier"),
        token(TokenType.COLON, ":"),
        token(TokenType.IDENTIFIER, "type"),
        opt(seq(
            token(TokenType.EQUAL, "="),
            expression
        )),
        token(TokenType.SEMICOLON, ";")
    )
) { values ->
    val list = values as List<Any?>
    // ... construcción de VariableDeclaration
}
```

