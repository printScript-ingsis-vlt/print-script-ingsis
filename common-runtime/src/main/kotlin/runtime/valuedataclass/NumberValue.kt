package runtime.valuedataclass

import kotlin.math.floor

data class NumberValue(val value: Double) : Value {
    override val type = "number"

    override fun asString(): String =
        if (value == floor(value) && !value.isInfinite()) {
            value.toLong().toString() // --> 5.0 -> "5"
        } else {
            value.toString() // --> 12.5 -> "12.5"
        }
}
