package com.nabto.edge.client.helloworld

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.nabto.edge.client.Connection
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.client.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {
    var connection : Connection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val helloworldButton = findViewById<Button>(R.id.helloworld_button)
        helloworldButton.setOnClickListener {
            val helloWorldText = findViewById<TextView>(R.id.output)
            helloWorldText.text = "Waiting for response..."
            lifecycleScope.launch {
                val client : NabtoClient = NabtoClient.create(this@MainActivity)
                connection = client.createConnection()

                val options = JSONObject()
                options.put("ProductId", resources.getString(R.string.product_id))
                options.put("DeviceId", resources.getString(R.string.device_id))
                options.put("ServerKey", resources.getString(R.string.server_key))
                options.put("PrivateKey", client.createPrivateKey())
                connection?.updateOptions(options.toString())

                connection?.connectAsync()
                val coap = connection?.createCoap("GET", "/hello-world")
                coap?.executeAsync()
                val status = coap?.responseStatusCode

                if (status == 205) {
                    val text = String(coap.responsePayload, StandardCharsets.UTF_8)
                    helloWorldText.text = text
                }
                else {
                    helloWorldText.text = "Failed"
                }
            }
        }
    }
}
