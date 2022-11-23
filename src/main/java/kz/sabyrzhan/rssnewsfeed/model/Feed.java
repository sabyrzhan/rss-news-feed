package kz.sabyrzhan.rssnewsfeed.model;

import lombok.Data;

@Data
public class Feed {
    private int id;
    private String source;
    private String url;
    private String description;
}
