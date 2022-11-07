package kz.sabyrzhan.rssnewsfeed;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import rawhttp.core.RawHttp;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Service {
    public void run() throws Exception {
        var serverSocket = new ServerSocket(8080);
//        serverSocket.setSoTimeout(10000);

        var executors = Executors.newVirtualThreadPerTaskExecutor();

        while(true) {
            try {
                var server = serverSocket.accept();

                executors.submit(() -> {
                    try {
                        System.out.println("Waiting for client on port " +
                                serverSocket.getLocalPort() + "...");
                        String data = readRequestData(server.getInputStream());
                        System.out.println("Data is: " + data);
                        //var data = IOUtils.toString(new InputStreamReader(server.getInputStream()));
                        System.out.println("Data: " + data);
                        var response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: 9\r\n" +
                                "\r\n" +
                                "something";
                        var out = new BufferedWriter(
                                new OutputStreamWriter(
                                        new BufferedOutputStream(server.getOutputStream()), "UTF-8")
                        );
                        out.write(response);
                        out.flush();
                        out.close();
                        server.close();
                        System.out.println("Closed");
                    } catch (Exception e) {
                        System.out.println("Executor error:" + e.toString());
                        e.printStackTrace();
                    } finally {
                        try {
                            System.out.println("Closing server");
                            server.close();
                        } catch (Exception e) {
                            System.out.println("Executor error2: ");
                            e.printStackTrace();
                        }
                    }
                });
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                System.out.println("Error");
                e.printStackTrace();
                break;
            }
        }

        serverSocket.close();
    }

    private String readRequestData(InputStream socketInputStream) {
        String result = "";
        try {
            var br = new BufferedReader(new InputStreamReader(socketInputStream));
            String data = null;
            int line = 1;
            int emptyCounter = 0;
            while ((data = br.readLine()) != null) {
                System.out.println((line++) + ") " + data);
                result += data;
                // this seems to be the key for me!
                // somehow I never get out of this loop if I don't
                // check for an empty line...
                if (data.isEmpty()) {
                    if (emptyCounter >= 1) {
                        System.out.println("Emptycounter: " + emptyCounter + ". Finishing");
                        break;
                    } else {
                        emptyCounter++;
                        System.out.println("Empty: " + emptyCounter);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
