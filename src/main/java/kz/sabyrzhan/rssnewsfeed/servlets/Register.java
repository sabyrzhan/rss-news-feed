package kz.sabyrzhan.rssnewsfeed.servlets;

import kz.sabyrzhan.rssnewsfeed.servlets.handlers.Handler;
import kz.sabyrzhan.rssnewsfeed.servlets.handlers.Runner;
import org.springframework.web.util.UriTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static kz.sabyrzhan.rssnewsfeed.servlets.handlers.Response.EMPTY_RESPONSE;

public class Register {
    public enum HttpMethod {
        GET, POST, PUT, DELETE;

        public static HttpMethod fromString(String string) {
            string = string.toUpperCase();
            for(HttpMethod method: values()) {
                if (method.toString().equals(string)) {
                    return method;
                }
            }

            return null;
        }
    }

    private record MappingKey(HttpMethod method, String url) {};

    private static Map<MappingKey, Handler> singletons = new LinkedHashMap<>();
    private static Map<Class, Object> beans = new HashMap<>();

    public static void registerBean(Class clazz, Object bean) {
        beans.put(clazz, bean);
    }

    public static <T> T getBean(Class<T> clazz) {
        return (T) beans.get(clazz);
    }

    public static void registerGet(String context, Handler handler) {
        registerUrl(HttpMethod.GET, context, handler);
    }

    public static void registerPost(String context, Handler handler) {
        registerUrl(HttpMethod.POST, context, handler);
    }

    public static void registerPostRunner(String context, Runner handler) {
        registerUrl(HttpMethod.POST, context, request -> {
            handler.run(request);
            return EMPTY_RESPONSE;
        });
    }

    public static void registerPut(String context, Handler handler) {
        registerUrl(HttpMethod.PUT, context, handler);
    }

    public static void registerDelete(String context, Handler handler) {
        registerUrl(HttpMethod.DELETE, context, handler);
    }

    private static void registerUrl(HttpMethod method, String context, Handler handler) {
        singletons.put(new MappingKey(method, context), handler);
    }

    public static Handler getHandler(HttpMethod method, String context) {
        for (MappingKey mappingKey : singletons.keySet()) {
            var urlTemplate = new UriTemplate(mappingKey.url);
            if (mappingKey.method == method && urlTemplate.matches(context)) {
                return singletons.get(mappingKey);
            }
        }

        return null;
    }

    public static Map<String, String> getPathParams(HttpMethod method, String context) {
        for (MappingKey mappingKey : singletons.keySet()) {
            var urlTemplate = new UriTemplate(mappingKey.url);
            if (mappingKey.method == method && urlTemplate.matches(context)) {
                var params = urlTemplate.match(context);
                return params;
            }
        }

        return Map.of();
    }
}
