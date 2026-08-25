
plugins {
    id("austral.quality")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}
