package kz.sabyrzhan.rssnewsfeed;

import kz.sabyrzhan.rssnewsfeed.exception.ApiException;
import kz.sabyrzhan.rssnewsfeed.servlets.Register;
import kz.sabyrzhan.rssnewsfeed.servlets.handlers.Request;
import kz.sabyrzhan.rssnewsfeed.servlets.handlers.Response;
import rawhttp.core.*;
import rawhttp.core.errors.InvalidHttpRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.concurrent.Executors;

import static kz.sabyrzhan.rssnewsfeed.servlets.handlers.Response.EMPTY_RESPONSE;

public class Service {
    public void run() throws Exception {
        var serverSocket = new ServerSocket(8080);

        var executors = Executors.newFixedThreadPool(4);
        var http = new RawHttp();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        while(true) {
            var client = serverSocket.accept();
            executors.submit(() -> {
                RawHttpRequest request;
                boolean serverWillCloseConnection = false;

                while (!serverWillCloseConnection) {
                    try {
                        if (serverSocket.isClosed()) {
                            client.close();
                            break;
                        }
                        request = http.parseRequest(
                                client.getInputStream(),
                                ((InetSocketAddress) client.getRemoteSocketAddress()).getAddress());
                        HttpVersion httpVersion = request.getStartLine().getHttpVersion();
                        Optional<String> connectionOption = request.getHeaders().getFirst("Connection");

                        // If the "close" connection option is present, the connection will
                        // not persist after the current response
                        serverWillCloseConnection = connectionOption
                                .map("close"::equalsIgnoreCase)
                                .orElse(false);

                        RawHttpResponse<?> response = null;
                        boolean expects100 = request.expectContinue();

                        if (expects100 && !request.getStartLine().getHttpVersion().isOlderThan(HttpVersion.HTTP_1_1)) {
                            RawHttpResponse<Void> interimResponse = new EagerHttpResponse<>(null, null,
                                    new StatusLine(HttpVersion.HTTP_1_1, 100, "Continue"), RawHttpHeaders.empty(), null);
                            if (interimResponse.getStatusCode() == 100) {
                                // tell the client that we shall continue
                                interimResponse.writeTo(client.getOutputStream());
                            } else {
                                // if we don't accept the request body, we must close the connection
                                serverWillCloseConnection = true;
                                response = interimResponse;
                            }
                        }

                        if (!serverWillCloseConnection) {
                            // https://tools.ietf.org/html/rfc7230#section-6.3
                            // If the received protocol is HTTP/1.1 (or later)
                            // OR
                            // If the received protocol is HTTP/1.0, the "keep-alive" connection
                            // option is present
                            // THEN the connection will persist
                            // OTHERWISE close the connection
                            boolean serverShouldPersistConnection =
                                    !httpVersion.isOlderThan(HttpVersion.HTTP_1_1)
                                            || (httpVersion == HttpVersion.HTTP_1_0 && connectionOption
                                            .map("keep-alive"::equalsIgnoreCase)
                                            .orElse(false));
                            serverWillCloseConnection = !serverShouldPersistConnection;
                        }

                        try {
                            if (response == null) {
                                record Error(String error) {};
                                record Success(String message) {};

                                var method = Register.HttpMethod.fromString(request.getMethod());
                                if (method == null) {
                                    var error = new Error("Method not supported");
                                    response = Response.buildResponse(405, error);
                                } else {
                                    var handler = Register.getHandler(method, request.getUri().getPath());
                                    if (handler == null) {
                                        response = Response.buildResponse(404, "Not found");
                                    } else {
                                        try {
                                            var requestWrapper = new Request(request);
                                            var result = handler.handle(requestWrapper);
                                            if (result == (Object) EMPTY_RESPONSE) {
                                                response = Response.buildResponse(200, new Success("success"));
                                            } else {
                                                response = Response.buildResponse(200, result);
                                            }
                                        } catch (ApiException e) {
                                            response = Response.buildResponse(500, new Error(e.getMessage()));
                                        }
                                    }
                                }
                            }
                            serverWillCloseConnection |= RawHttpResponse.shouldCloseConnectionAfter(response);
                            response.writeTo(client.getOutputStream());
                        } finally {
                            closeBodyOf(response);
                        }
                    } catch (SocketTimeoutException e) {
                        serverWillCloseConnection = true;
                    } catch (Exception e) {
                        if (!(e instanceof SocketException)) {
                            // only print stack trace if this is not due to a client closing the connection
                            boolean clientClosedConnection = e instanceof InvalidHttpRequest &&
                                    ((InvalidHttpRequest) e).getLineNumber() == 0;

                            if (!clientClosedConnection) {
                                e.printStackTrace();
                            }
                        }

                        serverWillCloseConnection = true; // cannot keep listening anymore
                    } finally {
                        if (serverWillCloseConnection) {
                            try {
                                client.close();
                            } catch (IOException e) {
                                // not a problem
                            }
                        }
                    }
                }
            });
        }
    }

    private static void closeBodyOf(RawHttpResponse<?> response) {
        if (response != null) {
            response.getBody().ifPresent(b -> {
                try {
                    b.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
