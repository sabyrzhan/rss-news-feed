package kz.sabyrzhan.rssnewsfeed.servlets.handlers;

@FunctionalInterface
public interface Runner<T> {
    void run(Request request);
}
