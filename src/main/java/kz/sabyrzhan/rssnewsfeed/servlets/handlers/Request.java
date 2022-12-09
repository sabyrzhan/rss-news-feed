package kz.sabyrzhan.rssnewsfeed.servlets.handlers;

import com.google.gson.Gson;
import kz.sabyrzhan.rssnewsfeed.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import rawhttp.core.HttpMetadataParser;
import rawhttp.core.RawHttpRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public record Request(RawHttpRequest rawHttpRequest, Map<String, String> pathParams) {
    public int getPage() {
        var pageString = getParam("page");
        return pageString == null ? 1 : Integer.parseInt(pageString);
    }

    public <T> T mapBody(Class<T> clazz) {
        return rawHttpRequest.getBody().map(bodyReader -> {
            try {
                var gson = new Gson();
                return gson.fromJson(bodyReader.asRawString(StandardCharsets.UTF_8), clazz);
            } catch (Exception e) {
                log.warn("Body parsing error. Maybe bad request: {}", e.getMessage());
                throw new BadRequestException();
            }
        }).orElse(null);
    }

    public String getParam(String param) {
        try {
            var params = new HttpMetadataParser(null).parseQueryString(rawHttpRequest.getUri().getQuery());
            return params.get(param).stream().findFirst().orElse(null);
        } catch (Exception e) {
            log.warn("No {} param", param, e, e);
            return null;
        }
    }

    public String getPathParams(String paramName) {
        return pathParams.get(paramName);
    }
}
