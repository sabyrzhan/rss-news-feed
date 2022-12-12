package kz.sabyrzhan.rssnewsfeed.facade;

import kz.sabyrzhan.rssnewsfeed.model.Models;
import kz.sabyrzhan.rssnewsfeed.repository.FeedRepository;
import kz.sabyrzhan.rssnewsfeed.servlets.handlers.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoField;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedsFacadeTest extends BaseRestTest {
    @Test
    void testCreateFeed_success() {
        var user = createUser();
        var createDate = Instant.now().with(ChronoField.MILLI_OF_SECOND, 0);
        var feed = new Models.Feed(user.id(), "Test title", "test-url.com", "Some text", createDate);

        var result = makePostRequest("/api/users/" + user.id() + "/feeds", feed, Models.Id.class);

        assertEquals(result.getValue(), HttpStatus.OK);
        var createdFeedId = result.getKey();
        var createdFeed = queryById(createdFeedId.id());
        feed = new Models.Feed(createdFeed.getId(), feed.getUserId(), feed.getTitle(), feed.getUrl(), feed.getText(), feed.getCreateDate());
        assertEquals(feed, createdFeed);
    }

    @Test
    void testCreateFeed_userNotFound() {
        var userId = 100;
        var createDate = Instant.now().with(ChronoField.MILLI_OF_SECOND, 0);
        var feed = new Models.Feed(userId, "Test title", "test-url.com", "Some text", createDate);

        var result = makePostRequest("/api/users/" + userId + "/feeds", feed, Models.Id.class);

        assertEquals(result.getValue(), HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetFeeds_success() {
        var user = createUser();
        for(int i = 0; i < 10; i++) {
            var createDate = Instant.now().with(ChronoField.MILLI_OF_SECOND, 0);
            var feed = new Models.Feed(user.id(), "Test title" + i, "test-url.com" + i, "Some text" + i, createDate);
            makePostRequest("/api/users/" + user.id() + "/feeds", feed, Models.Id.class);
        }

        var result = makeGetRequest("/api/users/" + user.id() + "/feeds", Models.Feed[].class);

        assertEquals(result.getValue(), HttpStatus.OK);
        var feeds = result.getKey();
        assertEquals(10, feeds.length);
    }

    @Test
    void testGetFeeds_userNotFound() {
        var result = makeGetRequest("/api/users/100/feeds", Response.ErrorResponse.class);

        assertEquals(result.getValue(), HttpStatus.NOT_FOUND);
    }

    private Models.User createUser() {
        var username = RandomStringUtils.randomAlphabetic(5);
        var password = RandomStringUtils.randomAlphanumeric(10);
        var newUser = new Models.User(username, password);

        var result = makePostRequest("/api/users", newUser, Models.User.class);

        return result.getKey();
    }

    private Models.Feed queryById(int id) {
        var feedRepository = new FeedRepository(jdbcTemplate);
        return feedRepository.getFeedById(id);
    }
}