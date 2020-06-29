package xyz.atenalp.riverfight.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.androidbrowserhelper.trusted.LauncherActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class AndroidWebServerActivity extends Activity {
    private static final int STATIC_CONTENT_PORT = 8080;
    private static final int WEB_SOCKET_PORT = 8088;
    private static final String WEB_GAME_URL = "https://atenalp.xyz";
    public static final String LOCAL_IP = "127.0.0.1";
    private AndroidStaticAssetsServer server;
    private WebSocketBroadcastServer webSocketServer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Context applicationContext = getApplicationContext();

        String formattedIpAddress = getIPAddress();
        final String host;
        if (formattedIpAddress != null) {
            host = getStaticHost(formattedIpAddress);
        } else {
            host = getStaticHost(LOCAL_IP);
        }
        String webSocketHost = getSocketHost(formattedIpAddress);
        TextView textIpAddress = (TextView) findViewById(R.id.ipaddr);
        textIpAddress.setText(getString(R.string.please, host));

        try {
            server = new AndroidStaticAssetsServer(applicationContext, STATIC_CONTENT_PORT, "www");
            if (!isHostLocal(host)) {
                webSocketServer = new WebSocketBroadcastServer(WEB_SOCKET_PORT);
                webSocketServer.start(0);
            }
            addButton(host, webSocketHost, host);
            addButton2(getStaticHost(LOCAL_IP), webSocketHost, host, applicationContext);
            addButton3(WEB_GAME_URL, webSocketHost, host, applicationContext);
            launchTwa(host, webSocketHost, host, applicationContext);
        } catch (IOException e) {
            TextView textIpAddress2 = (TextView) findViewById(R.id.ipaddr2);
            textIpAddress2.setText(Arrays.toString(e.getStackTrace()));
            Log.e("RIVER_FIGHT_TAG", "main", e);
        }
    }

    private void addButton(final String host, String socketHost, String staticHost) {
        Button btn = (Button) findViewById(R.id.button1);
        btn.setOnClickListener(v -> {
            Uri launchUri = Uri.parse(getLaunchUrl(host, socketHost, staticHost));
            startActivity(new Intent(Intent.ACTION_VIEW, launchUri));
        });
    }

    private void addButton2(final String host, String socketHost, String staticHost, Context context) {
        Button btn = (Button) findViewById(R.id.button2);
        btn.setOnClickListener(v -> launchTwa(host, socketHost, staticHost, context));
    }

    private void addButton3(final String host, String socketHost, String staticHost, Context context) {
        Button btn = (Button) findViewById(R.id.button3);
        btn.setOnClickListener(v -> launchTwa(host, socketHost, staticHost, context));
    }

    private void launchTwa(String host, String socketHost, String staticHost, Context context) {
        Uri launchUri = Uri.parse(getLaunchUrl(host, socketHost, staticHost));
        startActivity(new Intent(Intent.ACTION_VIEW, launchUri, context, LauncherActivity.class));
    }

    private String getLaunchUrl(String host, String socketHost, String staticHost) {
        StringBuilder b = new StringBuilder();
        b.append(host);
        b.append("/?color=red");
        try {
            if (socketHost != null) {
                b.append("&wh=");
                b.append(URLEncoder.encode(socketHost, StandardCharsets.UTF_8.toString()));
            }

            if (staticHost != null) {
                b.append("&sh=");
                b.append(URLEncoder.encode(staticHost, StandardCharsets.UTF_8.toString()));
            }
        } catch (Exception e) {
            Log.e("RIVER_FIGHT_TAG", "getLaunchUrl", e);
        }
        TextView textIpAddress2 = (TextView) findViewById(R.id.ipaddr2);
        textIpAddress2.setText(b.toString());
        return b.toString();
    }


    private static boolean isHostLocal(String host) {
        return host.contains(LOCAL_IP);
    }


    public static String getIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface interface_ : interfaces) {
                for (InetAddress inetAddress : Collections.list(interface_.getInetAddresses())) {
                    if (inetAddress.isLoopbackAddress()) {
                        continue;
                    }

                    String ipAddr = inetAddress.getHostAddress();
                    boolean isIPv4 = ipAddr.indexOf(':') < 0;
                    if (!isIPv4) {
                        continue;
                    }
                    return ipAddr;
                }

            }
        } catch (Exception e) {
            Log.e("RIVER_FIGHT_TAG", "getIPAddress", e);
        }
        return null;
    }

    private static String getStaticHost(String ip) {
        return "http://" + ip + ":" + STATIC_CONTENT_PORT;
    }

    private static String getSocketHost(String ip) {
        return ip + ":" + WEB_SOCKET_PORT;
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
