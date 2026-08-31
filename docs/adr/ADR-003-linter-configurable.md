# ADR: Analizador Estático de Código (Linter) Configurable para PrintScript

* **Estado:** Implementado
* **Fecha:** 2026-08-30
* **Decisores:** Equipo de desarrollo del lenguaje

---

## Contexto

El proyecto PrintScript requiere una herramienta de análisis estático de código (**Static Code Analyzer** o **Linter**)
que permita detectar incumplimientos de estilo, convenciones de nombres y posibles malas prácticas en el código fuente antes de su ejecución.

A diferencia del `Interpreter` (que ejecuta el código) o del `Formatter` (que reescribe el código con formato uniforme), el `Linter` debe:
1. Analizar el **AST (`Program`)** producido por el parser sin alterar el código ni detener la ejecución.
2. Emitir **notificaciones estructuradas** (`LintNotification`) que indiquen la regla infringida, severidad (`WARNING`/`ERROR`), mensaje explicativo y la **posición exacta** (`Position`: línea y columna) del nodo infractor.
3. Ser **configurable externamente** mediante un archivo JSON o YAML, permitiendo activar/desactivar reglas y personalizar parámetros según los estándares del equipo o proyecto.

---

## Decisión

Implementar un analizador estático modular basado en AST con:

1. **Configuración externa:** Carga de opciones desde archivo JSON mediante `kotlinx.serialization` con fallback a valores por defecto si el archivo no existe.
2. **Arquitectura basada en Reglas (`LintRule`):** Cada regla es una clase independiente con una única responsabilidad (cumpliendo con Single Responsibility y Open/Closed de SOLID).
3. **Orquestador (`PrintScriptLinter`):** Fachada que implementa la interfaz `Linter`, recibe la configuración, instancia las reglas activas y consolida las alertas.
4. **Notificaciones precisas (`LintNotification`):** Cada notificación incluye regla, severidad, mensaje y ubicación en el código fuente.

### Componentes Principales

| Componente | Archivo | Responsabilidad |
| :--- | :--- | :--- |
| **`Linter`** | `Linter.kt` | Interfaz pública que define el contrato de análisis (`lint(Program): List<LintNotification>`). |
| **`LintRule`** | `LintRule.kt` | Interfaz base para todas las reglas de análisis estático. |
| **`PrintScriptLinter`** | `PrintScriptLinter.kt` | Orquestador principal que activa y ejecuta las reglas según `LintConfig`. |
| **`LintConfig`** | `config/LintConfig.kt` | Data class `@Serializable` que modela las opciones configurables y enums. |
| **`LintConfigLoader`** | `config/LintConfigLoader.kt` | Carga y deserialización de configuración desde archivos JSON. |
| **`IdentifierFormatRule`** | `rules/IdentifierFormatRule.kt` | Regla que valida la convención de nombres de identificadores (`camelCase` o `snake_case`). |
| **`PrintlnArgumentRule`** | `rules/PrintlnArgumentRule.kt` | Regla que restringe los argumentos de `println` a literales o identificadores simples. |
| **`LintNotification`** | `dataclass/LintNotification.kt` | Modelo inmutable que representa un hallazgo o advertencia de linteo. |
| **`Severity`** | `dataclass/Severity.kt` | Nivel de severidad (`WARNING`, `ERROR`). |

### Reglas Soportadas en PrintScript 1.0

| Regla | Parámetro en Config | Tipo | Default | Propósito |
| :--- | :--- | :--- | :--- | :--- |
| **Formato de Identificadores** | `identifierFormat` | `IdentifierFormat` | `CAMEL_CASE` | Exige que las variables declaradas y asignadas sigan `camelCase` o `snake_case`. |
| **Argumentos de `println`** | `printlnArgumentCheck` | `Boolean` | `false` | Prohíbe pasar expresiones compuestas (ej. `BinaryExpression`) a `println`. |

---

## Flujo de Ejecución

### Paso 1: Invocación y Carga de Configuración

```kotlin
// Opción A: Con configuración por defecto (camelCase, printlnCheck=false)
val linter = PrintScriptLinter()
val notifications = linter.lint(program)

// Opción B: Con configuración explícita
val config = LintConfig(
    identifierFormat = IdentifierFormat.SNAKE_CASE,
    printlnArgumentCheck = true
)
val linter = PrintScriptLinter(config)
val notifications = linter.lint(program)

// Opción C: Desde archivo JSON
val config = LintConfigLoader.loadFromJson("linter-rules.json")
val linter = PrintScriptLinter(config)
val notifications = linter.lint(program)
```

### Paso 2: Orquestación de Reglas en `PrintScriptLinter`

