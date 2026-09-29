# ADR-00X: Parser extensible mediante reglas componibles e inyección de configuración

* **Estado:** Implementado
* **Fecha:** 2026-09-13
* **Decisores:** Equipo de desarrollo del lenguaje

---

## Contexto

La issue #65 pedía revisar el diseño del módulo `parser` (SOLID, configurabilidad,
extensibilidad, bajo acoplamiento) de cara a las próximas features de PrintScript 1.1
(`const`, `boolean`, `if/else`, `readInput`/`readEnv`), que van a necesitar sumar
statements y expresiones nuevas a la gramática.

Antes de esta revisión, `Grammar` era un único `object` que mezclaba los terminales
reutilizables, la regla de expresión, la regla de cada statement y la composición final
en un solo archivo. `ConfigurableParser` importaba ese objeto directamente y llamaba a
`Grammar.declaration.parse(...)` a secas.

Esa forma tenía dos problemas de diseño y, como consecuencia directa de uno de ellos,
tres bugs reales:

* **SRP violado:** un solo objeto con demasiadas razones para cambiar — tocar la regla de
  `println` o la de expresiones implicaba editar el mismo archivo que ya tenía todo lo
  demás.
* **OCP y DIP violados:** no había forma de sumar un statement nuevo sin editar
  `Grammar` directamente, ni de darle a `ConfigurableParser` un set de reglas distinto sin
  tocar su código — pese al nombre, no era configurable.
* **Bugs de 1.0:**
  * `ConfigurableParser` solo probaba `Grammar.declaration`, nunca `Grammar.statement`
    (que sí incluía `assignment`) → `x = 5;` no se podía parsear a través del pipeline
    real.
  * No existía ninguna regla de gramática para `println(...)`, pese a que `PrintStatement`
    ya existía en el AST y lo usaba el resto del proyecto.
  * `expression` era `choice(un literal numérico, un literal string, un identificador)` —
    sin operadores (`+ - * /`) ni paréntesis, así que `BinaryExpression` nunca se construía
    desde texto real.

El lexer ya había resuelto un problema análogo en PR #79
(`LexerConfiguration` + `LexerConfigurations.v1_0/v1_1` + `LexerMatcherFactory`): un
objeto de configuración inyectable que define qué reconoce el lexer, en vez de un
comportamiento fijo. El parser no tenía ningún equivalente.

## Decisión

Separar el parser en un motor de combinadores genérico, reglas de gramática componibles
por construcción del lenguaje, y una configuración inyectable de qué reglas están
disponibles.

| Componente | Responsabilidad |
| --- | --- |
| `ParserEngine` (`Seq`, `Choice`, `Many`, `Opt`, `TokenRule`, `Action`) | Combinadores genéricos, sin conocimiento de PrintScript. |
| `Ref` | Referencia perezosa a otra regla, resuelta recién al parsear — necesaria para reglas recursivas. |
| `LiteralRule` | Matchea un token por tipo *y* valor exacto — necesaria para palabras como `println` que no son keywords del lexer. |
| `Terminals` | Tokens reutilizables (`LET`, `COLON`, `EQUAL`, `SEMICOLON`, `IDENTIFIER`, `LEFT_PAREN`, `RIGHT_PAREN`). |
| `ExpressionRule` | Expresiones con precedencia: `primary → term → expression`, soporta `+ - * /` y paréntesis. |
| `StatementRule` | Contrato común de una regla de statement de primer nivel. |
| `DeclarationRule`, `AssignmentRule`, `PrintStatementRule` | Una clase por construcción del lenguaje, cada una implementando `StatementRule`. |
| `GrammarConfiguration` | Qué `StatementRule` están habilitadas. |
| `GrammarConfigurations` | Configuraciones nombradas (`v1_0`, con lugar para `v1_1`). |
| `ConfigurableParser` | Recibe una `GrammarConfiguration` por constructor y arma el `Choice` de statements a partir de ella. |

### Reglas recursivas: el combinador `Ref`

