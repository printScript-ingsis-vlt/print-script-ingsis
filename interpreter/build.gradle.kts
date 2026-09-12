#!/usr/bin/env kotlin

plugins {
    id("austral.quality")
}

dependencies {
    implementation(project(":common-ast"))
    implementation(project(":common-runtime"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
