package xyz.atenalp.riverfight.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.androidbrowserhelper.trusted.TwaLauncher;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;


public class AndroidWebServerActivity extends AppCompatActivity {
    private static final int STATIC_CONTENT_PORT = 8080;
    private static final int WEB_SOCKET_PORT = 8088;
    private static final String WEB_GAME_URL = "https://riverfight.ml";
    public static final String LOCAL_IP = "127.0.0.1";
    public static final String LOCALHOST = "localhost";
    public static final String WEB_VIEW_URL = "file:///android_asset/www/index.html";
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

        try {
            server = new AndroidStaticAssetsServer(applicationContext, STATIC_CONTENT_PORT, "www");
            if (!isHostLocal(host)) {
                webSocketServer = new WebSocketBroadcastServer(WEB_SOCKET_PORT);
                webSocketServer.start(0);
            }

            addButtons(host, webSocketHost);

            Map<String, String> mainParams = new LinkedHashMap<>();
            mainParams.put("color", "red");
            mainParams.put("wh", webSocketHost);
            mainParams.put("sh", host);
            // mainParams.put("useSound", "1");
            launchTwa(getStaticHost(LOCAL_IP), mainParams);

            // launchWebView(WEB_VIEW_URL, mainParams);
            // launchWebView(getStaticHost(LOCAL_IP), mainParams);
        } catch (Exception e) {
            Log.e("RIVER_FIGHT_TAG", "main", e);
        }
    }

    private void addButtons(String host, String webSocketHost) {
        Map<String, String> mainParams = new LinkedHashMap<>();
        mainParams.put("color", "red");
        mainParams.put("wh", webSocketHost);
        mainParams.put("sh", host);
        mainParams.put("useSound", "1");

        {
            addButton(host, mainParams, R.id.button1);
            addButtonTwa(WEB_GAME_URL, mainParams, R.id.button3);
            addButtonTwa(host, mainParams, R.id.button4, host);
            addButtonWebView(WEB_VIEW_URL, mainParams, R.id.webviewb);
        }

        {
            Map<String, String> b = new LinkedHashMap<>();
            b.put("wh", webSocketHost);
            b.put("sh", host);
            b.put("useSound", "1");
            addButtonTwa(host, b, R.id.button5);
        }

        {
            Map<String, String> b = new LinkedHashMap<>();
            b.put("currentMode", "server");
            b.put("wh", webSocketHost);
            b.put("sh", host);
            addButtonTwa(host, b, R.id.button6);
        }

        {
            Map<String, String> b = new LinkedHashMap<>();
            b.put("currentMode", "ai");
            addButtonWebView(WEB_VIEW_URL, b, R.id.button7);
        }
    }

    private void addButton(final String host, Map<String, String> parameters, int id) {
        Button btn = findViewById(id);
        btn.setOnClickListener(v -> {
            Uri launchUri = Uri.parse(getLaunchUrl(host, parameters));
            startActivity(new Intent(Intent.ACTION_VIEW, launchUri));
        });
    }

    private void addButtonWebView(final String host, Map<String, String> parameters, int id) {
        Button btn = findViewById(id);
        btn.setOnClickListener(v -> {
            launchWebView(host, parameters);
        });
    }

    private void launchWebView(String host, Map<String, String> parameters) {
        Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
        String launchUrl = getLaunchUrl(host, parameters);
        Log.i("RIVER_FIGHT_TAG", launchUrl);
        intent.putExtra("url", launchUrl);
        startActivity(intent);
    }

    private void addButtonTwa(String host, Map<String, String> parameters, int id) {
        addButtonTwa(host, parameters, id, null);
    }

    private void addButtonTwa(String host, Map<String, String> parameters, int id, String text) {
        Button btn = findViewById(id);
        if (text != null) {
            btn.setText(text);
        }
        btn.setOnClickListener(v -> launchTwa(host, parameters));
    }

    private void launchTwa(String host, Map<String, String> parameters) {
        Uri launchUri = Uri.parse(getLaunchUrl(host, parameters));
        TwaLauncher launcher = new TwaLauncher(this);
        launcher.launch(launchUri);
        // startActivity(new Intent(Intent.ACTION_VIEW, launchUri, context, LauncherActivity.class));
    }

    private static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static String mapToParamString(Map<String, String> parameters) {
        StringBuilder acc = new StringBuilder();
        boolean firstElem = true;
        for (Map.Entry<String, String> p : parameters.entrySet()) {
            if (!firstElem) {
                acc.append("&");
            }
            firstElem = false;
            acc.append(p.getKey()).append("=").append(urlEncodeUTF8(p.getValue()));
        }
        return acc.toString();
    }

    private static String getLaunchUrl(String host, Map<String, String> parameters) {
        StringBuilder b = new StringBuilder();
        b.append(host);
//        if (!host.endsWith("/") && parameters != null) {
//            b.append("/");
//        }
        if (parameters != null) {
            b.append("?").append(mapToParamString(parameters));
        }
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
