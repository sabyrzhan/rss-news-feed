package kz.sabyrzhan.rssnewsfeed.service;

import kz.sabyrzhan.rssnewsfeed.exception.UserNotFoundException;
import kz.sabyrzhan.rssnewsfeed.model.Models.Feed;
import kz.sabyrzhan.rssnewsfeed.repository.FeedRepository;
import kz.sabyrzhan.rssnewsfeed.repository.UsersRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;
    private final UsersRepository usersRepository;


    public List<Feed> getFeeds(int page) {
        return feedRepository.getFeeds(page);
    }

    public void addFeed(Feed feed) {
        var existingUser = usersRepository.findById(feed.userId());
        if (existingUser == null) {
            throw new UserNotFoundException();
        }

        feedRepository.addFeed(feed);
    }
}
