plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    android.set(false)
    ignoreFailures.set(false)
}