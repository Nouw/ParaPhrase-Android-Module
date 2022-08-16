package eu.prometech.paraphrase

import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketException
import com.neovisionaries.ws.client.WebSocketFrame
import org.json.JSONObject

class MessageListener(private val listener: (response: JSONObject) -> Unit) : WebSocketAdapter() {
    override fun onConnected(
        websocket: WebSocket?,
        headers: MutableMap<String, MutableList<String>>?
    ) {
        println("${ParaPhrase.getLogTag()} - Succesfully connected!")
    }

    override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
        println("${ParaPhrase.getLogTag()} - Connection error: $exception")
    }

    override fun onBinaryMessage(websocket: WebSocket?, binary: ByteArray?) {
        println("${ParaPhrase.getLogTag()} - Received binary message")

        val result = JSONObject(String(binary!!))

        listener(result)
    }

    override fun onDisconnected(
        websocket: WebSocket?,
        serverCloseFrame: WebSocketFrame?,
        clientCloseFrame: WebSocketFrame?,
        closedByServer: Boolean
    ) {
        println("${ParaPhrase.getLogTag()} - Disconnected")

        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
    }
}