package com.nabto.edge.client.webrtc

import android.util.Log
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nabto.edge.client.Connection
import com.nabto.edge.client.Stream
import com.nabto.edge.client.ktx.awaitReadAll
import com.nabto.edge.client.ktx.awaitWrite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


data class RTCInfo(
    @JsonProperty("SignalingStreamPort") val signalingStreamPort: Int
)

enum class SignalMessageType(@get:JsonValue val num: Int) {
    OFFER(0),
    ANSWER(1),
    ICE_CANDIDATE(2),
    TURN_REQUEST(3),
    TURN_RESPONSE(4)
}

data class SDP(
    val type: String,
    val sdp: String
)

data class SignalingIceCandidate(
    val sdpMid: String,
    val candidate: String
)

data class TurnServer(
    val hostname: String,
    val port: Int,
    val username: String,
    val password: String
)

data class MetadataTrack(
    val mid: String,
    val trackId: String
)

data class SignalMessageMetadata(
    val tracks: List<MetadataTrack>?,
    val noTrickle: Boolean = false,
    val status: String
)

data class SignalMessage(
    val type: SignalMessageType,
    val data: String? = null,
    val servers: List<TurnServer>? = null,
    val metadata: SignalMessageMetadata? = null
)

interface EdgeSignaling {
    suspend fun send(msg: SignalMessage)
    suspend fun recv(): SignalMessage
}

class EdgeStreamSignaling(conn: Connection) : EdgeSignaling {
    private val tag = "EdgeStreamSignaling"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val stream: Stream
    private val messageFlow = MutableSharedFlow<SignalMessage>()
    private val mapper = jacksonObjectMapper()

    init {
        // @TODO: figure out how we can use async functions instead of blocking.
        val coap = conn.createCoap("GET", "/webrtc/info")
        coap.execute()

        if (coap.responseStatusCode != 205) {
            Log.e(tag, "Unexpected /webrtc/info return code ${coap.responseStatusCode}")
            // @TODO: Throw an exception here
        }

        val jsonString = coap.responsePayload.decodeToString()
        Log.i("TEST", jsonString)
        val rtcInfo = mapper.readValue(jsonString, RTCInfo::class.java)

        stream = conn.createStream()
        stream.open(rtcInfo.signalingStreamPort)

        scope.launch {
            messageFlow.collect { msg -> sendMessage(msg) }
        }
    }

    private suspend fun sendMessage(msg: SignalMessage) {
        val json = mapper.writeValueAsString(msg)
        Log.i(tag, json)
        val lenBytes = byteArrayOf(
            (json.length shr 0).toByte(),
            (json.length shr 8).toByte(),
            (json.length shr 16).toByte(),
            (json.length shr 24).toByte()
        )
        val res = lenBytes + json.toByteArray(Charsets.UTF_8)
        stream.awaitWrite(res)
    }

    override suspend fun send(msg: SignalMessage) {
        val json = mapper.writeValueAsString(msg)
        val lenBytes = byteArrayOf(
            (json.length shr 0).toByte(),
            (json.length shr 8).toByte(),
            (json.length shr 16).toByte(),
            (json.length shr 24).toByte()
        )
        val res = lenBytes + json.toByteArray(Charsets.UTF_8)
        stream.awaitWrite(res)
    }

    override suspend fun recv(): SignalMessage {
        val lenData = stream.awaitReadAll(4)
        val len =
            ((lenData[0].toUInt() and 0xFFu)) or
                    ((lenData[1].toUInt() and 0xFFu) shl 8) or
                    ((lenData[2].toUInt() and 0xFFu) shl 16) or
                    ((lenData[3].toUInt() and 0xFFu) shl 24)

        val json = String(stream.readAll(len.toInt()), Charsets.UTF_8)
        return mapper.readValue(json, SignalMessage::class.java)
    }

}


