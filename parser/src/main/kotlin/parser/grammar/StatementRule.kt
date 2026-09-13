package parser.grammar

import parser.engine.Rule

/** contrato de una regla de statement de primer nivel (declaration, assignment, println, ...) */
interface StatementRule {
    val rule: Rule
}
