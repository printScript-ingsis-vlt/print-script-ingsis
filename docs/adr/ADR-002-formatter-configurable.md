# ADR: Formateador de Código Configurable para PrintScript

* **Estado:** Implementado
* **Fecha:** 2026-08-25
* **Decisores:** Equipo de desarrollo del lenguaje

---

## Contexto

El proyecto PrintScript requiere una herramienta de formateo de código que garantice consistencia de estilo en los programas. Se necesita que sea configurable (por JSON) pero también tenga reglas fijas que no puedan ser modificadas para mantener estándares mínimos.

---

## Decisión

Implementar un formateador basado en AST con:

1. **Configuración externa:** Cargar reglas desde JSON con fallback a valores por defecto
2. **Reglas configurables:** Solo 4 aspectos del formato varían según la configuración
3. **Reglas fijas:** 3 aspectos que siempre se aplican sin excepción
4. **Interfaz simple:** Un solo método `format(program: Program): String`

### Componentes

| Componente | Archivo | Responsabilidad |
| :--- | :--- | :--- |
| `Formatter` | `Formatter.kt` | Interfaz que define el contrato de formateo |
| `FormattingRules` | `FormattingRules.kt` | Data class @Serializable con reglas y constantes |
| `PrintScriptFormatter` | `PrintScriptFormatter.kt` | Implementación principal del formateo |
| `FormattingConfigLoader` | `FormattingConfigLoader.kt` | Carga de configuración desde JSON |

### Reglas Configurables

| Regla | Tipo | Default | Propósito |
| :--- | :--- | :--- | :--- |
| `spaceBeforeColon` | Boolean | true | Espacio antes de `:` en declaraciones |
| `spaceAfterColon` | Boolean | true | Espacio después de `:` en declaraciones |
| `spaceAroundEqual` | Boolean | true | Espacio alrededor de `=` en asignaciones |
| `newlinesBeforePrintln` | Int | 1 | Saltos de línea antes de `println` |

### Reglas Fijas (Constantes)

```kotlin
companion object {
    const val NEWLINE_AFTER_SEMICOLON = true      // Salto de línea después de ";"
    const val MAX_SPACES_BETWEEN_TOKENS = 1       // Máximo 1 espacio entre tokens
    const val SPACE_AROUND_OPERATORS = true       // Espacio alrededor de +, -, *, /
}
```

---

## Flujo de Ejecución

### Paso 1: Invocación

```kotlin
// Opción A: Con reglas por defecto
val formatter = PrintScriptFormatter()
val result = formatter.format(program)

// Opción B: Con reglas personalizadas
val rules = FormattingRules(spaceBeforeColon = false, newlinesBeforePrintln = 2)
val formatter = PrintScriptFormatter(rules)
val result = formatter.format(program)

// Opción C: Desde archivo JSON
val rules = FormattingConfigLoader.loadFromJson(".printscriptrc.json")
val formatter = PrintScriptFormatter(rules)
val result = formatter.format(program)
```

### Paso 2: Procesamiento del AST

```
Program
├── statements: List<Stmt>
│   ├── VariableDeclaration("x", "number", NumberLiteral(5.0))
│   ├── Assignment("x", NumberLiteral(10.0))
│   └── PrintStatement(Identifier("x"))
└── position: Position
```

### Paso 3: Iteración de Statements con Control de Newlines

```kotlin
for ((index, stmt) in program.statements.withIndex()) {
    // Si NO es la primera sentencia Y es PrintStatement Y hay newlines configurados
    if (index > 0 && stmt is PrintStatement && rules.newlinesBeforePrintln > 0) {
        repeat(rules.newlinesBeforePrintln) { lines.add("") }
    }
    lines.add(formatStatement(stmt))
}
```

**Lógica:**
- `index > 0`: Solo agrega newlines extra si NO es la primera sentencia (evita líneas vacías al inicio del archivo)
- `stmt is PrintStatement`: Solo para println
- `rules.newlinesBeforePrintln > 0`: Solo si hay newlines configurados
- `lines.add("")`: Agrega líneas vacías que se convertirán en `\n` al hacer join

### Paso 4: Aplicación de Reglas Configurables

#### VariableDeclaration:
```
spaceBeforeColon = true   → "let x : number = 5.0;"
spaceBeforeColon = false  → "let x: number = 5.0;"
```

#### Assignment:
```
spaceAroundEqual = true   → "x = 5.0;"
spaceAroundEqual = false  → "x=5.0;"
```

