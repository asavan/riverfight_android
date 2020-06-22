package xyz.atenalp.riverfight.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.androidbrowserhelper.trusted.LauncherActivity;

import java.io.IOException;


public class AndroidWebServerActivity extends Activity {
    private static final int STATIC_CONTENT_PORT = 8080;
    private static final int WEB_SOCKET_PORT = 8088;
    public static final String LOCAL_IP = "127.0.0.1";
    private AndroidStaticAssetsServer server;
    private WebSocketBroadcastServer webSocketServer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Context applicationContext = getApplicationContext();
        WifiManager wifiManager = (WifiManager) applicationContext.getSystemService(WIFI_SERVICE);

        final String host = getHostOrLocal(wifiManager);
        TextView textIpAddress = (TextView) findViewById(R.id.ipaddr);
        textIpAddress.setText(getString(R.string.please, host));
        try {
            server = new AndroidStaticAssetsServer(applicationContext, STATIC_CONTENT_PORT, "www");
            if (!isHostLocal(host)) {
                webSocketServer = new WebSocketBroadcastServer(WEB_SOCKET_PORT);
                webSocketServer.start(0);
            }
            addButton(host);
            addButton2(host);
            launchTwa(host, applicationContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addButton(final String host) {
        Button btn = (Button) findViewById(R.id.button1);
        btn.setOnClickListener(v -> {
            Uri launchUri = Uri.parse(getLaunchUrl(host));
            startActivity(new Intent(Intent.ACTION_VIEW, launchUri));
        });
    }

    private void addButton2(final String host) {
        Button btn = (Button) findViewById(R.id.button2);
        btn.setOnClickListener(v -> launchTwa(host, getApplicationContext()));
    }

    private void launchTwa(String host, Context context) {
        Uri launchUri = Uri.parse(getLaunchUrl(host));
        startActivity(new Intent(Intent.ACTION_VIEW, launchUri, context, LauncherActivity.class));
    }

    private static String getLaunchUrl(String host) {
        if (isHostLocal(host)) {
            return host;
        }
        return host + "/?color=red";
    }

    private static String getHostOrLocal(WifiManager wifiManager) {
        String formattedIpAddress = getNullableHost(wifiManager);
        if (formattedIpAddress != null) return formattedIpAddress;
        return getHost(LOCAL_IP);
    }

    private static String getNullableHost(WifiManager wifiManager) {
        if (wifiManager == null) {
            return null;
        }
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo == null) {
            return null;
        }
        int ipAddress = connectionInfo.getIpAddress();
        if (ipAddress == 0) {
            return null;
        }
        String formattedIpAddress = Formatter.formatIpAddress(ipAddress);
        return getHost(formattedIpAddress);
    }

    private static boolean isHostLocal(String host) {
        return host.contains(LOCAL_IP);
    }

    private static String getHost(String ip) {
        return "http://" + ip + ":" + STATIC_CONTENT_PORT;
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
