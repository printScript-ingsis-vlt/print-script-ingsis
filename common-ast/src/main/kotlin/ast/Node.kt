package ast

sealed interface Node {
    val position: Position
}

sealed interface Stmt : Node

sealed interface Expr : Node

// todos los nodos que se utilizan
data class Program(
    override val position: Position,
    val statements: List<Stmt>,
) : Node

// --> Statements
data class VariableDeclaration(
    val name: String,
    val type: String,
    val value: Expr?,
    override val position: Position,
    val mutable: Boolean = true,
) : Stmt

data class BinaryExpression(
    val left: Expr,
    val operator: String,
    val right: Expr,
    override val position: Position,
) : Expr

data class PrintStatement(
    val argument: Expr,
    override val position: Position,
) : Stmt

data class Assignment(
    val name: String,
    val value: Expr,
    override val position: Position,
) : Stmt

data class IfStatement(
    val condition: Expr,
    val thenBranch: List<Stmt>,
    val elseBranch: List<Stmt>?,
    override val position: Position,
) : Stmt

data class NumberLiteral(val value: Double, override val position: Position) : Expr

data class StringLiteral(val value: String, override val position: Position) : Expr

data class Identifier(val name: String, override val position: Position) : Expr

data class BooleanLiteral(
    val value: Boolean,
    override val position: Position,
) : Expr

data class ReadInputExpression(
    val prompt: Expr?,
    override val position: Position,
) : Expr

data class ReadEnvExpression(
    val envVariableName: Expr,
    override val position: Position,
) : Expr
