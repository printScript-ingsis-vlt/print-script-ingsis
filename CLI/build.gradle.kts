plugins {
    id("austral.quality")
    application
    // le pongo application para que sea un ejecutable
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:4.4.0")
    // llamo al clikt
    implementation(project(":common"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":interpreter"))
    implementation(project(":formatter"))
    implementation(project(":linter"))
}

application {
    mainClass.set("cli.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "cli.MainKt"
    }
}
