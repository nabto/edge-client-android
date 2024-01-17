package com.nabto.edge.client.webrtc.impl

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nabto.edge.client.Connection
import com.nabto.edge.client.webrtc.EdgeSignaling
import com.nabto.edge.client.webrtc.EdgeStreamSignaling
import com.nabto.edge.client.webrtc.EdgeWebRTC
import com.nabto.edge.client.webrtc.EdgeWebrtcConnection
import com.nabto.edge.client.webrtc.OnClosedCallback
import com.nabto.edge.client.webrtc.OnConnectedCallback
import com.nabto.edge.client.webrtc.OnTrackCallback
import com.nabto.edge.client.webrtc.SDP
import com.nabto.edge.client.webrtc.SignalMessage
import com.nabto.edge.client.webrtc.SignalMessageType
import com.nabto.edge.client.webrtc.SignalingIceCandidate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RendererCommon
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack

class EdgeWebrtcConnectionImpl(
    conn: Connection
) : EdgeWebrtcConnection, PeerConnection.Observer, RendererCommon.RendererEvents {
    private val tag = this.javaClass.simpleName
    private lateinit var peerConnection: PeerConnection
    private val signaling: EdgeSignaling = EdgeStreamSignaling(conn)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val jsonMapper = jacksonObjectMapper()

    private var onConnectedCallback: OnConnectedCallback? = null
    private var onClosedCallback: OnClosedCallback? = null
    private var onTrackCallback: OnTrackCallback? = null

    private val dummySdpObserver = object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }

    private val localConstraints = MediaConstraints().apply {
        this.mandatory.addAll(
            listOf(
                MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"),
                MediaConstraints.KeyValuePair("offerToReceiveVideo", "true")
            )
        )
    }

    private val localAnswerObserver = object : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription?) {
            scope.launch {
                val data = jsonMapper.writeValueAsString(SDP("answer", sdp?.description ?: ""))
                val msg = SignalMessage(type = SignalMessageType.ANSWER, data = data)
                signaling.send(msg)
                peerConnection.setLocalDescription(dummySdpObserver, sdp!!)
            }
        }

        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }

    init {
        scope.launch {
            signaling.send(SignalMessage(type = SignalMessageType.TURN_REQUEST))
            messageLoop()
        }
    }

    private suspend fun messageLoop() {
        while (true) {
            val msg = signaling.recv()
            Log.i(tag, msg.toString())
            when (msg.type) {
                SignalMessageType.ANSWER -> {
                    val answerData = jsonMapper.readValue(msg.data!!, SDP::class.java)
                    val answer = SessionDescription(SessionDescription.Type.ANSWER, answerData.sdp)
                    peerConnection.setRemoteDescription(dummySdpObserver, answer)
                }

                SignalMessageType.OFFER -> {
                    val offerData = jsonMapper.readValue(msg.data!!, SDP::class.java)
                    val offer = SessionDescription(SessionDescription.Type.OFFER, offerData.sdp)
                    peerConnection.setRemoteDescription(dummySdpObserver, offer)
                    peerConnection.createAnswer(localAnswerObserver, localConstraints)
                }

                SignalMessageType.ICE_CANDIDATE -> {
                    val candidate = jsonMapper.readValue(msg.data!!, SignalingIceCandidate::class.java)
                    val iceCandidate = IceCandidate(candidate.sdpMid, 0, candidate.candidate)
                    peerConnection.addIceCandidate(iceCandidate)
                }

                SignalMessageType.TURN_REQUEST -> {}
                SignalMessageType.TURN_RESPONSE -> {
                    val iceServers = mutableListOf(
                        PeerConnection.IceServer.builder("stun:stun.nabto.net").createIceServer()
                    )

                    msg.servers?.let {
                        for (server in it) {
                            iceServers.add(
                                PeerConnection.IceServer.builder(server.hostname).run {
                                    setUsername(server.username)
                                    setPassword(server.password)
                                    createIceServer()
                                }
                            )
                        }
                    }

                    val pcOpts = PeerConnection.RTCConfiguration(null)
                    pcOpts.iceServers = iceServers

                    peerConnection = EdgeWebRTC.peerConnectionFactory.createPeerConnection(pcOpts, this) ?: run {
                        // @TODO: Error in a better way than throwing a vague exception
                        throw RuntimeException("Could not create PeerConnection")
                    }

                    onConnectedCallback?.let { it() }
                }
            }
        }
    }

    override fun onConnected(cb: OnConnectedCallback) {
        onConnectedCallback = cb
    }

    override fun onClosed(cb: OnClosedCallback) {
        onClosedCallback = cb
    }

    override fun onTrack(cb: OnTrackCallback) {
        onTrackCallback = cb
    }

    override fun onFirstFrameRendered() {}
    override fun onFrameResolutionChanged(videoWidth: Int, videoHeight: Int, rotation: Int) {}
    override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
        if (state == PeerConnection.IceConnectionState.CONNECTED) {
            // onConnectedCallback?.let { it() }
        }

        if (state == PeerConnection.IceConnectionState.CLOSED) {
            onClosedCallback?.let { it() }
        }
    }
    override fun onIceConnectionReceivingChange(p0: Boolean) {}

    override fun onIceCandidate(candidate: IceCandidate?) {
        candidate?.let { cand ->
            scope.launch {
                val data = jsonMapper.writeValueAsString(SignalingIceCandidate(sdpMid = cand.sdpMid, candidate = cand.sdp))
                signaling.send(SignalMessage(type = SignalMessageType.ICE_CANDIDATE, data = data))
            }
        }
    }

    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
    override fun onAddStream(stream: MediaStream?) {}
    override fun onRemoveStream(stream: MediaStream?) {}
    override fun onDataChannel(dataChannel: DataChannel?) {}
    override fun onRenegotiationNeeded() {
        // @TODO: Figure out what to do with renegotiation
    }

    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}

    override fun onTrack(transceiver: RtpTransceiver?) {
        super.onTrack(transceiver)
        val track = transceiver?.receiver?.track()
        track?.let { t ->
            if (t.kind() == "video") {
                val videoTrack = t as VideoTrack
                onTrackCallback?.let { cb -> cb(EdgeVideoTrackImpl(videoTrack)) }
            }

            if (t.kind() == "audio") {
                val audioTrack = t as AudioTrack
                onTrackCallback?.let { cb -> cb(EdgeAudioTrackImpl(audioTrack)) }
            }
        }
    }
}
