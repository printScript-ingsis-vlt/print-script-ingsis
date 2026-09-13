package parser.grammar

import parser.engine.Rule
import parser.engine.choice

/**
 * punto de composición transitorio de las reglas de statement/expression, mientras
 * ConfigurableParser sigue dependiendo de esto directamente. Se retira cuando
 * ConfigurableParser pase a recibir una GrammarConfiguration inyectada.
 */
object Grammar {
    val expression: Rule = ExpressionRule.expression

    val declaration: Rule = DeclarationRule(expression).rule
    val assignment: Rule = AssignmentRule(expression).rule
    val printStatement: Rule = PrintStatementRule(expression).rule

    val statement: Rule = choice(declaration, assignment, printStatement)
}
