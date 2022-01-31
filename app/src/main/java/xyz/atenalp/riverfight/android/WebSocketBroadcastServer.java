package xyz.atenalp.riverfight.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoWSD;

public class WebSocketBroadcastServer extends NanoWSD {

    private final List<WebSocket> list;

    public WebSocketBroadcastServer(int port) {
        super(port);
        list = new ArrayList<>();
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new DumbWebSocket(handshake, this);
    }

    @Override
    public void stop() {
        try {
            disconnectAll();
        } catch (Exception ex) {
            // ignore
        }
        super.stop();
    }

    void addUser(WebSocket user) {
        list.add(user);
    }

    void removeUser(WebSocket user) {
        list.remove(user);
    }

    public void broadcast(WebSocketFrame message) {
        try {
            message.setUnmasked();
            for (WebSocket ws : list) {
                ws.sendFrame(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void disconnectAll() throws IOException {
        for (WebSocket ws : list) {
            if (ws != null) {
                ws.close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "exit", false);
            }
        }
    }
}
