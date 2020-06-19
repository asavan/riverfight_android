package xyz.atenalp.riverfight.android;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

public class AndroidStaticAssetsServer extends NanoHTTPD {
    private final Context context;
    private final String folderToServe;

    public AndroidStaticAssetsServer(Context context, int port, String folderToServe) throws IOException {
        super(port);
        this.context = context;
        this.folderToServe = folderToServe;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

    }

    // override here
    public String onRequest(String file) {
        return file;
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getMethod() != Method.GET) {
            return notFound();
        }
        String file = session.getUri();
        if ("/".equals(file)) {
            file += "index.html";
        }
        file = onRequest(file);
        // hello.setText(file);
        // TODO safe folder concatenate
        String fileWithFolder = folderToServe + file;
        try {
            InputStream is = context.getAssets().open(fileWithFolder);
            return newChunkedResponse(Response.Status.OK, getMimeTypeForFile(file), is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return notFound();
    }

    private static Response notFound() {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
    }
}
