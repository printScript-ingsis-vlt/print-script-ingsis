package parser.grammar

import ast.PrintScriptVersion
import ast.VersionConfigurationProvider
import parser.engine.Choice
import parser.engine.Rule
import parser.engine.ref

object GrammarConfigurations : VersionConfigurationProvider<GrammarConfiguration> {


object GrammarConfigurations {
    private val expression = ExpressionRule().expression


    // readInput/readEnv aceptan cualquier expression como argumento (incluyendose a si
    // mismas, ej. readInput(readEnv("X"))), asi que necesitan una referencia perezosa (ref)
    // a la expression completa antes de que termine de construirse.
    private fun buildExpressionV11(): Rule {
        lateinit var expr: Rule
        val expressionRef: Rule = ref { expr }

        expr =
            ExpressionRule(
                extraPrimaries =
                    listOf(
                        BooleanLiteralRule.rule,
                        ReadInputExpressionRule(expressionRef).rule,
                        ReadEnvExpressionRule(expressionRef).rule,
                    ),
            ).expression

        return expr
    }

    val v1_0 =
        GrammarConfiguration(
            statementRules =
                listOf(
                    DeclarationRule(expression),
                    AssignmentRule(expression),
                    PrintStatementRule(expression),
                ),
        )

    val v1_1 = buildV11()

    private fun buildV11(): GrammarConfiguration {
        lateinit var statementRules: List<StatementRule>
        val statementDispatcher: Rule = ref { Choice(statementRules.map { it.rule }) }

        statementRules =
            listOf(
                DeclarationRule(expressionV1_1, extraTypeTokens = listOf(Terminals.BOOLEAN)),
                ConstDeclarationRule(expressionV1_1, extraTypeTokens = listOf(Terminals.BOOLEAN)),
                AssignmentRule(expressionV1_1),
                PrintStatementRule(expressionV1_1),
                IfStatementRule(expressionV1_1, statementDispatcher),
            )

        return GrammarConfiguration(statementRules)
    }

    val default = v1_0

    override fun getConfiguration(version: PrintScriptVersion): GrammarConfiguration =
        when (version) {
            PrintScriptVersion.V1_0 -> v1_0
            PrintScriptVersion.V1_1 -> v1_0
        }
}
