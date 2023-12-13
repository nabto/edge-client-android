package com.nabto.edge.client.webrtc

import org.webrtc.AudioTrack

class EdgeAudioTrackImpl(val track: AudioTrack) : EdgeAudioTrack {
    override val type: EdgeMediaTrackType
        get() = EdgeMediaTrackType.AUDIO
}