#### PrintStatement:
```
newlinesBeforePrintln = 0 → "println(x);"      [sin newlines extra]
newlinesBeforePrintln = 1 → "println(x);"      [1 línea vacía ANTES]
newlinesBeforePrintln = 2 → "println(x);"      [2 líneas vacías ANTES]
```

**Nota:** Los newlines se agregan en el paso 3, NO en `formatPrintStatement()`. Esto garantiza que:
- No hay líneas vacías al inicio si el archivo empieza con `println`
- La responsabilidad está clara: formatear vs. separar statements

### Paso 5: Procesamiento Recursivo de Expresiones

```kotlin
formatExpression(expr: Expr): String =
    when (expr) {
        is NumberLiteral -> expr.value.toString()        // "5.0"
        is StringLiteral -> "\"${expr.value}\""          // "\"hello\""
        is Identifier -> expr.name                        // "x"
        is BinaryExpression -> formatBinaryExpression()  // "a + b"
    }
```

BinaryExpression siempre aplica `SPACE_AROUND_OPERATORS`:
```
"a + b"  (con espacios, siempre)
```

### Paso 6: Unión de Resultados

```kotlin
return lines.joinToString("\n")
```

**Ejemplo con `newlinesBeforePrintln = 1`:**

```
lines = ["let x : number = 5.0;", "x = 10.0;", "", "println(x);"]
         ↓
joinToString("\n")
         ↓
"let x : number = 5.0;\nx = 10.0;\n\nprintln(x);"
         ↓
let x : number = 5.0;
x = 10.0;

println(x);
```

**Ejemplo si el archivo empieza con `println`:**

```
lines = ["println(5.0);"]           [NO hay "" vacío al inicio]
         ↓
joinToString("\n")
         ↓
"println(5.0);"
         ↓
println(5.0);
```

---

## Configuración desde JSON

### Archivo: `.printscriptrc.json`

```json
{
  "spaceBeforeColon": true,
  "spaceAfterColon": true,
  "spaceAroundEqual": true,
  "newlinesBeforePrintln": 1
}
```

### Carga Automática

```kotlin
val rules = FormattingConfigLoader.loadFromJson(".printscriptrc.json")
```

**Comportamiento:**
- Si archivo existe y es válido → parsea y usa esas reglas
- Si archivo no existe → retorna `FormattingRules.default()`
- Si JSON es inválido → lanza `SerializationException`

---

## Statements Soportados

### 1. VariableDeclaration

```
Input:  VariableDeclaration(name="x", type="number", value=NumberLiteral(5.0))
Output: "let x : number = 5.0;"
```

### 2. Assignment

```
Input:  Assignment(name="x", value=NumberLiteral(10.0))
Output: "x = 10.0;"
```

### 3. PrintStatement

```
Input:  PrintStatement(argument=Identifier("x"))
Output: "println(x);"
```

---

## Expresiones Soportadas

### NumberLiteral
```
Input:  NumberLiteral(5.0)
Output: "5.0"
```

### StringLiteral
```
Input:  StringLiteral("hello")
Output: "\"hello\""
```

### Identifier
```
Input:  Identifier("x")
Output: "x"
```

### BinaryExpression
```
Input:  BinaryExpression(left=NumberLiteral(5.0), op="+", right=NumberLiteral(3.0))
Output: "5.0 + 3.0"

Input:  BinaryExpression(left=StringLiteral("Hello "), op="+", right=Identifier("name"))
Output: "\"Hello \" + name"
```

---

## Cobertura de Tests

### FormatterTest (8 tests)

1. ✅ `format simple variable declaration` - Verifica espacios alrededor de `:` y `=`
2. ✅ `format variable declaration without value` - Verifica declaración sin inicialización
3. ✅ `format assignment statement` - Verifica formato de asignación
4. ✅ `format binary expression in assignment` - Verifica espacios alrededor de operadores
5. ✅ `format print statement` - Verifica formato de println
6. ✅ `format print with binary expression` - Verifica println con operaciones
7. ✅ `format string concatenation` - Verifica concatenación de strings
8. ✅ `format multiple statements` - Verifica unión de múltiples statements
9. ✅ `format with custom rules - no spaces around equal` - Verifica reglas personalizadas

### FormattingConfigLoaderTest (3 tests)

1. ✅ `load default when file does not exist` - Fallback a defaults
2. ✅ `load default rules` - Carga explícita de defaults
3. ✅ `load from json file` - Deserialización desde JSON con valores custom

---

## Integración Técnica

