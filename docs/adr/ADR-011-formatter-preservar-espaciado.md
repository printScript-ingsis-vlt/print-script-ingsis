# ADR: Reglas nullable y preservación de espaciado original en el Formatter

* **Estado:** Implementado
* **Fecha:** 2026-09-28
* **Decisores:** Equipo de desarrollo del lenguaje
* **Supera a:** [ADR-002](./ADR-002-formatter-configurable.md) (sección de defaults y "por qué no preservar espacios")

---

## Contexto

Corriendo el Test Compatibility Kit (TCK) de la cátedra contra nuestra implementación del `Formatter`, varios tests fallaban de forma sistemática. Investigando byte a byte los fixtures (`main.ps` vs `golden.ps`), se confirmó que el TCK espera un comportamiento distinto al que describía ADR-002: cuando una regla de formato **no está presente en el `config.json`** del test, el formatter debe **preservar el espaciado original del archivo fuente**, no aplicar un valor por defecto fijo (`true`/`1`).

Con el diseño anterior (`FormattingRules` con defaults concretos `true`/`1`), no había forma de distinguir "el usuario no configuró esta regla" de "el usuario la configuró explícitamente en su valor por defecto". Esto hacía imposible pasar los tests del TCK que dependen de que el archivo original se mantenga intacto cuando no se pide una regla específica.

---

## Decisión

1. **Todos los campos de `FormattingRules` pasan a ser nullable** (`Boolean?` / `Int?`), con default `null`.
   - `null` → no forzar la regla, preservar el espaciado original del archivo.
   - `true` / `false` / un `Int` → forzar esa regla exactamente como antes.
2. **`Formatter.format()` recibe los tokens del lexer además del `Program`** (`format(tokens: List<Token>, program: Program): String`), porque para preservar el espaciado original hace falta saber dónde empezaba y terminaba cada token en el archivo fuente. El AST solo no alcanza — no contiene esa información posicional entre tokens hermanos.
3. Se **descartó pasar el texto fuente crudo**: alcanza con la `Position(line, column)` de cada token para reconstruir el espacio exacto entre dos tokens consecutivos (resta de columnas), sin necesidad de mantener el string original en memoria.
4. Se agregaron dos reglas nuevas, con el mismo patrón nullable, pedidas por el TCK:
   - `spaceAroundPrintParens: Boolean?` — espacio pegado a los paréntesis de `println` (ej. `println ( x );`).
   - `ifBraceOnNewLine: Boolean?` — llave del `if` en la línea siguiente en vez de la misma línea.

### Componentes modificados

| Componente | Cambio |
| :--- | :--- |
| `FormattingRules.kt` | Todos los campos nullable; se agregan `spaceAroundPrintParens` e `ifBraceOnNewLine`; constantes `DEFAULT_INDENTATION_SPACES` (ahora `2`, antes `4`) y `DEFAULT_NEWLINES_BEFORE_PRINTLN` como fallback solo para esos dos campos (no se implementó preservación para indentación ni saltos de línea, ver Alcance). |
| `Formatter.kt` | `format()` ahora recibe `tokens: List<Token>`. |
| `PrintScriptFormatter.kt` | Nueva función `originalGap(position, beforeIndex, afterIndex)` que ubica el token en esa posición y calcula el espacio real entre dos tokens relativos a él. Se usa en `formatVariableDeclaration` y `formatAssignment` cuando la regla correspondiente es `null`. |
| `formatter/build.gradle.kts` | Se agrega dependencia a `:common-lexer-parser` (para acceder a `Token`). |
| `CLI/Commands.kt` | `loadProgram()` ahora también expone los tokens (antes los descartaba) para pasárselos al formatter. |

### Ejemplo

```kotlin
// Antes (ADR-002): siempre forzaba espacio, sin importar el original
FormattingRules() // spaceBeforeColon = true (default)
formatter.format(program) // "let x : number = 5;" siempre

// Ahora: null preserva el espaciado real del archivo
FormattingRules() // spaceBeforeColon = null (default)
formatter.format(tokens, program)
// si el original era "let x:number = 5;" (sin espacio antes de ':'),
// el resultado preserva eso: "let x:number = 5;"
```

---

## Alcance de la preservación

Solo se implementó preservación real (basada en tokens) para:
- `spaceBeforeColon`, `spaceAfterColon` (declaraciones)
- `spaceAroundEqual` (declaraciones y asignaciones)

`indentationSpaces` y `newlinesBeforePrintln` siguen usando un fallback fijo (`DEFAULT_INDENTATION_SPACES = 2`, `DEFAULT_NEWLINES_BEFORE_PRINTLN = 1`) cuando vienen en `null`, porque ningún test del TCK requería preservar esos dos específicamente. Si en el futuro se necesita, el mecanismo (`originalGap`) es extensible al mismo patrón.

---

## Otros bugs corregidos en el mismo trabajo

Detectados corriendo la suite completa del TCK, no relacionados directamente con la decisión de nullable pero corregidos en el mismo período:

- **Números como `5.0` en vez de `5`:** `formatExpression` para `NumberLiteral` ahora omite el `.0` cuando el valor es un entero exacto.
- **Líneas en blanco antes de cualquier `println`:** la regla `newlinesBeforePrintln` insertaba líneas en blanco antes de *cualquier* `println` que no fuera el primer statement. Ahora solo separa un `println` de **otro `println` inmediatamente anterior**.

---

## Consecuencias

### Positivas

✅ El formatter ahora es compatible con la suite de tests del TCK de la cátedra (21/21 en `FormatterTest`, era ~9/21 antes de este trabajo).
✅ El comportamiento por defecto (todo `null`) es no-destructivo: un archivo ya formateado a mano no se reescribe de forma innecesaria.
✅ La preservación no requiere mantener el texto fuente crudo en memoria, solo las posiciones de los tokens ya calculadas por el lexer.

### Negativas

⚠️ El formatter deja de ser "puro AST → texto": ahora depende también de los tokens, acoplando el módulo `formatter` a `common-lexer-parser`.
⚠️ Es un cambio de firma pública (`format()`), rompe cualquier consumidor externo que llamara al `Formatter` con la firma anterior (CLI y tests ya migrados en este mismo cambio).
⚠️ La preservación parcial (solo 3 de 5 reglas) puede ser confusa si no se documenta bien qué reglas preservan y cuáles usan fallback fijo.

---

## Referencias

- Fixtures del TCK inspeccionados: `formatter/1.0/assign-spacing-surrounding-equals`, `enforce-decl-spacing-before-colon`, `enforce-decl-spacing-after-colon`, `print-{0,1,2}-line-breaks-after`.
- [ADR-002](./ADR-002-formatter-configurable.md) — decisión original que este documento actualiza parcialmente.
