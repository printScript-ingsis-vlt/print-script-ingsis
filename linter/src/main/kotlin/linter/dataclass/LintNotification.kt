package linter.dataclass

import ast.Position

data class LintNotification(
    // Identifier of the rule that generated the notification.
    val rule: String,
    // Warning or error severity.
    val severity: Severity,
    // Message associated with the notification.
    val message: String,
    // Source position that originated the notification.
    val position: Position,
)
