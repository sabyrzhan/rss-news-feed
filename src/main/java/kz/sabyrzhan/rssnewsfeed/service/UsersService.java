package kz.sabyrzhan.rssnewsfeed.service;

import kz.sabyrzhan.rssnewsfeed.exception.UserAlreadyExistsException;
import kz.sabyrzhan.rssnewsfeed.exception.UserNotFoundException;
import kz.sabyrzhan.rssnewsfeed.model.Models.User;
import kz.sabyrzhan.rssnewsfeed.repository.UsersRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;

    public User findByUsername(String username) {
        var user = usersRepository.findUserByUsername(username);
        if (user == null) {
            throw new UserNotFoundException();
        }

        return user;
    }

    public void save(User user) {
        var existingUser = usersRepository.findUserByUsername(user.username());
        if (existingUser != null) {
            throw new UserAlreadyExistsException();
        }
        usersRepository.save(user);
    }
}
