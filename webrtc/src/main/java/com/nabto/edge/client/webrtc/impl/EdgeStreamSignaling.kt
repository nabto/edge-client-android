package com.nabto.edge.client.webrtc.impl

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nabto.edge.client.Connection
import com.nabto.edge.client.NabtoRuntimeException
import com.nabto.edge.client.ktx.awaitExecute
import com.nabto.edge.client.ktx.awaitOpen
import com.nabto.edge.client.ktx.awaitReadAll
import com.nabto.edge.client.ktx.awaitStreamClose
import com.nabto.edge.client.ktx.awaitWrite
import com.nabto.edge.client.webrtc.EdgeSignaling
import com.nabto.edge.client.webrtc.EdgeWebrtcError
import com.nabto.edge.client.webrtc.SignalMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

data class RTCInfo(
    @JsonProperty("SignalingStreamPort") val signalingStreamPort: Long
)

class EdgeStreamSignaling(conn: Connection) : EdgeSignaling {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val messageFlow = MutableSharedFlow<SignalMessage>(replay = 16)
    private val mapper = jacksonObjectMapper()

    private val coap = conn.createCoap("GET", "/p2p/webrtc-info")
    private val stream = conn.createStream()

    private val initialized = CompletableDeferred<Unit>()

    override suspend fun connect() {
        coap.awaitExecute()

        if (coap.responseStatusCode != 205) {
            EdgeLogger.error("Unexpected /p2p/webrtc-info return code ${coap.responseStatusCode}")
            throw EdgeWebrtcError.SignalingFailedToInitialize()
        }

        val rtcInfo = if (coap.responseContentFormat == 60) {
            val cborMapper = CBORMapper().registerKotlinModule()
            cborMapper.readValue(coap.responsePayload, RTCInfo::class.java)
        } else {
            mapper.readValue(coap.responsePayload, RTCInfo::class.java)
        }

        stream.awaitOpen(rtcInfo.signalingStreamPort.toInt())
        initialized.complete(Unit)
        EdgeLogger.info("")
        scope.launch {
            messageFlow.collect { msg -> sendMessage(msg) }
        }
    }

    override suspend fun disconnect() {
        try {
            stream.awaitStreamClose()
        } catch (exception: NabtoRuntimeException) {
            EdgeLogger.warning("Attempted to close signaling service but received error $exception")
        }
    }

    private suspend fun sendMessage(msg: SignalMessage) {
        val json = mapper.writeValueAsString(msg)
        val lenBytes = byteArrayOf(
            (json.length shr 0).toByte(),
            (json.length shr 8).toByte(),
            (json.length shr 16).toByte(),
            (json.length shr 24).toByte()
        )
        val res = lenBytes + json.toByteArray(Charsets.UTF_8)

        // @TODO: Catch and output errors.
        stream.awaitWrite(res)
    }

    override suspend fun send(msg: SignalMessage) {
        messageFlow.emit(msg)
    }

    override suspend fun recv(): SignalMessage {
        initialized.await()

        // @TODO: Catch and output errors for this and the awaitReadAll below.
        val lenData = stream.awaitReadAll(4)
        val len =
            ((lenData[0].toUInt() and 0xFFu)) or
                    ((lenData[1].toUInt() and 0xFFu) shl 8) or
                    ((lenData[2].toUInt() and 0xFFu) shl 16) or
                    ((lenData[3].toUInt() and 0xFFu) shl 24)

        val json = String(stream.awaitReadAll(len.toInt()), Charsets.UTF_8)
        return mapper.readValue(json, SignalMessage::class.java)
    }
}