```
[ LintConfig ]
      │
      ▼
PrintScriptLinter.buildRules()
      ├──> IdentifierFormatRule(config.identifierFormat)  [Siempre activa con el formato elegido]
      └──> PrintlnArgumentRule()                         [Solo si config.printlnArgumentCheck == true]
```

### Paso 3: Procesamiento y Recorrido del AST

El método `lint(program: Program)` ejecuta concurrentemente o secuencialmente cada regla sobre el árbol sintáctico:

```kotlin
override fun lint(program: Program): List<LintNotification> {
    return rules.flatMap { it.check(program) }
}
```

Cada regla inspecciona los nodos relevantes de `program.statements`:
* `IdentifierFormatRule` inspecciona `VariableDeclaration.name` y `Assignment.name`.
* `PrintlnArgumentRule` inspecciona `PrintStatement.argument`.

### Paso 4: Emisión de Notificaciones

Si se detecta un incumplimiento, la regla genera una `LintNotification` apuntando al nodo específico:

```
Program
├── VariableDeclaration(name="my_var", position=Position(1, 1))   ──> ⚠️ Warning: no cumple CAMEL_CASE (1:1)
├── Assignment(name="my_var", position=Position(2, 1))            ──> ⚠️ Warning: no cumple CAMEL_CASE (2:1)
└── PrintStatement(argument=BinaryExpression, position=Position(3, 9)) ──> ⚠️ Warning: expresión no permitida en println (3:9)
```

---

## Detalle de Reglas

### 1. `IdentifierFormatRule`

Valida que todos los nombres de variables cumplan con el estándar configurado:

* **`CAMEL_CASE`**:
  * Expresión regular: `^[a-z][a-zA-Z0-9]*$`
  * *Válidos:* `x`, `myVariable`, `totalCount1`
  * *Inválidos:* `my_variable` (snake_case), `MyVariable` (PascalCase), `MY_VAR`
* **`SNAKE_CASE`**:
  * Expresión regular: `^[a-z][a-z0-9]*(_[a-z0-9]+)*$`
  * *Válidos:* `x`, `my_variable`, `total_count_1`
  * *Inválidos:* `myVariable` (camelCase), `My_Variable`, `MY_VARIABLE`

#### Ejemplo:
```typescript
let my_number: number = 10;
```
* Con `CAMEL_CASE` → Notificación en `Position(1, 1)`: `"Identifier 'my_number' does not match CAMEL_CASE naming convention"`.

---

### 2. `PrintlnArgumentRule`

Garantiza la buena práctica de no evaluar expresiones complejas dentro de la llamada a `println`:

* **Argumentos permitidos:**
  * `Identifier` (ej: `println(result);`)
  * `NumberLiteral` (ej: `println(42.0);`)
  * `StringLiteral` (ej: `println("Operación completada");`)
* **Argumentos prohibidos:**
  * `BinaryExpression` (ej: `println(a + b);`, `println("Resultado: " + c);`)

#### Ejemplo:
```typescript
let a: number = 5;
let b: number = 10;
println(a + b);
```
* Con `printlnArgumentCheck: true` → Notificación en `Position(3, 9)`: `"println argument must be an identifier or a literal, complex expressions are not allowed"`.

---

## Configuración desde JSON

### Archivo: `linter-rules.json` o `.printscript-lint.json`

```json
{
  "identifierFormat": "SNAKE_CASE",
  "printlnArgumentCheck": true
}
```

### Comportamiento del Loader (`LintConfigLoader`)

```kotlin
val config = LintConfigLoader.loadFromJson("linter-rules.json")
```

1. **Archivo existe y es válido:** Deserializa los campos a `LintConfig`.
2. **Archivo inexistente:** Retorna `LintConfig()` con valores por defecto (`CAMEL_CASE`, `printlnArgumentCheck = false`).
3. **Propiedades no especificadas:** Toman su valor por defecto gracias a los argumentos nombrados de Kotlin.
4. **Campos desconocidos:** Se ignoran automáticamente (`ignoreUnknownKeys = true`).

---

## Cobertura de Tests

El módulo cuenta con una suite completa de pruebas unitarias y de integración construida con **JUnit 5**:

### 1. `LintConfigLoaderTest` (3 tests)
* ✅ `load default when file does not exist`: Comprueba el fallback a defaults.
* ✅ `load from test resources json file`: Carga desde `src/test/resources/test-lint-rules.json`.
* ✅ `load custom config from dynamic json file`: Deserialización desde archivo temporal con valores arbitrarios.

### 2. `IdentifierFormatRuleTest` (5 tests)
* ✅ `should accept valid camelCase variable declaration`: Valida identificadores camelCase correctos.
* ✅ `should report warning when camelCase is violated in declaration`: Detecta snake_case cuando se exige camelCase.
* ✅ `should report warning when identifier starts with uppercase in camelCase`: Rechaza PascalCase.
* ✅ `should accept valid snake_case variable declaration`: Valida snake_case correcto.
* ✅ `should report warning when snake_case is violated in assignment`: Detecta violaciones en asignaciones.

