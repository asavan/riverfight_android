package xyz.atenalp.riverfight.android;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;


public class AndroidWebServerActivity extends Activity {
    private static final int PORT = 8765;
    private TextView hello;
    private AndroidStaticAssetsServer server;
    private DebugWebSocketServer webSocketServer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        hello = (TextView) findViewById(R.id.hello);
        TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        try {
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            String formatedIpAddress = android.text.format.Formatter.formatIpAddress(ipAddress);
            textIpaddr.setText("Please access! http://" + formatedIpAddress + ":" + PORT);
            server = new AndroidStaticAssetsServer(getApplicationContext(), PORT, "www") {
                @Override
                public String onRequest(String file) {
                    hello.setText(file);
                    return super.onRequest(file);
                }
            };
            webSocketServer = new DebugWebSocketServer(8088);
            webSocketServer.start(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