Kotlin inicializa los `val` de un `object` en orden, de arriba hacia abajo, así que una
regla no puede referenciarse a sí misma directamente. Eso bloqueaba los paréntesis en
expresiones (`(2 + 3) * 4` necesita que `expression` se contenga a sí misma) y, a futuro,
va a bloquear los bloques `{ }` de `if/else` (que contienen statements, que pueden
contener otro `if`).

```kotlin
data class Ref(private val resolve: () -> Rule) : Rule {
    override fun parse(tokens: List<Token>, pos: Int): ParseResult =
        resolve().parse(tokens, pos)
}
```

`resolve` es un lambda: se evalúa recién cuando se llama a `parse`, no cuando se construye
la regla, así que puede referenciar una `val` que todavía no terminó de inicializarse.

### Expresiones con precedencia

```kotlin
// primary := NUMBER_LITERAL | STRING_LITERAL | IDENTIFIER | '(' expression ')'
// term := primary (('*' | '/') primary)*
// expression := term (('+' | '-') term)*
```

Cada nivel pliega sus repeticiones en `BinaryExpression` anidados de izquierda a derecha
(asociatividad izquierda), y `primary` usa `Ref` para permitir paréntesis.

### Configuración inyectable

```kotlin
data class GrammarConfiguration(
    val statementRules: List<StatementRule>,
)

object GrammarConfigurations {
    private val expression = ExpressionRule.expression

    val v1_0 = GrammarConfiguration(
        statementRules = listOf(
            DeclarationRule(expression),
            AssignmentRule(expression),
            PrintStatementRule(expression),
        ),
    )

    val default = v1_0
}

class ConfigurableParser(
    configuration: GrammarConfiguration = GrammarConfigurations.default,
) : Parser {
    private val statement: Rule = Choice(configuration.statementRules.map { it.rule })
    // ...
}
```

Como efecto directo de este cambio, `ConfigurableParser` recorre **todas** las reglas de
la configuración (no solo `declaration`), así que `assignment` y `println` quedan
alcanzables por primera vez a través del parser real.

### Por qué el parser no necesita saber la versión explícitamente

A diferencia del lexer, el parser no recibe un flag de versión. El lexer ya filtra qué
tokens pueden existir según la versión configurada: en modo 1.0, tokens como `IF` o
`LEFT_BRACE` ni siquiera se generan (se tokenizan como `IDENTIFIER` o fallan como error
léxico). Una gramática que sepa reconocer `if/else` cuando esos tokens aparecen ya
funciona correctamente para las dos versiones, sin necesidad de preguntarse en cuál está.

### Sin destructuring de más de 3 componentes

`DeclarationRule` necesitaba extraer 5 valores con nombre del resultado de su `Seq`
(`let`, nombre, tipo, inicializador opcional). Detekt limita el destructuring de Kotlin a
3 componentes (`DestructuringDeclarationWithTooManyEntries`), así que en vez de acceder
por índice (`list[3]`, `list[4]`, …) se partió la regla en sub-reglas más chicas que ya
devuelven valores con nombre:

```kotlin
private data class TypedName(val name: Token, val type: Token)

private val typedName: Rule =
    action(seq(IDENTIFIER, COLON, IDENTIFIER)) { parts ->
        val (name, _, type) = parts as List<*>
        TypedName(name as Token, type as Token)
    }

private val initializer: Rule =
    action(seq(EQUAL, expression)) { parts -> (parts as List<*>)[1] as Expr }

override val rule: Rule =
    action(seq(LET, typedName, opt(initializer), SEMICOLON)) { values ->
        val (letToken, declared, value) = values as List<*>
        // ...
    }
```

`typedName` queda reutilizable para `const` en 1.1, que tiene la misma forma
(`nombre: tipo`).

## Consecuencias

### Positivas

* **Responsabilidad única:** cada regla de statement vive en su propia clase; `Grammar`
  deja de ser un god-object.
* **Abierto/cerrado:** sumar `const`/`if-else`/`readInput` en 1.1 es agregar una clase que
  implemente `StatementRule` y sumarla a una `GrammarConfiguration` nueva, no editar las
  reglas existentes.
* **Inversión de dependencias:** `ConfigurableParser` depende de una abstracción
  (`GrammarConfiguration`) inyectada, no de un singleton concreto.
