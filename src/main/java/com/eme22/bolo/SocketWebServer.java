package com.eme22.bolo;

import java.net.*;
import java.io.*;
import java.util.StringTokenizer;

@SuppressWarnings("InfiniteLoopStatement")
public class SocketWebServer implements Runnable{

    private final int port;

    public SocketWebServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {

        ServerSocket server_socket;
        BufferedReader input;
        String lineToBeSent;

        PrintWriter output;
        try {

            server_socket = new ServerSocket(port);
            System.out.println("Esperando cliente en el puerto " +
                    server_socket.getLocalPort());

            while(true) {
                java.net.Socket socket = server_socket.accept();
                System.out.println("Coneccion aceptada de " +
                        socket.getInetAddress() + ":" + socket.getPort());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream())); // print received data
                try {
                    while(true) {
                        String message = input.readLine();
                        if (message==null) break;
                        System.out.println("Mensaje: "+message);
                        if (message.equals("PING|")) {
                            output = new PrintWriter(socket.getOutputStream(),true); // get user input and transmit it to server

                            lineToBeSent = "OK";// stop if input line is "."
                            output.println(lineToBeSent);
                            socket.close();
                            return;
                        }

                        StringTokenizer st = new StringTokenizer(message);
                        String method = st.nextToken();

                        if (method.equals("HEAD")){
                            while ((message = input.readLine()) != null) {
                                if (message.trim().equals("")) break;
                            }
                            File file =new File(st.nextToken().substring(1));

                            //HEADERS
                            PrintStream os =  new PrintStream(socket.getOutputStream());
                            os.print("HTTP/1.0 200 OK\r\n");
                            os.print("Server: Bolo/1.0\r\n");
                            if(file.getName().contains("html"))
                                os.print("content-type: text/html\r\n");
                            os.print("content-length: "+(int) file.length());
                            os.print("\r\n");
                            os.close();
                        }
                        else if (method.equals("GET")){
                            while ((message = input.readLine()) != null) {
                                if (message.trim().equals("")) break;
                            }
                            String file = st.nextToken();

                            if (file.charAt(0) != '/') {
                                System.err.println("Saliendo: El nombre de archivo debe iniciar "
                                        + "con \"/\"");
                                return;
                            }
                            if (file.contains("../")) {
                                System.err.println("Saliendo: \"../\" no esta permitido en el nombre de archivo");
                                return;
                            }
                            sendFile(new PrintStream(socket.getOutputStream()), file);
                            socket.close();
                        }
                        else {
                            System.err.println("Solo metodo HTTP \"GET\" implementado");
                            socket.close();
                        }
                    }
                }
                catch (IOException ignore) {
                    System.out.println("Coneccion cerrada por el cliente");
                }
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }


    }

    void sendFile(PrintStream os, String file) {
        System.out.println("Solicitud de archivo \""  + file + "\"");

        if (file.equals("/")){
            sendFile(os, "/index.html");
            return;
        }

        File theFile;
        try {
            theFile = new File(file.substring(1));
            FileInputStream fis = new FileInputStream(theFile);
            byte[] theData = new byte[(int) theFile.length()];
            fis.read(theData);
            fis.close();

            //HEADERS
            os.print("HTTP/1.0 200 OK\r\n");
            os.print("Server: Bolo/1.0\r\n");
            if(theFile.getName().contains("html"))
                os.print("Content-type: text/html\r\n");
            os.print("content-length: "+(int) theFile.length());
            os.print("\r\n");
            os.write(theData);
            os.close();
        }
        catch (IOException e) {
            sendFile(os, "/404.html");
        }
    }
}
