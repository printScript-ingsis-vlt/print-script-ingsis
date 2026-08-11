plugins {
    kotlin("jvm") version "2.4.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    main { kotlin.srcDirs("austral/src/main/kotlin") }
    test { kotlin.srcDirs("austral/src/test/kotlin") }
}

application {
    mainClass.set("austral.src.main.kotlin.MainKt")
}