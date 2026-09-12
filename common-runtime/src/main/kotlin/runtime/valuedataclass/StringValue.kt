package runtime.valuedataclass

data class StringValue(val value: String) : Value {
    override val type = "string"

    override fun asString(): String = value
}
