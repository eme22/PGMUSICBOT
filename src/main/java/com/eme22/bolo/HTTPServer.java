package com.eme22.bolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("InfiniteLoopStatement")
public class HTTPServer {
    private volatile int threadCount = 0;
    final Object obj = new Object();

    public class HTTPThread extends Thread {
        private final Socket clientSocket;
        private final BufferedReader in;
        private final OutputStream out;

        HTTPThread(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            clientSocket.setSoTimeout(1000);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = clientSocket.getOutputStream();
        }

        public void listen() throws IOException {
            StringBuilder request_frame = new StringBuilder();
            String line;
            while((line = in.readLine()) != null) {
                request_frame.append(line).append("\r\n");
                if (!in.ready() && request_frame.length() > 4) {
                    byte[] outputLine;
                    HTTPProtocol proto = new HTTPProtocol(request_frame.toString());
                    outputLine = proto.processReq();
                    out.write(outputLine);
                    request_frame = new StringBuilder();
                }
            }
        }

        public void run() {
            try {
                listen();
                clientSocket.close();
                synchronized (obj) {
                    System.out.println("Hilo cerrado - " + threadCount);
                    threadCount--;
                }
            }  catch (Exception e) {
                try {
                    clientSocket.close();
                } catch (IOException f) {
                    f.printStackTrace();
                }
                synchronized (obj) {
                    System.out.println("Hilo cerrado - " + threadCount);
                    threadCount--;
                }
                e.printStackTrace();
            }
        }
    }

    public void startServer() {
        try {
            int port = 8080;
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado");
            while (true) {
                if(threadCount < 100) {
                    Socket clientSocket = serverSocket.accept();
                    HTTPThread clientThread = new HTTPThread(clientSocket);
                    synchronized (obj) {
                        threadCount++;
                    }
                    clientThread.start();
                    System.out.println("Hilo creado - " + threadCount);
                } else Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}