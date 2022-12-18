package kz.sabyrzhan.rssnewsfeed.model;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

public class Models {
    public record Id(int id) {}

    public record User(int id, String username, String password) {
        public User {
            username = username.toLowerCase();
        }
        public User(String username, String password) {
            this(0, username, password);
        }
    }

    @Data
    public static class Feed {
        private int id;
        private int userId;
        private String title;
        private String url;
        private String text;
        @JsonAdapter(InstantTypeAdapter.class)
        Instant createDate;


        public Feed() {
            createDate = Instant.now();
        }

        public Feed(int id, int userId, String title, String url, String text, Instant createDate) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.url = url;
            this.text = text;
            if (createDate == null) {
                this.createDate = Instant.now();
            } else {
                this.createDate = createDate;
            }
        }

        public Feed(int userId, String title, String url, String text, Instant createDate) {
            this(0, userId, title, url, text, createDate);
        }

        public Feed(int userId, String title, String url, String text) {
            this(0, userId, title, url, text, null);
        }
    }


    @Slf4j
    private static class InstantTypeAdapter implements JsonDeserializer<Instant>, JsonSerializer<Instant> {
        @Override
        public Instant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                var timeString = jsonElement.getAsString();
                var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse(timeString).toInstant();
            } catch (Exception e) {
                log.debug("Failed to deserialize instant: {}", e.toString());
                return null;
            }
        }

        @Override
        public JsonElement serialize(Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
            try {
                var sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return new JsonPrimitive(sdf.format(Date.from(instant)));
            } catch (Exception e) {
                log.debug("Failed to serialize instant: {}", e.toString());
                return null;
            }
        }
    }
}
