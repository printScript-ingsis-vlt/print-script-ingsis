# ADR: Arquitectura y uso del CLI de PrintScript

- **Estado:** Pending
- **Fecha:** 2026-09-13
- **Decisión relacionada:** Integración del módulo `semantic` en el flujo del CLI.

## Contexto

El proyecto PrintScript expone una interfaz de línea de comandos (CLI) para ejecutar las herramientas del compilador sobre archivos `.ps`.

Inicialmente el CLI utilizaba únicamente los módulos de **lexer** y **parser** antes de ejecutar los comandos (`run`, `fmt`, `lint`). Como consecuencia, los errores semánticos nunca eran detectados desde el CLI, permitiendo que programas inválidos continuaran hacia etapas posteriores.

El objetivo es que todos los comandos que trabajan sobre un programa válido compartan el mismo pipeline de compilación.

## Decisión

Se centraliza la carga y validación del programa en una única función (`loadProgram()`), que ejecuta todas las etapas necesarias antes de devolver un `Program`.

El flujo queda definido como:

1. **Lexer** (`StreamLexer.tokenize()`)
2. **Parser** (`ConfigurableParser.parse()`)
3. **Semantic Analyzer** (`SemanticAnalyzer.analyze()`)
4. Ejecución del comando correspondiente.

Si cualquiera de las etapas detecta errores, estos se muestran al usuario y el comando finaliza sin continuar.

## Arquitectura

```text
Archivo .ps
     │
     ▼
  StreamLexer
     │
     ▼
 ConfigurableParser
     │
     ▼
 SemanticAnalyzer
     │
     ▼
┌──────────────┐
│ run          │
│ fmt          │
│ lint         │
└──────────────┘
```

## Responsabilidad de cada comando

| Comando | Responsabilidad |
|----------|-----------------|
| `lex` | Ejecuta únicamente el lexer e imprime los tokens. |
| `run` | Ejecuta el programa luego de pasar todas las validaciones. |
| `fmt` | Formatea un programa semánticamente válido. |
| `lint` | Ejecuta el linter sobre un programa válido. |

### Excepción: `lex`

`lex` no utiliza `loadProgram()`, ya que su propósito es inspeccionar la salida del lexer y no requiere parsing ni validación semántica.

## `loadProgram()`

Esta función se convierte en el punto único de entrada para cualquier comando que necesite un `Program`.

Responsabilidades:

- tokenizar el archivo;
- parsearlo;
- ejecutar el análisis semántico;
- mostrar los errores encontrados;
- devolver el AST únicamente cuando todas las etapas son exitosas.

Esto evita duplicar lógica entre comandos y garantiza un comportamiento consistente.

## Manejo de errores

Cada etapa corta la ejecución si detecta errores.

| Etapa | Acción |
|--------|---------|
| Lexer | Muestra errores léxicos y termina. |
| Parser | Muestra errores sintácticos y termina. |
| Semantic Analyzer | Muestra errores semánticos y termina. |
| Comando | Se ejecuta únicamente con un `Program` válido. |

## Cómo ejecutar el CLI

Todos los comandos deben ejecutarse desde la raíz del proyecto mediante Gradle.

### Ejecutar un programa

```bash
./gradlew :CLI:run --args="run ../main.ps"
```

### Mostrar tokens

```bash
./gradlew :CLI:run --args="lex ../main.ps"
```

### Ejecutar el linter

```bash
./gradlew :CLI:run --args="lint ../main.ps"
```

### Formatear un archivo

```bash
./gradlew :CLI:run --args="fmt ../main.ps"
```

### Formatear sobrescribiendo el archivo

```bash
./gradlew :CLI:run --args="fmt -w ../main.ps"
```

## Nota sobre las rutas

El módulo `CLI` utiliza su propio directorio como *working directory*. Por este motivo, si el archivo se encuentra en la raíz del repositorio, debe referenciarse como:

```text
../main.ps
```

En general, los archivos deben pasarse utilizando una ruta relativa al directorio del módulo `CLI` o una ruta absoluta.

## Consecuencias

### Ventajas

- Todos los comandos utilizan el mismo pipeline.
- Los errores semánticos se detectan antes de ejecutar cualquier acción sobre el programa.
- Se elimina la duplicación de lógica entre comandos.
- El comportamiento del CLI es consistente.

### Desventajas

- `run`, `fmt` y `lint` dependen ahora del módulo `semantic`.
- `loadProgram()` concentra más responsabilidades, por lo que conviene mantener el análisis semántico encapsulado en funciones auxiliares para conservar métodos pequeños y cumplir con las reglas de Detekt.
