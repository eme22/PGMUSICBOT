package com.eme22.bolo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HTTPProtocol {

    private String path;

    private byte[] fileBytes;
    private String contentType;
    private byte[] successRes;
    private byte[] errorRes;

    HTTPProtocol(String requestString) {
        String[] requestLines = requestString.split("\r\n");
        String[] reqLine = requestLines[0].split(" ");

        String method = reqLine[0];
        path = reqLine[1];
        String version = reqLine[2];
        System.out.println("Method - " + method + " Path - " + path + " Version - " + version);
        for(int h = 2; h < requestLines.length; h++) {
            String header = requestLines[h];
            List<String> headers = new ArrayList<>();
            headers.add(header);
        }
    }

    private boolean findFile() throws IOException {
        if("/".equals(path))
            path = "/index.html";
        String directory = ".";
        Path filePath = Paths.get(directory, path);
        if(Files.exists(filePath)) {
            contentType = Files.probeContentType(filePath);
            fileBytes = Files.readAllBytes(filePath);
            return true;
        }
        return false;
    }

    private void successResponse() {
        String res = "";
        res += "HTTP/1.1 200 OK\r\n";
        final Date currentTime = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        res += "Date: " + sdf.format(currentTime) + "\r\n";
        res += "Content-Type: " + contentType + "\r\n";
        res += "Content-Length: " + fileBytes.length + "\r\n";
        res += "Connection: keep-alive\r\n";
        res += "Keep-Alive: timeout=5, max=1000\r\n";
        res += "\r\n";
        successRes = res.getBytes();
    }

    private void errorResponse() {
        String res = "";
        res += "HTTP/1.1 404 Not Found\r\n";
        res += "Content-Type: text/html\r\n\r\n";
        errorRes = (res + "<h1>404: Not found</h1>\r\n\r\n").getBytes();
    }

    public byte[] processReq() {
        try {
            if(findFile()) {
                successResponse();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                outStream.write(successRes);
                outStream.write(fileBytes);
                outStream.write("\r\n\r\n".getBytes());
                return outStream.toByteArray();
            } else {
                errorResponse();
                return errorRes;
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorResponse();
            return errorRes;
        }
    }
}
