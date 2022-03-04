package com.eme22.bolo.utils;

import io.undertow.io.Sender;
import io.undertow.server.DefaultResponseListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.Headers;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Set;

public class ErrorPageHandler implements HttpHandler {

    private final HttpHandler next;

    public ErrorPageHandler(final HttpHandler next) {
        this.next = next;
    }

    public ErrorPageHandler() {
        this.next = ResponseCodeHandler.HANDLE_404;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.addDefaultResponseListener(exchange1 -> {
            if (!exchange1.isResponseChannelAvailable()) {
                return false;
            }
            if (exchange1.getResponseCode() == 404) {
                final File errorPage = new File("404.html");
                exchange1.getResponseHeaders().put(Headers.CONTENT_LENGTH, "" + errorPage.length());
                exchange1.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
                Sender sender = exchange1.getResponseSender();

                byte[] bytes;
                try {
                    bytes = Files.readAllBytes(errorPage.toPath());
                } catch (IOException e) {
                    return false;
                }
                sender.send(ByteBuffer.wrap(bytes));
                return true;
            }
            return false;
        });
        next.handleRequest(exchange);
    }
}