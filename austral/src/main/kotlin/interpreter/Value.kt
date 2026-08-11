package austral.src.main.kotlin.interpreter

import kotlin.math.floor

//--> En vez de laburar con los nodos del AST se labura con los values
sealed interface Value {
    val type: String
    fun asString(): String
}

data class NumberValue(val value: Double) : Value {
    override val type = "number"
    override fun asString(): String =
        if (value == floor(value) && !value.isInfinite())
            value.toLong().toString()   // --> 5.0 -> "5"
        else value.toString()           // --> 12.5 -> "12.5"
}

data class StringValue(val value: String) : Value {
    override val type = "string"
    override fun asString(): String = value
}