package austral.src.main.kotlin.interpreter.commun


sealed interface Node { val position: Position }
data class Position(val line: Int, val column: Int)
sealed interface Stmt : Node
sealed interface Expr : Node

// todos los nodos que se utilizan
data class Program(
    override val position: Position,
    val statements: List<Stmt>
) : Node

data class VariableDeclaration(
    val name: String,
    val type: String,
    val value: Expr?,
    override val position: Position
) : Stmt

data class BinaryExpression(
    val left: Expr,
    val operator: String,
    val right: Expr,
    override val position: Position
) : Expr

data class NumberLiteral(val value: Double, override val position: Position) : Expr
data class StringLiteral(val value: String, override val position: Position) : Expr
data class Identifier(val name: String, override val position: Position) : Expr