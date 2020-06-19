package xyz.atenalp.riverfight.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoWSD;

public class UserList {
    List<User> list;

    public UserList() {
        list = new ArrayList<>();
    }

    public void addUser(User user) {
        list.add(user);
    }

    public void removeUser(User user) {
        list.remove(user);
    }

    public void sendToAll(String str) {
        for (User user :list) {
            NanoWSD.WebSocket ws = user.webSocket;
            if (ws != null) {
                try {
                    ws.send(str);
                } catch (IOException e) {
                    System.out.println("sending error.....");
                    try {
                        ws.close(NanoWSD.WebSocketFrame.CloseCode.InvalidFramePayloadData, "reqrement", false);
                    } catch (IOException e1) {
                        // removeUser(user);
                    }
                }
            }
        }
    }

    public void sendFrameToAll(NanoWSD.WebSocketFrame message) {
        try {
            message.setUnmasked();
            for (User user : list) {
                user.webSocket.sendFrame(message);
            }
        } catch (IOException e) {
            // throw new RuntimeException(e);
        }

    }

    public void disconectAll() {
        for (User user : list) {
            NanoWSD.WebSocket ws = user.webSocket;
            if (ws != null) {
                try {
                    ws.close(NanoWSD.WebSocketFrame.CloseCode.InvalidFramePayloadData, "reqrement", false);
                } catch (IOException e) {
                    // removeUser(user);
                }
            }
        }
    }
}
