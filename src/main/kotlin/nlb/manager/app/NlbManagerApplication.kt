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
            while (true) {
                val requestCode = try {
                    get(node.requestUrl, timeout = requestTimeout).statusCode
                } catch (e: SocketTimeoutException) {
                    500
                }
                val state = getStateByRequestCode(requestCode)
                transmission(state, node)
                TimeUnit.MILLISECONDS.sleep(sleepTime)
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
        nodes.add(Node(connectionUrl, startCommand, stopCommand))
    }
    return nodes;
}