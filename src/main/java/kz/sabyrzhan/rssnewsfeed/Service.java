package kz.sabyrzhan.rssnewsfeed;

import org.apache.commons.io.IOUtils;
import rawhttp.core.RawHttp;

import java.io.*;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class Service {
    public void run() throws Exception {
        var serverSocket = new ServerSocket(8080);
//        serverSocket.setSoTimeout(10000);

        var executors = Executors.newVirtualThreadPerTaskExecutor();

        while(true) {
            try {
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");
                var server = serverSocket.accept();
                String requestData = IOUtils.toString(server.getInputStream(), StandardCharsets.UTF_8);
                executors.submit(() -> {
                    try {
                        System.out.println("Just connected to " + server.getRemoteSocketAddress());
                        var rawHttp = new RawHttp();


                        var request = rawHttp.parseRequest(requestData);
                        request.writeTo(server.getOutputStream());

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            server.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
