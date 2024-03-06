package com.nabto.edge.client.webrtc

import com.fasterxml.jackson.annotation.JsonValue

enum class SignalMessageType(@get:JsonValue val num: Int) {
    OFFER(0),
    ANSWER(1),
    ICE_CANDIDATE(2),
    TURN_REQUEST(3),
    TURN_RESPONSE(4)
}

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
    suspend fun connect()
    suspend fun disconnect()
    suspend fun send(msg: SignalMessage)
    suspend fun recv(): SignalMessage
}
