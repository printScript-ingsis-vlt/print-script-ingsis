package runtime

import runtime.valuedataclass.Value

class Environment {
    private val scopes = mutableListOf(mutableMapOf<String, Variable>())

    private val currentScope
        get() = scopes.last()

    fun pushScope() {
        scopes.add(mutableMapOf())
    }

    fun popScope() {
        require(scopes.size > 1) { "Cannot remove global scope" }
        scopes.removeLast()
    }

    fun withScope(block: () -> Unit) {
        pushScope()
        try {
            block()
        } finally {
            popScope()
        }
    }

    fun declare(
        name: String,
        variable: Variable,
    ) {
        require(name !in currentScope) {
            "Variable $name already declared in this scope"
        }
        currentScope[name] = variable
    }

    fun lookup(name: String): Variable? = scopes.asReversed().firstNotNullOfOrNull { it[name] }

    fun assign(
        name: String,
        value: Value,
    ) {
        val variable =
            scopes.asReversed()
                .firstNotNullOfOrNull { it[name] }
                ?: throw NoSuchElementException("Variable $name not found")

        variable.value = value
    }
}
