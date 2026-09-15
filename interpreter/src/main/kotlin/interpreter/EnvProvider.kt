package interpreter

interface EnvProvider {
    fun getEnv(name: String): String?
}

class SystemEnvProvider : EnvProvider {
    override fun getEnv(name: String): String? = System.getenv(name)
}

class MockEnvProvider(private val envVars: Map<String, String>) : EnvProvider {
    override fun getEnv(name: String): String? = envVars[name]
}
