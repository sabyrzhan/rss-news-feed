package kz.sabyrzhan.rssnewsfeed.facade;

import kz.sabyrzhan.rssnewsfeed.exception.UserNotFoundException;
import kz.sabyrzhan.rssnewsfeed.model.Models;
import kz.sabyrzhan.rssnewsfeed.model.Models.Feed;
import kz.sabyrzhan.rssnewsfeed.service.FeedService;
import kz.sabyrzhan.rssnewsfeed.service.UsersService;
import kz.sabyrzhan.rssnewsfeed.servlets.handlers.Request;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FeedsFacade {
    private final FeedService feedService;
    private final UsersService usersService;

    public List<Feed> getFeeds(Request request) {
        var userId = Integer.valueOf(request.getPathParams("userId"));
        var user = usersService.findById(userId);
        if (user == null) {
            throw new UserNotFoundException();
        }

        return feedService.getFeeds(userId, request.getPage());
    }

    public Models.Id addFeed(Request request) {
        var userId = Integer.parseInt(request.getPathParams("userId"));
        var feed = request.mapBody(Feed.class);
        feed.setUserId(userId);
        return feedService.addFeed(feed);
    }
}
