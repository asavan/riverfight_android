package xyz.atenalp.riverfight.android;

import android.os.Bundle;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;


public class AndroidWebServerActivity extends AppCompatActivity {
    private static final int STATIC_CONTENT_PORT = 8080;
    private static final int WEB_SOCKET_PORT = 8088;
    public static final String WEB_VIEW_URL = "file:///android_asset/www/index.html";
    private static final boolean secure = false;
    private BtnUtils btnUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btnUtils = new BtnUtils(this, STATIC_CONTENT_PORT, WEB_SOCKET_PORT, secure);

        try {
            final String formattedIpAddress = IpUtils.getIPAddressSafe();
            addButtons(formattedIpAddress);

            HostUtils hostUtils = new HostUtils(STATIC_CONTENT_PORT, WEB_SOCKET_PORT, secure);
            final String host = hostUtils.getStaticHost(formattedIpAddress);
            final String webSocketHost = hostUtils.getSocketHost(formattedIpAddress);

            Map<String, String> mainParams = new LinkedHashMap<>();
            mainParams.put("color", "red");
            mainParams.put("wh", webSocketHost);
            mainParams.put("sh", host);
            mainParams.put("currentMode", "net");
            // mainParams.put("useSound", "1");
            btnUtils.launchTwa(hostUtils.getStaticHost(IpUtils.LOCAL_IP), mainParams);

        } catch (Exception e) {
            Log.e("RIVER_FIGHT_TAG", "main", e);
        }
    }

    private void addButtons(String formattedIpAddress) {

        HostUtils hostUtils = new HostUtils(STATIC_CONTENT_PORT, WEB_SOCKET_PORT, secure);
        final String host = hostUtils.getStaticHost(formattedIpAddress);
        final String webSocketHost = hostUtils.getSocketHost(formattedIpAddress);


        Map<String, String> mainParams = new LinkedHashMap<>();
        mainParams.put("color", "red");
        mainParams.put("wh", webSocketHost);
        mainParams.put("sh", host);
        mainParams.put("useSound", "1");
        mainParams.put("currentMode", "net");

        {
            btnUtils.addButtonBrowser(host, mainParams, R.id.button1);
            btnUtils.addButtonTwa(host, mainParams, R.id.button4, host);
            btnUtils.addButtonWebView(WEB_VIEW_URL, mainParams, R.id.webviewb);
        }

        {
            Map<String, String> b = new LinkedHashMap<>();
            b.put("wh", webSocketHost);
            b.put("sh", host);
            b.put("useSound", "1");
            btnUtils.addButtonTwa(host, b, R.id.button5);
        }

        {
            Map<String, String> b = new LinkedHashMap<>();
            b.put("currentMode", "server");
            b.put("wh", webSocketHost);
            b.put("sh", host);
            btnUtils.addButtonTwa(host, b, R.id.button6);
        }

        {
            Map<String, String> b = new LinkedHashMap<>();
            b.put("currentMode", "ai");
            btnUtils.addButtonWebView(WEB_VIEW_URL, b, R.id.button7);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (btnUtils != null) {
            btnUtils.onDestroy();
        }
    }
}
