package runtime

enum class PrintScriptVersion(val versionString: String) {
    V1_0("1.0"),
    V1_1("1.1");

    companion object {
        fun fromString(version: String): PrintScriptVersion {
            return entries.find { it.versionString == version }
                ?: throw IllegalArgumentException("Versión no soportada: $version")
        }

        val DEFAULT = V1_1
    }
}
