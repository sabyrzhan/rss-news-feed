package kz.sabyrzhan.rssnewsfeed.service;

import kz.sabyrzhan.rssnewsfeed.model.Feed;
import kz.sabyrzhan.rssnewsfeed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;


    public List<Feed> getFeeds(int page) {
        return feedRepository.getFeeds(page);
    }
}
