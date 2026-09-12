import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {

}

subprojects {
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
    }

    // El componente "java" aparece recién cuando austral.quality aplica Kotlin JVM.
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("gpr") {
                    from(components["java"])
                    groupId = "com.printscript"
                    artifactId = project.name
                    version = "1.0.0"
                }
            }

            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/printScript-ingsis-vlt/print-script-ingsis")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
}

val installHooks by tasks.registering(Copy::class) {
    description = "Copia los git hooks de .githooks a .git/hooks"
    group = "git hooks"

    from(file("$rootDir/.githooks"))
    into(file("$rootDir/.git/hooks"))

    // Asigna permisos de ejecución (rwxr-xr-x)
    filePermissions {
        user {
            read = true
            write = true
            execute = true
        }
        other {
            read = true
            execute = true
        }
        group {
            read = true
            execute = true
        }
    }
}
