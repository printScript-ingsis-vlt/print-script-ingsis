plugins {
    id("austral.quality")
    application
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:4.4.0")
    // llamo al clikt
    implementation(project(":common-lexer-parser"))
    implementation(project(":common-ast"))
    implementation(project(":common-result"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":semantic"))
    implementation(project(":interpreter"))
    implementation(project(":formatter"))
    implementation(project(":linter"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("cli.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "cli.MainKt"
    }
}
