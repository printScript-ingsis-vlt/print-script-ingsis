package austral.src.main.kotlin.commun.ast

data class NumberLiteral(val value: Double, override val position: Position) : Expr
data class StringLiteral(val value: String, override val position: Position) : Expr
data class Identifier(val name: String, override val position: Position) : Expr