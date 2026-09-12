plugins {
    id("austral.quality")
    application
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:4.4.0")
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
