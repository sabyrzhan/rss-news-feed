package kz.sabyrzhan.rssnewsfeed;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Service {
    private RawHttp rawHttp = new RawHttp();

    static class ConnectionService implements Runnable {

        Socket socket;
        ServerSocket serverSocket;

        ConnectionService(ServerSocket serverSocket,Socket server) {
            this.serverSocket = serverSocket;
            this.socket = server;
        }


        @Override
        public void run() {
            try {
                var rawHttp = new RawHttp();
                int cr_count = 0;
                var text = "sample";
                byte[] tb = text.getBytes(StandardCharsets.UTF_8);
                while(true) {
                    int i=0;
                    int r = socket.getInputStream().read();
                    if(r == -1) break;

                    char[] buffer = new char[256];

                    while( r != -1 ){
                        char c = (char)r;
                        if( c == '\n' ){
                            cr_count++;
                        } else if( c != '\r' ){
                            cr_count = 0;
                        }
                        buffer[i++] = c;
                        if(cr_count == 2) break;
                        r = socket.getInputStream().read();
                    }
                    System.out.println("request: " + new String(buffer) + " and cr_count: " + cr_count);
                    var response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Content-Length: " + tb.length + "\r\n\r\n";
                    socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
                    socket.getOutputStream().write(tb);
                    socket.getOutputStream().flush();
                }
            } catch (Exception e) {
                System.out.println("Executor error:" + e.toString());
                e.printStackTrace();
            } finally {
                try {
                    System.out.println("Closing server");
                    socket.close();
                } catch (Exception e) {
                    System.out.println("Executor error2: ");
                    e.printStackTrace();
                }
            }
        }

    }

    public void run() throws Exception {
        var serverSocket = new ServerSocket(8080);
//        serverSocket.setSoTimeout(0);
//        serverSocket.setReuseAddress(false);
//        serverSocket.setSoTimeout(10000);

        var executors = Executors.newFixedThreadPool(4);


        if (true) {
            while(true) {
                Socket server = serverSocket.accept();

                executors.submit(new ConnectionService(serverSocket, server));
                if (true) {
                    continue;
                }
            }
        }


        // Run server socket handler in separate thread so accept doesn't block stop
        Thread serverSocketHandler = new Thread(new Runnable() {
            @Override
            public void run() {

                int clientNumber = 1;
                while(true) {
                    try {
                        final Socket clientSocket = serverSocket.accept();
                        executors.execute(() -> {
                            try {
                                System.out.println("Getting empty request");
//                                var header = "HTTP/1.1 200 OK\r\n" +
//                                        "Content-Type: text/plain\r\n\r\n";
//                                var body = "sample";
//                                var pw = new PrintWriter(new BufferedOutputStream(clientSocket.getOutputStream()));
//                                pw.write(header);
//                                pw.write(body);
//                                pw.flush();
//                                pw.close();
                                clientSocket.getInputStream().close();
                                while(!clientSocket.isOutputShutdown() || !clientSocket.isInputShutdown()) {
                                    System.out.println("Not shutdown");
                                }

                                clientSocket.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        serverSocketHandler.start();

        if (true) {
            return;
        }


        while(true) {
            try {
                var server = serverSocket.accept();

                if (true) {
                    var text = "sample";
                    System.out.println("Getting empty request");
                    var response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain\r\n\r\n"
//                            "Content-Length: " + text.length() + "\r\n\r\n"
                            + text;
                    var pw = new PrintWriter(server.getOutputStream());
                    pw.write(response);
                    pw.close();
                    server.close();
                    continue;
                }

                executors.submit(() -> {
                    try {
                        System.out.println("Waiting for client on port " +
                                serverSocket.getLocalPort() + "...");

                        if (true) {
                            var text = "sample";
                            System.out.println("Getting empty request");
                            var response = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: " + text.length() + "\r\n\r\n"
                                    + text;
                            var pw = new PrintWriter(server.getOutputStream());
                            pw.write(response);
                            pw.close();
                            server.close();
                            return;
                        }


                        var request = readRequestData(server.getInputStream());
                        System.out.println("Data is: " + request);
                        if (request == null) {
                            System.out.println("Getting empty request");
                            var response = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: 0\r\n";
                            rawHttp.parseResponse(response).writeTo(server.getOutputStream());
                        } else {
                            System.out.println("Getting normal request");
                            var response = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: 9\r\n" +
                                    "\r\n" +
                                    "something";
                            rawHttp.parseResponse(response).writeTo(server.getOutputStream());
                        }
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

    private RawHttpRequest readRequestData(InputStream socketInputStream) {
        RawHttpRequest request = null;
        try {
            request = rawHttp.parseRequest(socketInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return request;
    }
}
