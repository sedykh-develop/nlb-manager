package nlb.manager.app

class Node constructor(val requestUrl: String, val startCommand: String, val stopCommand: String, var state: NodeState = NodeState.CONNECT, var badConnection: Int = 0) {

    fun successConnection() {
        state = NodeState.CONNECT
        badConnection = 0
    }

    fun badConnection() {
        if (badConnection == 0 && state != NodeState.STOPPED) {
            state = NodeState.DISCONNECT
        }
        if (badConnection > 100_000) {
            badConnection = 0
        } else {
            badConnection++
        }
    }

    fun stopNlbNode() {
        state = NodeState.STOPPED
    }
}