### 3. `PrintlnArgumentRuleTest` (4 tests)
* ✅ `should accept println with identifier argument`: Permite `println(x)`.
* ✅ `should accept println with number literal argument`: Permite `println(123.0)`.
* ✅ `should accept println with string literal argument`: Permite `println("Hello World")`.
* ✅ `should report warning when println argument is a binary expression`: Reporta advertencia ante `println(a + b)`.

### 4. `PrintScriptLinterTest` (3 tests)
* ✅ `should report no warnings for clean program with default config`: Flujo limpio sin falsos positivos.
* ✅ `should not report binary expression in println when check is turned off`: Respeta regla desactivada.
* ✅ `should report multiple violations when rules are violated`: Recolección simultánea de múltiples reglas sobre un mismo programa.

---

## Integración Técnica

### `linter/build.gradle.kts`

```kotlin
plugins {
    id("austral.quality")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
```

---

## Justificaciones de Diseño

### ¿Por qué operar sobre el AST y no sobre tokens?
* **Semántica estructural clara:** El AST (`Program`, `VariableDeclaration`, `PrintStatement`, etc.) ya tiene identificados los roles de cada elemento sintáctico, evitando tener que parsear o inferir el contexto de los tokens manualmente.
* **Precisión de ubicación:** Cada nodo del AST preserva su `Position` exacta (línea y columna), permitiendo generar alertas precisas para el usuario o la CLI.

### ¿Por qué desacoplar cada regla en una clase `LintRule`?
* **Principio de Responsabilidad Única (SRP):** Cada regla solo se preocupa por su propia condición de inspección.
* **Principio Abierto/Cerrado (OCP):** Agregar nuevas reglas en versiones futuras de PrintScript (por ejemplo, detectar variables no utilizadas, prohibir `let` reasignados, etc.) solo requiere crear una nueva clase que implemente `LintRule`, sin modificar el motor del linter.

### ¿Por qué usar una lista de `LintNotification` y no excepciones?
* El análisis estático no debe abortar al encontrar el primer problema; debe continuar analizando todo el programa para reportar **todos los hallazgos en una única pasada**.

---

## Consecuencias

### Positivas
✅ **Modularidad y Extensibilidad:** Fácil incorporación de nuevas reglas en futuras versiones de PrintScript.
✅ **Alta Testabilidad:** Reglas testables de forma aislada sin requerir la ejecución de un Lexer o Parser.
✅ **Interoperabilidad:** Totalmente integrable con el CLI y reportes automáticos.
✅ **Consistencia:** Sigue el mismo patrón arquitectónico de `Formatter` y `Interpreter`.

### Negativas
⚠️ Cambios en la jerarquía o estructura del AST requerirán actualizar las reglas que dependen de esos nodos.

### Neutrales
◻️ El linter solo advierte y notifica; la decisión de detener el pipeline de compilación recae en la CLI o el usuario según la severidad (`Severity.ERROR` vs `Severity.WARNING`).

---

## Estructura del Módulo

```
linter/
├── build.gradle.kts
├── src/
│   ├── main/kotlin/
│   │   ├── config/
│   │   │   ├── LintConfig.kt                    (Data class de configuración y enums)
│   │   │   └── LintConfigLoader.kt              (Carga y parseo de JSON)
│   │   ├── dataclass/
│   │   │   ├── LintNotification.kt              (Modelo de alerta / notificación)
│   │   │   └── Severity.kt                      (Enum: ERROR, WARNING)
│   │   ├── rules/
│   │   │   ├── IdentifierFormatRule.kt          (Regla de camelCase / snake_case)
│   │   │   └── PrintlnArgumentRule.kt           (Regla de argumentos de println)
│   │   ├── LintRule.kt                          (Interfaz base de regla)
│   │   ├── Linter.kt                            (Interfaz pública del linter)
│   │   └── PrintScriptLinter.kt                 (Implementación / Orquestador)
│   └── test/
│       ├── kotlin/
│       │   ├── config/
│       │   │   └── LintConfigLoaderTest.kt
│       │   ├── rules/
│       │   │   ├── IdentifierFormatRuleTest.kt
│       │   │   └── PrintlnArgumentRuleTest.kt
│       │   └── PrintScriptLinterTest.kt
│       └── resources/
│           └── test-lint-rules.json
```

---

## Próximas Mejoras (Out of Scope para v1.0)

* Detección de variables no utilizadas o redundantes.
* Detección de código muerto (*unreachable code*).
* Soporte para configuración en formato YAML adicionalmente a JSON.
* Reglas de linteo para nuevas estructuras de control en versiones futuras (ej. `if/else`, `const`).
