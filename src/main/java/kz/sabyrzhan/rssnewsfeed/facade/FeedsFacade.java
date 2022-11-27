package kz.sabyrzhan.rssnewsfeed.facade;

import kz.sabyrzhan.rssnewsfeed.model.Models.Feed;
import kz.sabyrzhan.rssnewsfeed.service.FeedService;
import kz.sabyrzhan.rssnewsfeed.servlets.handlers.Request;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FeedsFacade {
    private final FeedService feedService;

    public List<Feed> getFeeds(Request request) {
        return feedService.getFeeds(request.getPage());
    }
}