* **Bugs de 1.0 resueltos:** asignaciones sueltas, `println` y expresiones con
  precedencia/paréntesis ahora se parsean de verdad a través del pipeline real.
* **Testabilidad:** cada regla se prueba de forma aislada (`GrammarExpressionTest`,
  `PrintStatementRuleTest`) y también a través de `ConfigurableParser` con distintas
  configuraciones.

### Negativas / Trade-offs

* Más clases e indirección que un único objeto con todo adentro.
* `Ref` agrega una capa de lambdas que hay que entender para poder leer las reglas
  recursivas — el costo de performance es despreciable, pero sí hay una curva de lectura.
* La partición de `DeclarationRule` en sub-reglas (`typedName`, `initializer`) es menos
  directa de seguir que un solo bloque con índices, aunque más segura.

## Alternativas consideradas

* **Mantener `Grammar` como objeto único, pero parametrizado.** Se descartó porque no
  resuelve el problema de fondo (OCP): seguiría habiendo un solo lugar donde agregar cada
  regla nueva, aunque reciba parámetros.
* **Generar el parser con una librería externa (parser generator/DSL de terceros).** Se
  descartó por alcance — cambiaría la arquitectura entera del proyecto para un problema
  que se resuelve extendiendo el motor de combinadores ya existente.
* **Un flag de versión explícito en el parser, análogo al del lexer.** Se descartó porque
  es redundante: el lexer ya restringe qué tokens puede recibir el parser según la
  versión, así que el parser no necesita esa información para comportarse correctamente.

## Estado de pruebas

La suite del parser cubre: precedencia y asociatividad de expresiones, paréntesis,
`PrintStatementRule` aislada (incluyendo el caso de que el identificador no sea
exactamente `"println"`), `Ref` con una gramática recursiva (paréntesis anidados), y la
integración completa vía `ConfigurableParser` (declaración + asignación + print en un
mismo programa, y una `GrammarConfiguration` reducida que confirma que lo no habilitado
se rechaza).

## Actualización: implementación concreta de `GrammarConfigurations.v1_1`

