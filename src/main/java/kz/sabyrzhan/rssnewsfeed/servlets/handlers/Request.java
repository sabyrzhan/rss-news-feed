package kz.sabyrzhan.rssnewsfeed.servlets.handlers;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import rawhttp.core.HttpMetadataParser;
import rawhttp.core.RawHttpRequest;

import java.nio.charset.StandardCharsets;

@Slf4j
public record Request(RawHttpRequest rawHttpRequest) {
    public int getPage() {
        return 1;
    }

    public <T> T mapBody(Class<T> clazz) {
        return rawHttpRequest.getBody().map(bodyReader -> {
            try {
                var gson = new Gson();
                return gson.fromJson(bodyReader.asRawString(StandardCharsets.UTF_8), clazz);
            } catch (Exception e) {
                log.warn("Body parsing error", e, e);
                return null;
            }
        }).orElse(null);
    }

    public String getParam(String param) {
        var params = new HttpMetadataParser(null).parseQueryString(rawHttpRequest.getUri().getQuery());
        return params.get(param).stream().findFirst().orElse(null);
    }
}
