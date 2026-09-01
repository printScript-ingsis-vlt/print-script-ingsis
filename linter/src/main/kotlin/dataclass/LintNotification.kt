package dataclass

import recurses.Position

data class LintNotification(
    val rule: String, // --> El tipo de notificacion
    val severity: Severity, // --> Si es un warning o un error
    val message: String, // --> Mensaje asociado a la notificacion
    val position: Position, // --> La position de aquello que genero la notification
)
