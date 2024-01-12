package com.nabto.edge.client.webrtc

import android.util.Log
import com.nabto.edge.client.Connection
import com.nabto.edge.client.Stream
import com.nabto.edge.client.ktx.awaitReadAll
import com.nabto.edge.client.ktx.awaitWrite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@Serializable
data class RTCInfo(
    @Required @SerialName("SignalingStreamPort") val signalingStreamPort: Int
)

object SignalMessageTypeAsIntSerializer : KSerializer<SignalMessageType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SignalMessageType", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): SignalMessageType {
        val num = decoder.decodeInt()
        return SignalMessageType.values()[num]
    }

    override fun serialize(encoder: Encoder, value: SignalMessageType) {
        encoder.encodeInt(value.num)
    }
}

@Serializable(with = SignalMessageTypeAsIntSerializer::class)
enum class SignalMessageType(val num: Int) {
    OFFER(0),
    ANSWER(1),
    ICE_CANDIDATE(2),
    TURN_REQUEST(3),
    TURN_RESPONSE(4)
}

@Serializable
data class SDP(
    val type: String,
    val sdp: String
)

@Serializable
data class SignalingIceCandidate(
    val sdpMid: String,
    val candidate: String
)

@Serializable
data class TurnServer(
    val hostname: String,
    val port: Int,
    val username: String,
    val password: String
)

@Serializable
data class MetadataTrack(
    val mid: String,
    val trackId: String
)

@Serializable
data class SignalMessageMetadata(
    val tracks: List<MetadataTrack>?,
    val noTrickle: Boolean = false,
    val status: String
)

@Serializable
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

@OptIn(ExperimentalSerializationApi::class)
class EdgeStreamSignaling(conn: Connection) : EdgeSignaling {
    private val tag = "EdgeStreamSignaling"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val stream: Stream
    private val messageFlow = MutableSharedFlow<SignalMessage>()

    init {
        // @TODO: figure out how we can use async functions instead of blocking.
        val coap = conn.createCoap("GET", "/webrtc/info")
        val result = coap.execute()

        if (coap.responseStatusCode != 205) {
            Log.e(tag, "Unexpected /webrtc/info return code ${coap.responseStatusCode}")
            // @TODO: Throw an exception here
        }

        val rtcInfo = Cbor.decodeFromByteArray<RTCInfo>(coap.responsePayload)

        stream = conn.createStream()
        stream.open(rtcInfo.signalingStreamPort)

        scope.launch {
            messageFlow.collect { msg -> sendMessage(msg) }
        }
    }

    private suspend fun sendMessage(msg: SignalMessage) {
        val json = Json.encodeToString(msg)
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
        val json = Json.encodeToString(msg)
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

    override suspend fun recv(): SignalMessage {
        val lenData = stream.awaitReadAll(4)
        val len =
            ((lenData[0].toUInt() and 0xFFu)) or
                    ((lenData[1].toUInt() and 0xFFu) shl 8) or
                    ((lenData[2].toUInt() and 0xFFu) shl 16) or
                    ((lenData[3].toUInt() and 0xFFu) shl 24)

        val json = String(stream.readAll(len.toInt()), Charsets.UTF_8)
        Log.i(tag, "Recv: $json")
        return Json.decodeFromString(json)
    }

}


