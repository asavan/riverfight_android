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
    private static final int STATIC_CONTENT_PORT = 8080;
    private static final int WEB_SOCKET_PORT = 8088;
    private AndroidStaticAssetsServer server;
    private WebSocketBroadcastServer webSocketServer;
    private int ipAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final TextView hello = (TextView) findViewById(R.id.hello);
        TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        try {
            ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            textIpaddr.setText(getString(R.string.please) + getHost(ipAddress));
            server = new AndroidStaticAssetsServer(getApplicationContext(), STATIC_CONTENT_PORT, "www") {
                @Override
                public String onRequest(String file) {
                    hello.setText(file);
                    return super.onRequest(file);
                }
            };
            webSocketServer = new WebSocketBroadcastServer(WEB_SOCKET_PORT);
            webSocketServer.start(0);
            Button btn = (Button) findViewById(R.id.button1);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String redUrl = getHost(ipAddress) + "?color=red";
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(redUrl)));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getHost(int ipAddress) {
        String formatedIpAddress = android.text.format.Formatter.formatIpAddress(ipAddress);
        return "http://" + formatedIpAddress + ":" + STATIC_CONTENT_PORT;
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
