package xyz.atenalp.riverfight.android;

import fi.iki.elonen.NanoWSD;

public class User {
    final NanoWSD.WebSocket webSocket;

    public User(NanoWSD.WebSocket webSocket) {
        this.webSocket = webSocket;
    }
    //Other info about User
}
