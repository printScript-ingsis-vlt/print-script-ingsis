plugins {
    // vacío
}

subprojects {
    repositories {
        mavenCentral()
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
