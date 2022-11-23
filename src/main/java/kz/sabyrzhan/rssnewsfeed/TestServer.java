package kz.sabyrzhan.rssnewsfeed;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class TestServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8080);
        var executors = Executors.newVirtualThreadPerTaskExecutor();
        while(true) {
            try {
                Socket server = serverSocket.accept();

                executors.submit(new ConnectionService(serverSocket, server));
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        serverSocket.close();
    }


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
                var text = "sample";
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");

                System.out.println("Getting empty request");
                var response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + text.length() + "\r\n\r\n"
                        + text;
                socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
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
}