package kz.sabyrzhan.rssnewsfeed.service;

import kz.sabyrzhan.rssnewsfeed.exception.InternalServerException;
import kz.sabyrzhan.rssnewsfeed.exception.UserNotFoundException;
import kz.sabyrzhan.rssnewsfeed.model.Models;
import kz.sabyrzhan.rssnewsfeed.model.Models.Feed;
import kz.sabyrzhan.rssnewsfeed.repository.FeedRepository;
import kz.sabyrzhan.rssnewsfeed.repository.UsersRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;
    private final UsersRepository usersRepository;


    public List<Feed> getFeeds(int userId, int page) {
        return feedRepository.getFeeds(userId, page);
    }

    public Models.Id addFeed(Feed feed) {
        var existingUser = usersRepository.findById(feed.getUserId());
        if (existingUser == null) {
            throw new UserNotFoundException();
        }

        var newFeed = feedRepository.addFeed(feed);
        if (newFeed < 1) {
            throw new InternalServerException();
        }

        return new Models.Id(newFeed);
    }
}
