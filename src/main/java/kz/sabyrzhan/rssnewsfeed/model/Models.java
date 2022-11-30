package kz.sabyrzhan.rssnewsfeed.model;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

public class Models {
    public record User(int id, String username, String password) {
        public User(String username, String password) {
            this(0, username, password);
        }
    }

    public record Feed(int id, int userId, String title, String url, String text, @JsonAdapter(InstantTypeAdapter.class) Instant createDate) {
        public Feed {
            if (createDate == null) {
                createDate = Instant.now();
            }
        }
    }


    @Slf4j
    private static class InstantTypeAdapter implements JsonDeserializer<Instant>, JsonSerializer<Instant> {
        @Override
        public Instant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                var timeString = jsonElement.getAsString();
                var sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
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