Esta sección documenta cómo se terminaron resolviendo, en código, los puntos que el ADR
dejaba abiertos ("a futuro, va a bloquear los bloques `{ }` de `if/else`", "`typedName`
queda reutilizable para `const`").

### `extraTypeTokens`: mismo mecanismo que `extraPrimaries`, para el tipo de una declaración

`ExpressionRule` ya recibía `extraPrimaries: List<Rule>` para sumar alternativas a
`primary` sin editar la clase. `DeclarationRule` no tenía un mecanismo equivalente para su
posición de tipo — el tipo era siempre `IDENTIFIER` a secas, lo cual alcanzaba para
`number`/`string` (el lexer los tokeniza como identificadores comunes) pero no para
`boolean`, que el lexer de 1.1 tokeniza como `BOOLEAN`, un tipo de token distinto.

```kotlin
class DeclarationRule(
    expression: Rule,
    extraTypeTokens: List<Rule> = emptyList(),
) : StatementRule {
    private val typedName: Rule = TypedNameRule(extraTypeTokens).rule
    // ...
}
```

`GrammarConfigurations.v1_0` sigue instanciando `DeclarationRule(expression)` sin el
segundo parámetro — el comportamiento de 1.0 queda **exactamente igual** que antes de
este cambio. `v1_1` pasa `extraTypeTokens = listOf(Terminals.BOOLEAN)`. Esto confirma la
misma decisión de diseño que ya regía para `extraPrimaries`: extender por parámetro en
vez de por herencia o por un `if (version == ...)` dentro de la regla.

`TypedNameRule` (nueva) extrae la lógica de `IDENTIFIER ':' tipo` que antes vivía inline
en `DeclarationRule`, para que `ConstDeclarationRule` la reutilice sin duplicarla — tal
como este mismo ADR anticipaba.

### `Ref` se usa, como estaba previsto, para los bloques de `if`

`IfStatementRule` necesita poder parsear *cualquier* statement dentro de sus bloques
`{ }`, incluido otro `if` anidado — pero esa lista de statements (incluyéndose a sí misma)
no existe todavía mientras se construye. `GrammarConfigurations` resuelve esto con el
mismo patrón `Ref` que el ADR documentaba para paréntesis, combinado con un
`lateinit var`:

```kotlin
private fun buildV11(): GrammarConfiguration {
    lateinit var statementRules: List<StatementRule>
    val statementDispatcher: Rule = ref { Choice(statementRules.map { it.rule }) }

    statementRules = listOf(
        DeclarationRule(expressionV1_1, extraTypeTokens = listOf(Terminals.BOOLEAN)),
        ConstDeclarationRule(expressionV1_1, extraTypeTokens = listOf(Terminals.BOOLEAN)),
        AssignmentRule(expressionV1_1),
        PrintStatementRule(expressionV1_1),
        IfStatementRule(expressionV1_1, statementDispatcher),
    )

    return GrammarConfiguration(statementRules)
}
```

`Ref` resuelve `statementDispatcher` recién cuando se llama a `.parse(...)`, momento en el
que `statementRules` ya está completamente inicializada — el mismo motivo por el que
sirve para expresiones parentizadas, aplicado ahora a bloques de statements.

### Un caso no anticipado: la recursión también aparece en expresiones

El ADR preveía `Ref` para statements recursivos, pero `readInput`/`readEnv` agregaron un
caso similar del lado de las expresiones: su argumento puede ser *cualquier* expresión,
incluyendo otro `readInput`/`readEnv` anidado (`readInput(readEnv("PROMPT_VAR"))`). Se
resolvió con el mismo mecanismo, aplicado a la expresión en vez de al statement:

```kotlin
private fun buildExpressionV11(): Rule {
    lateinit var expr: Rule
    val expressionRef: Rule = ref { expr }

    expr = ExpressionRule(
        extraPrimaries = listOf(
            BooleanLiteralRule.rule,
            ReadInputExpressionRule(expressionRef).rule,
            ReadEnvExpressionRule(expressionRef).rule,
        ),
    ).expression

    return expr
}
```

### `const`: reutiliza `VariableDeclaration`, no un nodo separado

`ConstDeclarationRule` parsea `const IDENTIFIER : tipo '=' expression ';'` (inicializador
obligatorio, a diferencia de `let`) y construye el mismo nodo `VariableDeclaration` que
`DeclarationRule`, con `mutable = false`. No existe un nodo `ConstDeclaration` en el AST —
la diferencia entre `let` y `const` es un campo, no una jerarquía de tipos distinta, para
que semantic e interpreter no necesiten un handler completo duplicado por una sola regla
de negocio ("no se puede reasignar").

### Por qué `readInput`/`readEnv` no restringen su argumento en la gramática

La consigna de 1.1 dice que `readInput` "solo puede llamarse con un identificador o un
literal, pero esto puede estar prendido o apagado" — es una regla del **linter**,
configurable en runtime. Si el parser restringiera la gramática a solo esas dos formas,
esa regla no podría "apagarse" nunca: la información ya se habría perdido antes de llegar
al linter. Por eso `ReadInputExpressionRule`/`ReadEnvExpressionRule` aceptan `expression`
completa como argumento, igual que `println`, y la restricción vive más arriba en el
pipeline, donde puede activarse o no.

## Estado de pruebas (actualizado)

Se suman a la cobertura descripta arriba: `BooleanLiteralRule` y su integración en
`ExpressionRule`/`DeclarationRule`; `IfStatementRule` (bloque simple, `if/else`, bloque
vacío, sin llaves falla, `else if` falla de punta a punta vía `ConfigurableParser`,
anidado); `ReadInputExpressionRule`/`ReadEnvExpressionRule` (aislado y anidado uno dentro
del otro); `ConstDeclarationRule` (con inicializador, sin inicializador falla, sin `=`
falla); y en `GrammarConfigurationsTest`, que `v1_0` seguía rechazando cada uno de estos
tokens/construcciones a nivel de gramática, no solo porque el lexer de 1.0 nunca los
produce.
