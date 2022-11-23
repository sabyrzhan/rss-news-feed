package kz.sabyrzhan.rssnewsfeed.servlets;

import jakarta.servlet.http.HttpServlet;

import java.util.HashMap;
import java.util.Map;

public class Register {
    private static Map<Class<? extends Object>, Object> singletons = new HashMap<>();

    public static void register(Object item) {
        singletons.put(item.getClass(), item);
    }

    public static <T> T getObject(Class<T> clazz) {
        return (T) singletons.get(clazz);
    }
}
