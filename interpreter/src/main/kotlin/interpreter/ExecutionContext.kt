package interpreter

import runtime.Environment

data class ExecutionContext(
    val environment: Environment,
    val output: Output,
    val inputProvider: ScriptInputProvider,
    val envProvider: EnvProvider,
)