### Build (formatter/build.gradle.kts)

```kotlin
plugins {
    id("austral.quality")
    kotlin("plugin.serialization")  // Necesario para @Serializable
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}
```

### Dependencia en buildSrc

```kotlin
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-serialization:2.0.21")
}
```

---

## Justificaciones

### ¿Por qué AST y no tokens?

- **AST = semántica clara:** Cada nodo es una construcción válida del lenguaje
- **Tokens = ruido:** Espacios, saltos de línea y comentarios se pierden (y no importa)
- **Reconstrucción limpia:** Generamos código formateado desde cero sin artefactos

### ¿Por qué 4 configurables + 3 fijos?

- **Configurables:** Permiten equipos elegir su estilo dentro de límites razonables
- **Fijos:** Garantizan coherencia mínima (nadie puede remover espacios de operadores)
- **Balance:** Similar a ktlint (errores críticos vs. advertencias configurables)

### ¿Por qué desde JSON?

- **Portabilidad:** La configuración es independiente del código
- **Versionable:** Se puede incluir en git (`.printscriptrc.json`)
- **Flexible:** Facilita CI/CD y diferentes entornos
- **Estándar:** Muchas herramientas usan JSON para configuración

### ¿Por qué no preservar espacios originales?

- Un formateador correctamente implementado **no preserva** espacios: los reconstruye
- Esto es lo que hacen herramientas como prettier, gofmt, rustfmt
- Garantiza consistencia total

---

## Consequencias

### Positivas

✅ **Flexibilidad:** 4 aspectos configurables sin comprometer estándares  
✅ **Coherencia:** Reglas fijas aseguran mínima calidad  
✅ **Testabilidad:** 12 tests cubren todos los casos principales  
✅ **Integración:** Soporta JSON como requiere la consigna  
✅ **Extensibilidad:** Agregar nuevos statements solo requiere un método `format*()`  
✅ **Mantenibilidad:** Código limpio y modular

### Negativas

⚠️ El formateador no preserva comentarios (el AST no los contiene)  
⚠️ Cambios en el AST requieren actualizar el formateador  
⚠️ La configuración es por archivo, no por statement

### Neutrales

◻️ No valida código (responsabilidad del parser)  
◻️ Puede evolucionar a linter en el futuro

---

## Guía de Uso

### Formatear un programa

```kotlin
// Leer archivo de entrada
val source = File("script.ps").readText()

// Parsear
val lexer = StreamLexer(source)
val tokens = lexer.tokenize()
val parser = ConfigurableParser(tokens)
val program = parser.parse()

// Formatear
val formatter = PrintScriptFormatter()
val formatted = formatter.format(program)

// Escribir salida
File("script.formatted.ps").writeText(formatted)
```

### Con configuración personalizada

```kotlin
val rules = FormattingRules(
    spaceBeforeColon = false,
    spaceAfterColon = false,
    spaceAroundEqual = true,
    newlinesBeforePrintln = 0
)
val formatter = PrintScriptFormatter(rules)
val formatted = formatter.format(program)
```

### Con archivo de configuración

```kotlin
val rules = FormattingConfigLoader.loadFromJson(".printscriptrc.json")
val formatter = PrintScriptFormatter(rules)
val formatted = formatter.format(program)
```

---

## Referencias

- **Presentación:** "07-herramientas-de-desarrollo.pdf" (ktlint, configuración JSON, herramientas de calidad)
- **Módulo:** `formatter/` con estructura estándar
- **Tests:** `formatter/src/test/kotlin/` (12 tests, todos pasando)
- **Configuración:** Opcional `.printscriptrc.json` en root
- **Build:** Integración con `austral.quality` plugin

---

## Estructura de Carpetas

```
formatter/
├── build.gradle.kts
├── src/
│   ├── main/kotlin/
│   │   ├── Formatter.kt                    (interfaz)
│   │   ├── FormattingRules.kt              (data class + constantes)
│   │   ├── PrintScriptFormatter.kt         (implementación)
│   │   └── FormattingConfigLoader.kt       (carga JSON)
│   └── test/kotlin/
│       ├── FormatterTest.kt                (8 tests)
│       └── FormattingConfigLoaderTest.kt   (3 tests)
```

---

## Próximas Mejoras (Out of Scope)

- Soporte para comentarios (requiere cambios en lexer/parser)
- Linter integrado (sugerencias sin modificar)
- Más opciones configurables (identación, largo de línea, etc.)
- Integración con IDE (VSCode plugin, etc.)
