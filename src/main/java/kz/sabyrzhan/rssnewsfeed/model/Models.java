package kz.sabyrzhan.rssnewsfeed.model;

import java.time.Instant;

public class Models {
    public record User(int id, String username, String password) {
        public User(String username, String password) {
            this(0, username, password);
        }
    }
    public record Feed(int id, int userId, String title, String url, String text, Instant createDate) {}
}
