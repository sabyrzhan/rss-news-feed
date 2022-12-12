package kz.sabyrzhan.rssnewsfeed.repository;

import kz.sabyrzhan.rssnewsfeed.model.Models.Feed;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor
public class FeedRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final int MAX_RECORDS = 30;
    private static final RowMapper<Feed> RAW_MAPPER = new FeedMapper();

    public List<Feed> getFeeds(int userId, int page) {
        var feeds = jdbcTemplate.query("select * from feeds where user_id = ? limit " + MAX_RECORDS + " offset " + (page - 1) * MAX_RECORDS, RAW_MAPPER, userId);
        return feeds;
    }

    public Feed getFeedById(int id) {
        var query = "select * from feeds where id = ? limit 1";
        List<Feed> feeds = jdbcTemplate.query(query, RAW_MAPPER, id);

        return feeds.isEmpty() ? null : feeds.get(0);
    }

    public int addFeed(Feed feed) {
        var query = "insert into feeds(user_id, title, url, text, create_date) values(?,?,?,?,?) returning id";
        return jdbcTemplate.queryForObject(query, Integer.class, feed.getUserId(), feed.getTitle(), feed.getUrl(), feed.getText(), Timestamp.from(feed.getCreateDate()));
    }

    public static class FeedMapper implements RowMapper<Feed> {
        @Override
        public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
            var id = rs.getInt("id");
            var userId = rs.getInt("user_id");
            var title = rs.getString("title");
            var url = rs.getString("url");
            var text = rs.getString("text");
            var date = rs.getTimestamp("create_date").toInstant();

            return new Feed(id, userId, title, url, text, date);
        }
    }
}