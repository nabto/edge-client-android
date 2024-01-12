package com.example.webrtc_demo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.webrtc_demo.databinding.FragmentFirstBinding
import com.nabto.edge.client.Connection
import com.nabto.edge.client.ConnectionEventsCallback
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.client.webrtc.*
import com.nabto.edge.iamutil.IamUtil
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    suspend fun onConnected() {
        val iam = IamUtil.create()
        val modes = iam.getAvailablePairingModes(conn)
        Log.i("TestApp", modes.joinToString())
        if (!iam.isCurrentUserPaired(conn)) {
            iam.pairPasswordOpen(conn, "eln", "VAykyzWk74TU")
        }

        Log.i("TestApp", "Logged in")
        pc = EdgeWebRTC.create(conn, requireActivity())

        pc.onConnected {
            Log.i("TestApp", "Connected to peer!")
            val coap = conn.createCoap("GET", "/webrtc/video/frontdoor")
            coap.execute()
            Log.i("TestApp", "Coap response: ${coap.responseStatusCode}")
            if (coap.responseStatusCode != 201) {
                Log.i("TestApp", "Failed to get video feed with status ${coap.responseStatusCode}")
            }
        }

        pc.onTrack { track ->
            if (track.type == EdgeMediaTrackType.VIDEO) {
                remoteTrack = track as EdgeVideoTrack
                remoteTrack.add(binding.videoView)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EdgeWebRTC.initVideoView(binding.videoView)
        val client = NabtoClient.create(requireActivity())
        conn = client.createConnection()

        val key = """
<client private key>
            """.trimIndent()

        val opts = JSONObject()
        opts.put("ProductId", "<product id>")
        opts.put("DeviceId", "<device id>")
        opts.put("PrivateKey", key)
        opts.put("ServerConnectToken", "<sct>")

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