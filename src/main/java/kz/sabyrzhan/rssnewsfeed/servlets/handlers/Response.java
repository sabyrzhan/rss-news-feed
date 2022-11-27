package kz.sabyrzhan.rssnewsfeed.servlets.handlers;

import com.google.gson.Gson;
import rawhttp.core.HttpVersion;
import rawhttp.core.RawHttpHeaders;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.StatusLine;
import rawhttp.core.body.FramedBody;
import rawhttp.core.body.LazyBodyReader;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class Response {
    public static final int EMPTY_RESPONSE = 0;

    public static RawHttpResponse buildResponse(int status, Object responseObject) {
        StatusLine statusLine = switch (status) {
            case 200 -> new StatusLine(HttpVersion.HTTP_1_1, 200, "OK");
            default -> new StatusLine(HttpVersion.HTTP_1_1, 404, "Not Found");
        };

        var gson = new Gson();
        var responseString = gson.toJson(responseObject);
        var httpHeaders = RawHttpHeaders.newBuilder()
                .with("Content-Type", "application/json; charset=utf-8")
                .with("Content-Length", String.valueOf(responseString.length()))
                .build();
        var byteArrayInputStream = new ByteArrayInputStream(responseString.getBytes(StandardCharsets.UTF_8));
        var lazyBodyReader = new LazyBodyReader(new FramedBody.ContentLength(responseString.length()), byteArrayInputStream);
        var httpResponse = new RawHttpResponse<String>(null, null, statusLine, httpHeaders, lazyBodyReader);

        return httpResponse;
    }
}
