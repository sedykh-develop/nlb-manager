@file:JvmName("NlbManagerApplication")

package nlb.manager.app

import khttp.get
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val nodes = loadNodes()

    val threadPool = Executors.newFixedThreadPool(nodes.size)

    for (node in nodes) {
        threadPool.submit({
            val sleepTime = PropertySource.attemptConnectionTimeout
            val requestTimeout = PropertySource.requestTimeout
            var startTime = System.currentTimeMillis()
            while (true) {
                val requestCode = try {
                    get(node.requestUrl, timeout = requestTimeout).statusCode
                } catch (e: SocketTimeoutException) {
                    500
                }
                val state = getStateByRequestCode(requestCode)
                transmission(state, node)
                TimeUnit.MILLISECONDS.sleep(sleepTime)
                startTime = healthCheck(startTime, node)
            }
        })
    }
}

private fun loadNodes(): MutableList<Node> {
    val nodes: MutableList<Node> = mutableListOf()
    for (host in PropertySource.clusterHosts) {
        val connectionUrl = "http://${host}/${PropertySource.connectionUrlPath}"
        val startCommand = "powershell.exe nlb start ${PropertySource.mainHost}:${host}"
        val stopCommand = "powershell.exe nlb stop ${PropertySource.mainHost}:${host}"
        val healthCheckCommand = "powershell.exe nlb query ${PropertySource.mainHost}:${host}"
        nodes.add(Node(connectionUrl, startCommand, stopCommand, healthCheckCommand))
    }
    return nodes
}

private fun healthCheck(startTime: Long, node: Node): Long {
    if (System.currentTimeMillis() - startTime > PropertySource.healthCheckTimeout) {
        if (node.state == NodeState.CONNECT) {
            Runtime.getRuntime().exec(node.healthCheckCommand).inputStream.bufferedReader(PropertySource.encoding).readLine().contains("отсоединено")
        }
        return System.currentTimeMillis()
    }

    return startTime
}