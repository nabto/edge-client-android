package com.nabto.edge.client.helloworld

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.nabto.edge.client.Connection
import com.nabto.edge.client.NabtoClient
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val helloworldButton = findViewById<Button>(R.id.helloworld_button);
        helloworldButton.setOnClickListener {
            val client : NabtoClient = NabtoClient.create(this);
            val connection : Connection = client.createConnection();
            val options = JSONObject()
            options.put("ProductId", getResources().getString(R.string.product_id));
            options.put("DeviceId", getResources().getString(R.string.device_id));
            options.put("ServerKey", getResources().getString(R.string.server_key));
            options.put("PrivateKey", client.createPrivateKey());
            connection.updateOptions(options.toString());

            connection.connect();
            val coap = connection.createCoap("GET", "/hello-world");
            val result = coap.execute();
            val status = coap.responseStatusCode;
            if (status == 205) {
                val helloworldText = findViewById<TextView>(R.id.output);
                val text = String(coap.responsePayload, StandardCharsets.UTF_8);
                helloworldText.setText(text);
            }
        }
    }
}