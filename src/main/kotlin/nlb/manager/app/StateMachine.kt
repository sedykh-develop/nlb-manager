package nlb.manager.app

fun getStateByRequestCode(requestCode: Int): NodeState {
    return if (PropertySource.badCodes.contains(requestCode)) {
        NodeState.DISCONNECT
    } else {
        NodeState.CONNECT
    }
}

fun transmission(state: NodeState, node: Node) {
    when (state) {
        NodeState.DISCONNECT -> {
                node.badConnection()
                if (node.badConnection == PropertySource.attemptConnectionCount) {
                    sendStopCommand(node)
                }
        }
        NodeState.CONNECT -> {
            when (node.state) {
                NodeState.DISCONNECT -> node.successConnection()
                NodeState.STOPPED -> sendStartCommand(node)
                else -> {}
            }
        } else -> {}
    }
}

fun sendStopCommand(node: Node) {
    node.stopNlbNode()
    Runtime.getRuntime().exec(node.stopCommand)
}

fun sendStartCommand(node: Node) {
    node.successConnection()
    Runtime.getRuntime().exec(node.startCommand)
}