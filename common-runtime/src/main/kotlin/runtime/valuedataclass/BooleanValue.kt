package runtime.valuedataclass

data class BooleanValue(val value: Boolean) : Value {
    override val type = "boolean"

    override fun asString(): String = value.toString()
}
