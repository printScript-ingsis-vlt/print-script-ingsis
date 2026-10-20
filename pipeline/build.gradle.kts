plugins {
    id("austral.quality")
}

dependencies {
    implementation(project(":common-ast"))
    implementation(project(":common-lexer-parser"))
    implementation(project(":common-result"))
    implementation(project(":common-runtime"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":semantic"))
    implementation(project(":interpreter"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
