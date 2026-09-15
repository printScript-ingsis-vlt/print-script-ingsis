package semantic.symbols

/**
 * Tabla de símbolos del análisis semántico.
 *
 * Permite que los futuros bloques como if puedan declarar
 * variables locales
 */

// el constructor de scopes privado permite construir una tabla a partir de una copia de scopes existentes
class SemanticSymbolTable private constructor(
    private val scopes: MutableList<MutableMap<String, SemanticSymbol>>,
) {
    constructor() : this(mutableListOf(mutableMapOf()))

    // Registra el simbolo del scope actual (variable de tal nombre es este symbol)
    fun declare(
        name: String,
        symbol: SemanticSymbol,
    ) {
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
    fun assign(
        name: String,
        knownNumberValue: Double?,
    ) {
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

    // intenta el trabajo que se debe hacer dentro de un scope
    fun <T> withScope(block: () -> T): T {
        enterScope()
        return try {
            block()
        } finally {
            exitScope()
        }
    }

    // Copia el estado actual completo de la tabla (cada mapa de cada scope)
    // en if, por ejemplo, se crearian dos copias (thenBlock y elseBlock)
    // y se analizaria independientemente sin mutar la tabla original
    fun copy(): SemanticSymbolTable =
        SemanticSymbolTable(scopes.map { scope -> scope.toMutableMap() }.toMutableList())

    // se invoca en el estado previo al if con las copias de ambos bloques y se updatea la original con el merge de ambas garantizadas
    fun mergeConditionalBranches(
        thenSymbols: SemanticSymbolTable,
        elseSymbols: SemanticSymbolTable,
    ) {
        require(scopes.size == thenSymbols.scopes.size && scopes.size == elseSymbols.scopes.size) {
            "Conditional branches must have the same scope structure"
        }

        scopes.indices.forEach { index ->
            val originalScope = scopes[index]
            val thenScope = thenSymbols.scopes[index]
            val elseScope = elseSymbols.scopes[index]

            originalScope.keys.toList().forEach { name ->
                val original = originalScope.getValue(name)
                val thenSymbol = thenScope[name] ?: original
                val elseSymbol = elseScope[name] ?: original

                originalScope[name] = mergeSymbol(original, thenSymbol, elseSymbol)
            }
        }
    }

    private fun mergeSymbol(
        original: SemanticSymbol,
        thenSymbol: SemanticSymbol,
        elseSymbol: SemanticSymbol,
    ): SemanticSymbol {
        val initialized = thenSymbol.initialized && elseSymbol.initialized
        // Solo se conserva un valor numerico si ambas ramas garantizan el mismo valor
        val knownNumberValue =
            if (initialized && thenSymbol.knownNumberValue == elseSymbol.knownNumberValue) {
                thenSymbol.knownNumberValue
            } else {
                null
            }

        return original.copy(initialized = initialized, knownNumberValue = knownNumberValue)
    }
}
