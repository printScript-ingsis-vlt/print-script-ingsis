# ADR-004: Lexer extensible mediante matchers de categorías léxicas

* **Estado:** Implementado
* **Fecha:** 2026-09-08
* **Decisores:** Equipo de desarrollo del lenguaje

---

## Contexto

`StreamLexer` leía el `Reader`, mantenía la posición y, además, contenía las reglas para
identificadores, palabras reservadas, números, strings y símbolos. El despacho ocurría en
un único `when` que inspeccionaba el primer carácter de cada token.

El lenguaje crecerá con nuevas palabras reservadas (`const`, `if`, `for`) y operadores.
También puede incorporar categorías léxicas adicionales, como comentarios u operadores de
más de un carácter. Mantener todas las reglas dentro de `StreamLexer` aumentaría sus motivos
de cambio y obligaría a modificar su lógica de orquestación cada vez que se agregue una
categoría.

Se necesita conservar estas propiedades:

* Lectura por streaming desde un `Reader`, sin cargar el código fuente completo en memoria.
* Posiciones precisas de inicio y fin para tokens y errores léxicos.
* Configuración de keywords y operadores por lenguaje o dialecto.
* Un único resultado para cada carácter inicial, sin que el orden de inyección cambie
  silenciosamente la gramática.

## Decisión

Separar el lexer en un orquestador, un cursor de lectura y matchers por categoría léxica.

| Componente | Responsabilidad |
| --- | --- |
| `StreamLexer` | Saltear whitespace, emitir EOF, seleccionar un matcher y orquestar la tokenización. |
| `LexerCursor` | Encapsular el `PushbackReader`, exponer `peek` y `read`, y actualizar línea/columna. |
| `TokenMatcher` | Contrato para reconocer y consumir una categoría léxica. |
| `IdentifierMatcher` | Reconocer identificadores y resolver keywords configuradas. |
| `NumberMatcher` | Reconocer enteros y decimales; exige al menos un dígito después de `.`. |
| `StringMatcher` | Reconocer strings entre comillas simples o dobles y reportar strings sin cierre. |
| `OperatorMatcher` | Reconocer símbolos y operadores configurados con máxima coincidencia. |
| `LexerConfiguration` | Contener los mapas de keywords y operadores del lenguaje. |

El contrato de extensión es:

```kotlin
interface TokenMatcher {
    val name: String

    fun canStartWith(character: Char): Boolean

    fun match(
        cursor: LexerCursor,
        start: Position,
    ): Result<Token, LexicalError>
}
```

`canStartWith` no consume caracteres ni modifica el cursor. `match` se invoca solamente
cuando el matcher fue seleccionado; debe consumir el token completo y devolver un
`Result.Success` o un `Result.Failure` para una estructura malformada.

### Selección del matcher

Para cada token, `StreamLexer` consulta todos los matchers inyectados con el carácter
obtenido mediante `LexerCursor.peek()`:

* **Cero candidatos:** devuelve `LexicalError` por carácter inesperado.
* **Un candidato:** delega en su método `match`.
* **Más de un candidato:** aborta con un error de configuración ambigua.

Esto evita adoptar una política implícita de “primer matcher que devuelve `true`”. Los
matchers deben tener prefijos iniciales no superpuestos. Cuando varias construcciones
comparten prefijo —por ejemplo, `=`, `==` y `===`— pertenecen al mismo
`OperatorMatcher`, que resuelve la máxima coincidencia.

### Configuración

Las keywords se mantienen en `IdentifierMatcher` porque comparten exactamente la misma
forma léxica que un identificador. Agregar `const`, `if` o `for` requiere añadir el
`TokenType` correspondiente y una entrada al mapa de keywords, no crear un matcher por
palabra.

```kotlin
val configuration = LexerConfiguration(
    keywords = mapOf(
        "let" to TokenType.LET,
        "const" to TokenType.CONST,
        "if" to TokenType.IF,
    ),
    operators = mapOf(
        "=" to TokenType.EQUAL,
        "==" to TokenType.EQUAL_EQUAL,
        ">=" to TokenType.GREATER_OR_EQUAL,
    ),
)

val lexer = StreamLexer.fromString("const value = 12.5", configuration)
```

`OperatorMatcher` acepta prefijos compartidos de forma deliberada y consume la alternativa
más larga configurada. Por ejemplo, para `==`, no emite dos tokens `EQUAL`.

## Reglas y validaciones léxicas actuales

* Un identificador comienza con una letra o `_`; luego puede contener letras, dígitos o `_`.
* Las keywords se resuelven después de consumir el identificador completo. Por ello, `letx`
  es un `IDENTIFIER`, no `LET` seguido de `IDENTIFIER`.
* Los números comienzan con un dígito. Un punto decimal requiere al menos un dígito posterior:
  `3.14` es válido y `7.` produce `LexicalError` en el punto decimal.
* Los strings comienzan con `'` o `"`; llegar a un salto de línea o EOF antes del cierre
  produce un error léxico en la posición de apertura.
* Los operadores no pueden configurarse vacíos ni contener whitespace.
* La posición del cursor siempre representa el próximo carácter por consumir. Esto preserva
  correctamente los rangos de cada token y la ubicación de errores.

## Consecuencias

### Positivas

* **Responsabilidad única:** `StreamLexer` deja de contener las reglas de cada categoría y
  `LexerCursor` es el único responsable de la lectura y de las posiciones.
* **Extensibilidad:** una categoría nueva se agrega mediante un nuevo `TokenMatcher` y su
  inyección, sin modificar el flujo principal del lexer.
* **Configurabilidad:** keywords y operadores son datos del lenguaje y pueden variar por
  configuración.
* **Testabilidad:** cada matcher puede probarse con entradas y errores específicos, mientras
  que `StreamLexer` se prueba por su selección y orquestación.
* **Evolución localizada:** escapes de strings, exponentes numéricos o nuevos operadores
  afectan principalmente a su matcher correspondiente.

### Negativas

* Hay más clases e indirección que en un único `when`.
* Los matchers comparten el mismo cursor; su contrato de consumo debe respetarse para evitar
  errores de posición o ciclos sin avance.
* Las categorías con el mismo carácter inicial requieren diseño explícito. Por ejemplo,
  comentarios que comiencen con `/` deben ser responsabilidad de `OperatorMatcher` o de una
  categoría que sea propietaria de ese prefijo.

### Compatibilidad

Se conserva el constructor que recibe `keywords` y `singleCharTokens`. Internamente adapta
los símbolos de un carácter a `LexerConfiguration` y construye los matchers por defecto.
Esto permite migrar gradualmente a la configuración nueva sin romper consumidores existentes.

## Estado de pruebas

La suite del lexer cubre tokens básicos, literales, posiciones, ciclo de vida y errores.
Incluye explícitamente el rechazo de `7.` mediante el mensaje
`"Se esperaba un dígito después del punto decimal"`.
