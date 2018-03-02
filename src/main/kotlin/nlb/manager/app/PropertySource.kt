package nlb.manager.app

import java.io.FileReader
import java.util.*

object PropertySource {

    val mainHost: String
    val clusterHosts: List<String>
    val connectionUrlPath: String
    val attemptConnectionCount: Int
    val attemptConnectionTimeout: Long
    val badCodes: IntArray
    val requestTimeout: Double

    init {
        val properties = loadProperties()
        mainHost = properties.getProperty("main.host")
        connectionUrlPath = properties.getProperty("connection.url.path")
        attemptConnectionCount = properties.getProperty("connection.attempt.count").toInt()
        attemptConnectionTimeout = properties.getProperty("connection.attempt.timeout").toLong()
        clusterHosts = properties.getProperty("cluster.hosts").split(",")
        badCodes = properties.getProperty("http.response.codes.bad").split(",").map { code -> code.toInt() }.toIntArray()
        requestTimeout = properties.getProperty("http.request.timeout").toLong() / 1000.0
    }

    private fun loadProperties(): Properties {
        val properties = Properties()
        val reader = FileReader(System.getProperty("config.location"))
        properties.load(reader)
        reader.close()
        return properties
    }
}