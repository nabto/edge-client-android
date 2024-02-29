package com.nabto.edge.client.webrtc.impl

import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nabto.edge.client.Connection
import com.nabto.edge.client.webrtc.EdgeSignaling
import com.nabto.edge.client.webrtc.EdgeStreamSignaling
import com.nabto.edge.client.webrtc.EdgeWebRTCError
import com.nabto.edge.client.webrtc.EdgeWebrtcConnection
import com.nabto.edge.client.webrtc.OnClosedCallback
import com.nabto.edge.client.webrtc.OnConnectedCallback
import com.nabto.edge.client.webrtc.OnErrorCallback
import com.nabto.edge.client.webrtc.OnTrackCallback
import com.nabto.edge.client.webrtc.SDP
import com.nabto.edge.client.webrtc.SignalMessage
import com.nabto.edge.client.webrtc.SignalMessageType
import com.nabto.edge.client.webrtc.SignalingIceCandidate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.webrtc.AddIceObserver
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RendererCommon
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack

internal class EdgeWebrtcConnectionImpl(
    conn: Connection
) : EdgeWebrtcConnection, PeerConnection.Observer, RendererCommon.RendererEvents {
    private val tag = this.javaClass.simpleName
    private lateinit var peerConnection: PeerConnection
    private val signaling: EdgeSignaling = EdgeStreamSignaling(conn)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val jsonMapper = jacksonObjectMapper()

    private var polite = true
    private var makingOffer = false
    private var ignoreOffer = false

    private var onConnectedCallback: OnConnectedCallback? = null
    private var onClosedCallback: OnClosedCallback? = null
    private var onTrackCallback: OnTrackCallback? = null
    private var onErrorCallback: OnErrorCallback? = null

    private val offerObserver = object : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription?) {}
        override fun onCreateFailure(p0: String?) {}

        override fun onSetSuccess() {
            EdgeLogger.info("local description set to: ${peerConnection.localDescription.description}")
            scope.launch {
                sendDescription(peerConnection.localDescription)
            }
        }

        override fun onSetFailure(p0: String?) {
            // @TODO: Error logging
            EdgeLogger.error("Failed to set local description: $p0")
        }
    }

    private val renegotiationObserver = object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onCreateFailure(p0: String?) {}

        override fun onSetSuccess() {
            scope.launch {
                sendDescription(peerConnection.localDescription)
                makingOffer = false
            }
        }

        override fun onSetFailure(p0: String?) {
            EdgeLogger.error("Failed to set local description in renegotiation: $p0")
            makingOffer = false
        }
    }

    override fun connect() {
        scope.launch {
            signaling.send(SignalMessage(type = SignalMessageType.TURN_REQUEST))
            messageLoop()
        }
    }

    override fun close() {
        if (::peerConnection.isInitialized) {
            peerConnection.close()
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

    override fun onError(cb: OnErrorCallback) {
        onErrorCallback = cb
    }

    private suspend fun sendDescription(sdp: SessionDescription) {
        EdgeLogger.info("Sending description to peer: ${sdp.description}")
        val data = jsonMapper.writeValueAsString(SDP("answer", sdp.description))
        val type = when (sdp.type) {
            SessionDescription.Type.OFFER -> SignalMessageType.OFFER
            SessionDescription.Type.ANSWER -> SignalMessageType.ANSWER
            else -> SignalMessageType.OFFER
        }
        val msg = SignalMessage(type = type, data = data)
        signaling.send(msg)
    }

    private fun handleDescription(sdp: SessionDescription?) {
        if (sdp != null) {
            val offerCollision = sdp.type == SessionDescription.Type.OFFER && (makingOffer || peerConnection.signalingState() == PeerConnection.SignalingState.STABLE)
            ignoreOffer = !polite && offerCollision

            if (ignoreOffer) {
                EdgeLogger.info("Ignoring offer...")
                return
            }

            peerConnection.setRemoteDescription(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onCreateFailure(p0: String?) {}

                override fun onSetSuccess() {
                    EdgeLogger.info("Remote ${sdp.type} SDP has been set.")
                    if (sdp.type == SessionDescription.Type.OFFER) {
                        peerConnection.setLocalDescription(offerObserver)
                    }
                }

                override fun onSetFailure(p0: String?) {
                    EdgeLogger.error("Setting remote SDP failed: $p0")
                    onErrorCallback?.invoke(EdgeWebRTCError.SetRemoteDescriptionError())
                }

            }, sdp)
        } else {
            EdgeLogger.error("Null SDP in handleDescription, this shouldn't happen! Ensure your signaling is functional.")
        }
    }

    private fun handleIceCandidate(candidate: IceCandidate) {
        peerConnection.addIceCandidate(candidate, object: AddIceObserver {
            override fun onAddSuccess() {}

            override fun onAddFailure(str: String?) {
                if (!ignoreOffer) {
                    EdgeLogger.error("Failed adding ice candidate: $str")
                    onErrorCallback?.invoke(EdgeWebRTCError.ICECandidateError())
                }
            }
        })
    }

    private suspend fun messageLoop() {
        while (true) {
            val msg = signaling.recv()
            Log.i(tag, msg.toString())
            when (msg.type) {
                SignalMessageType.ANSWER -> {
                    val answerData = jsonMapper.readValue(msg.data!!, SDP::class.java)
                    val answer = SessionDescription(SessionDescription.Type.ANSWER, answerData.sdp)
                    handleDescription(answer)
                }

                SignalMessageType.OFFER -> {
                    val offerData = jsonMapper.readValue(msg.data!!, SDP::class.java)
                    val offer = SessionDescription(SessionDescription.Type.OFFER, offerData.sdp)
                    handleDescription(offer)
                }

                SignalMessageType.ICE_CANDIDATE -> {
                    val candidate = jsonMapper.readValue(msg.data!!, SignalingIceCandidate::class.java)
                    val iceCandidate = IceCandidate(candidate.sdpMid, 0, candidate.candidate)
                    handleIceCandidate(iceCandidate)
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
                    pcOpts.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.NEGOTIATE
                    pcOpts.bundlePolicy = PeerConnection.BundlePolicy.BALANCED
                    pcOpts.iceServers = iceServers

                    peerConnection = EdgeWebRTCManagerInternal.peerConnectionFactory.createPeerConnection(pcOpts, this) ?: run {
                        EdgeLogger.error("PeerConnectionFactory.createPeerConnection failed. Returned peerConnection is null.")
                        onErrorCallback?.invoke(EdgeWebRTCError.ConnectionInitError())
                        throw EdgeWebRTCError.ConnectionInitError()
                    }

                    onConnectedCallback?.let { it() }
                }
            }
        }
    }

    override fun onFirstFrameRendered() {}
    override fun onFrameResolutionChanged(videoWidth: Int, videoHeight: Int, rotation: Int) {}
    override fun onSignalingChange(state: PeerConnection.SignalingState?) {
        EdgeLogger.info("Signaling state changed to: $state")
    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
        EdgeLogger.info("Connection state changed to: $state")
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
        makingOffer = true
        peerConnection.setLocalDescription(renegotiationObserver)
    }

    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
        EdgeLogger.info("ICE gathering state changed to: $state")
    }

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
