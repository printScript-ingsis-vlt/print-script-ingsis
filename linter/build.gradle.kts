
plugins {
    id("austral.quality")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":common-ast"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
