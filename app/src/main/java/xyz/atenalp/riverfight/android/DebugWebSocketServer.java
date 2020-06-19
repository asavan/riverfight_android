package xyz.atenalp.riverfight.android;

import java.io.IOException;
import java.util.TimerTask;

import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

public class DebugWebSocketServer extends NanoWSD {

    private UserList userList;

    public DebugWebSocketServer(int port) {
        super(port);
        userList = new UserList();
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new DebugWebSocket(handshake, userList);
    }

    private static class DebugWebSocket extends WebSocket {

        private final UserList userList;
        private User user;
        private TimerTask ping = null;


        public DebugWebSocket(IHTTPSession handshakeRequest, UserList userList) {
            super(handshakeRequest);
            this.userList = userList;
        }

        @Override
        protected void onOpen() {
            user = new User(this);
            userList.addUser(user);
        }

        @Override
        protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
            userList.removeUser(user);
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            userList.sendFrameToAll(message);
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
        }

        @Override
        protected void onException(IOException exception) {
            userList.removeUser(user);
        }
    }

    @Override
    public void stop() {
        try {
            userList.disconectAll();
        } catch (Exception ex) {
            // ignore
        }
        super.stop();
    }
}
