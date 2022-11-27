package kz.sabyrzhan.rssnewsfeed.servlets.handlers;

import rawhttp.core.HttpMetadataParser;
import rawhttp.core.RawHttpRequest;

public record Request(RawHttpRequest rawHttpRequest) {
    public int getPage() {
        return 1;
    }

    public String getParam(String param) {
        var params = new HttpMetadataParser(null).parseQueryString(rawHttpRequest.getUri().getQuery());
        return params.get(param).stream().findFirst().orElse(null);
    }
}
