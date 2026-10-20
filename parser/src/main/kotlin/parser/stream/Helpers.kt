package parser.stream

import token.TokenType

// Helpers de construcción (azúcar sintáctico) — versión stream

fun seq(vararg rules: StreamRule): StreamRule = Seq(rules.toList())

fun choice(vararg rules: StreamRule): StreamRule = Choice(rules.toList())

fun many(rule: StreamRule): StreamRule = Many(rule)

fun opt(rule: StreamRule): StreamRule = Opt(rule)

fun ref(resolve: () -> StreamRule): StreamRule = Ref(resolve)

fun token(
    type: TokenType,
    expected: String = type.name,
): StreamRule = TokenRule(type, expected)

fun literal(
    type: TokenType,
    value: String,
): StreamRule = LiteralRule(type, value)

fun action(
    rule: StreamRule,
    transform: (Any?) -> Any?,
): StreamRule = Action(rule, transform)
