package austral.src.main.kotlin.commun.ast

sealed interface Node { val position: Position }
data class Position(val line: Int, val column: Int)
sealed interface Stmt : Node
sealed interface Expr : Node

data class Program(
    override val position: Position,
    val statements: List<Stmt>
) : Node
