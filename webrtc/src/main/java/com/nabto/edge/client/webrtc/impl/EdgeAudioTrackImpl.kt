package com.nabto.edge.client.webrtc.impl

import com.nabto.edge.client.webrtc.EdgeAudioTrack
import com.nabto.edge.client.webrtc.EdgeMediaTrackType
import org.webrtc.AudioTrack

class EdgeAudioTrackImpl(val track: AudioTrack) : EdgeAudioTrack {
    override val type: EdgeMediaTrackType
        get() = EdgeMediaTrackType.AUDIO

    override fun setEnabled(enabled: Boolean) {
        track.setEnabled(enabled)
    }

    override fun setVolume(volume: Double) {
        track.setVolume(volume)
    }
}
