package com.example.webrtc_demo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.webrtc_demo.databinding.FragmentFirstBinding
import com.nabto.edge.client.Connection
import com.nabto.edge.client.ConnectionEventsCallback
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.client.webrtc.*
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var conn: Connection
    private lateinit var pc: EdgeWebrtcConnection
    private lateinit var remoteTrack: EdgeVideoTrack
    private lateinit var remoteAudioTrack: EdgeAudioTrack

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    suspend fun onConnected() {
        Log.i("TestApp", "Logged in")
        pc = EdgeWebrtcManager.getInstance().createRTCConnection(conn)

        pc.onConnected {
            val coap = conn.createCoap("POST", "/webrtc/from_device")
            coap.execute()
            Log.i("TestApp", "Coap response: ${coap.responseStatusCode}")
            if (coap.responseStatusCode != 201) {
                Log.i("TestApp", "Failed to get video feed with status ${coap.responseStatusCode}")
            }
        }

        pc.onTrack { track ->
            Log.i("TestApp", "Track of type ${track.type}")
            if (track.type == EdgeMediaTrackType.VIDEO) {
                remoteTrack = track as EdgeVideoTrack
                remoteTrack.add(binding.videoView)
            }

            if (track.type == EdgeMediaTrackType.AUDIO) {
                remoteAudioTrack = track as EdgeAudioTrack
                remoteAudioTrack.setEnabled(true)
            }
        }

        pc.connect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EdgeWebrtcManager.getInstance().initVideoView(binding.videoView)
        val client = NabtoClient.create(requireActivity())
        conn = client.createConnection()

        val opts = JSONObject()
        opts.put("ProductId", "pr-4fiowoh4")
        opts.put("DeviceId", "de-bgdqxtqs")
        opts.put("PrivateKey", client.createPrivateKey())
        opts.put("ServerConnectToken", "demosct")

        conn.updateOptions(opts.toString())
        conn.addConnectionEventsListener(object : ConnectionEventsCallback() {
            override fun onEvent(event: Int) {
                if (event == CONNECTED) {
                    lifecycleScope.launch {
                        onConnected()
                    }
                }
            }

        })
        conn.connect()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}