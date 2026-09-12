package recurses

enum class OperationType(val symbol: String) {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    ;

    companion object {
        fun fromString(symbol: String): OperationType =
            entries.find { it.symbol == symbol }
                ?: throw IllegalArgumentException("Unknown operator: $symbol")
    }
}
