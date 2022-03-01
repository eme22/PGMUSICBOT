package webserver;

import org.jetbrains.annotations.NotNull;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class NotFoundHandler implements HttpHandler {

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {

        response.content(get404())
                .status(404)
                .end();
    }

    @NotNull
    private byte[] get404() throws IOException {
        File file = new File("404.html");
        FileInputStream fl = new FileInputStream(file);
        byte[] arr = new byte[(int) file.length()];
        fl.read(arr);
        fl.close();
        return arr;
    }
}
