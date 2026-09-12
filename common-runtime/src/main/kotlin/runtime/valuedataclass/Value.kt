package runtime.valuedataclass

// --> En vez de laburar con los nodos del AST se labura con los values
sealed interface Value {
    val type: String

    fun asString(): String
}
