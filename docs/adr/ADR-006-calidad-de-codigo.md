# ADR 006: Implementación de Control de Calidad Estático y Publicación Automática con Detekt y GitHub Actions

Estatus: Aceptado

Fecha: 2026-09-13

Contexto: Calidad de código y distribución en proyectos Kotlin/Gradle.

## 1. Contexto y Problema

El equipo requiere garantizar que el código introducido a la rama principal mantenga estándares de mantenibilidad, esté libre de olores de código (code smells) y que los artefactos generados se publiquen de manera automatizada y segura sin intervención manual imprecisa.

## 2. Opciones Consideradas

- Verificación manual local previa al Merge (Propenso a errores humanos).

- SonarQube externo (Mayor complejidad de infraestructura y costo).

- Detekt + GitHub Actions integrado (Solución nativa para Kotlin, ligera, integrable en Gradle y sin costo directo de infraestructura adicional).

## 3. Decisión Adoptada

- Se adopta Detekt como linter estático y GitHub Actions como orquestador de CI/CD por las siguientes razones:

- Integración nativa: Detekt entiende la AST (Abstract Syntax Tree) de Kotlin a la perfección.

- Fácil replicación: La ejecución local con ./gradlew detekt coincide al 100% con la ejecución del servidor de CI.

- Cero tolerancia a errores: Configuración de maxIssues: 0 para impedir la acumulación de deuda técnica.

## 4. Consecuencias

### Positivas

- Automatización: No se requiere revisión manual para detectar problemas de estilo o complejidad básica.

- Trazabilidad de artefactos: Publicación consistente de versiones mediante tags v* utilizando permisos limitados al ámbito de GitHub Actions.

- Tiempos de ejecución óptimos: Uso del caché de Gradle integrado en actions/setup-java@v4.

### Negativas / Mitigaciones

- Falsos positivos de análisis estático: Mitigable mediante supresiones puntuales @Suppress("...") justificadas en código.

- Tiempos de build más estrictos: El build fallará si se excede la complejidad de métodos (40 líneas) o número de retornos (máximo 2), obligando a refactorizar al desarrollador antes del merge.
