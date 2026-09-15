package interpreter

import runtime.Environment

data class ExecutionContext(
    val environment: Environment,
    val output: Output,
    val inputProvider: InputProvider,
    val envProvider: EnvProvider,
)
