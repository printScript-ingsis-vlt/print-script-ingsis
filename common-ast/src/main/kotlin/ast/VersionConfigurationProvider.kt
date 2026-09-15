package ast

fun interface VersionConfigurationProvider<T> {
    fun getConfiguration(version: PrintScriptVersion): T
}
