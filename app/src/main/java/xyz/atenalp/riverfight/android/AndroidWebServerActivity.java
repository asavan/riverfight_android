package xyz.atenalp.riverfight.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;


public class AndroidWebServerActivity extends Activity {
    private static final int PORT = 8765;
    private TextView hello;
    private AndroidStaticAssetsServer server;
    private DebugWebSocketServer webSocketServer;
    Button btn;
    String formatedIpAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        hello = (TextView) findViewById(R.id.hello);
        TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        try {
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            formatedIpAddress = android.text.format.Formatter.formatIpAddress(ipAddress);
            textIpaddr.setText("Please access! " + getHost(formatedIpAddress));
            server = new AndroidStaticAssetsServer(getApplicationContext(), PORT, "www") {
                @Override
                public String onRequest(String file) {
                    hello.setText(file);
                    return super.onRequest(file);
                }
            };
            webSocketServer = new DebugWebSocketServer(8088);
            webSocketServer.start(0);
            btn=(Button)findViewById(R.id.button1);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String redUrl = getHost(formatedIpAddress) + "?color=red";
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(redUrl)));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getHost(String formatedIpAddress) {
        return "http://" + formatedIpAddress + ":" + PORT;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
        }
        if (webSocketServer != null) {
            webSocketServer.stop();
        }
    }
}
