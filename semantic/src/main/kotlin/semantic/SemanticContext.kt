package semantic

import semantic.symbols.SemanticSymbolTable

/**
 * Estado compartido durante el análisis semántico de un programa
 *
 * El analyzer crea un contexto único por programa y los handlers comparten su
 * tabla de símbolos.
 */
class SemanticContext(
    val symbols: SemanticSymbolTable = SemanticSymbolTable(),
)
