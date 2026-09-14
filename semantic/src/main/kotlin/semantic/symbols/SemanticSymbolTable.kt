package semantic.symbols

/**
 * Tabla de símbolos del análisis semántico.
 *
 * Permite que los futuros bloques como if puedan declarar
 * variables locales
 */
class SemanticSymbolTable {
    private val scopes = mutableListOf(mutableMapOf<String, SemanticSymbol>())

    // Registra el simbolo del scope actual (variable de tal nombre es este symbol)
    fun declare(name: String, symbol: SemanticSymbol) {
        scopes.last()[name] = symbol
    }

    // Busca por un simbolo dado, arrancando del scope mas interno hacia el mas externo
    fun lookup(name: String): SemanticSymbol? {
        for (scope in scopes.asReversed()) {
            scope[name]?.let { return it }
        }
        return null
    }

    // actualiza un simbolo ya existente tras un assignment. Busca otra vez de interno a externo
    fun assign(name: String, knownNumberValue: Double?) {
        for (scope in scopes.asReversed()) {
            val current = scope[name] ?: continue
            scope[name] = current.copy(initialized = true, knownNumberValue = knownNumberValue)
            return
        }
        error("Cannot assign undeclared symbol '$name'")
    }

    fun enterScope() {
        scopes.add(mutableMapOf())
    }

    fun exitScope() {
        require(scopes.size > 1) { "Cannot exit the global semantic scope" }
        scopes.removeAt(scopes.lastIndex)
    }
}
