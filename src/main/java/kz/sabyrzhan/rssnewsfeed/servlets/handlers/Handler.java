package kz.sabyrzhan.rssnewsfeed.servlets.handlers;

@FunctionalInterface
public interface Handler<T> {
    T handle(Request request);
}
