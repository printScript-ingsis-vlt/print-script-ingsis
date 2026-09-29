# Ejemplos manuales — PrintScript 1.0 vs 1.1

Esta carpeta **no es parte de la suite de tests automáticos**. Son archivos `.ps` de
ejemplo para correr el CLI a mano y ver cada feature funcionando (o fallando) en la
práctica, sin depender de escribir tests nuevos por cada caso.

Corré Gradle desde la **raíz del repo**, pero pasá la ruta del archivo **completa**
(con `$(pwd)` alcanza) — la tarea `:CLI:run` ejecuta desde adentro del módulo `CLI/`,
así que una ruta relativa tipo `CLI/examples/...` no se encuentra.

## Comandos y flags

```
mylang lex <archivo.ps>  [-v|--version|--lang-version <1.0|1.1>]
mylang run <archivo.ps>  [-v|--version|--lang-version <1.0|1.1>]
mylang fmt <archivo.ps>  [-w|--write]
mylang lint <archivo.ps>
```

| Comando | Flag de versión | Default |
| --- | --- | --- |
| `lex` | sí | `1.1` |
| `run` | sí | `1.1` |
| `fmt` | no tiene | siempre `1.1` |
| `lint` | no tiene | siempre `1.1` |

Como todavía no hay un binario `mylang` instalado, se corre así (`--args` recibe
exactamente lo que escribirías después de `mylang`):
```bash
./gradlew :CLI:run --args="run --version 1.0 $(pwd)/CLI/examples/v1.0/hello.ps" --console=plain -q
```

Si preferís el binario real en vez de pasar por Gradle cada vez:
```bash
./gradlew :CLI:installDist
./CLI/build/install/CLI/bin/CLI run CLI/examples/v1.1/if_else.ps
```

## Casos de prueba

### v1.0 (`v1.0/`)

**1. `hello.ps` — tiene que funcionar**
```bash
./gradlew :CLI:run --args="run --version 1.0 $(pwd)/CLI/examples/v1.0/hello.ps" --console=plain -q
```
Esperado: imprime `Hola PrintScript 1.0`.

**2. `if_else_not_supported.ps` — tiene que fallar**
```bash
./gradlew :CLI:run --args="run --version 1.0 $(pwd)/CLI/examples/v1.0/if_else_not_supported.ps" --console=plain -q
```
Esperado: `Error léxico: Caracter inesperado '{'` — confirma que 1.0 rechaza `if`.

### v1.1 (`v1.1/`)

**3. `const.ps`**
```bash
./gradlew :CLI:run --args="run $(pwd)/CLI/examples/v1.1/const.ps" --console=plain -q
```
Esperado: imprime `3.14`.

**4. `boolean.ps`**
```bash
./gradlew :CLI:run --args="run $(pwd)/CLI/examples/v1.1/boolean.ps" --console=plain -q
```
Esperado: imprime `true`.

**5. `if_else.ps`**
```bash
./gradlew :CLI:run --args="run $(pwd)/CLI/examples/v1.1/if_else.ps" --console=plain -q
```
Esperado: imprime `solo a`.

**6. `read_input.ps`**
```bash
./gradlew :CLI:run --args="run $(pwd)/CLI/examples/v1.1/read_input.ps" --console=plain -q
```
Esperado: pide texto por consola, después lo imprime.

**7. `read_env.ps`**
```bash
./gradlew :CLI:run --args="run $(pwd)/CLI/examples/v1.1/read_env.ps" --console=plain -q
```
Esperado: imprime tu `$HOME` real.

**8. `mixed.ps`** (const + if/else + readInput combinados)
```bash
./gradlew :CLI:run --args="run $(pwd)/CLI/examples/v1.1/mixed.ps" --console=plain -q
```
Esperado: pide tu nombre, lo imprime.

### Formatter y Lint (mismos archivos de `v1.1/`)

**9. Formatter — indentado de `if` anidado**
```bash
./gradlew :CLI:run --args="fmt $(pwd)/CLI/examples/v1.1/if_else.ps" --console=plain -q
```
Esperado: cada nivel de `if` con 4 espacios más de indentación que el anterior.

**10. Lint sobre un script válido**
```bash
./gradlew :CLI:run --args="lint $(pwd)/CLI/examples/v1.1/const.ps" --console=plain -q
```
Esperado: `No se encontraron problemas.`
