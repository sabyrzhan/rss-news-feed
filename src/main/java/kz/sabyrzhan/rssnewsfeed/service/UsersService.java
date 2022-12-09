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

    public User findById(int id) {
        var user = usersRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException();
        }

        return user;
    }

    public User save(User user) {
        var existingUser = usersRepository.findUserByUsername(user.username());
        if (existingUser != null) {
            throw new UserAlreadyExistsException();
        }
        return usersRepository.save(user);
    }
}
