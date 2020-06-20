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
            disconectAll();
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

    public void sendToAll(String str) {
        for (WebSocket ws : list) {
            if (ws != null) {
                try {
                    ws.send(str);
                } catch (IOException e) {
                    System.out.println("sending error.....");
                    try {
                        ws.close(WebSocketFrame.CloseCode.InvalidFramePayloadData, "reqrement", false);
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            }
        }
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

    public void disconectAll() {
        for (WebSocket ws : list) {
            if (ws != null) {
                try {
                    ws.close(NanoWSD.WebSocketFrame.CloseCode.InvalidFramePayloadData, "reqrement", false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
