package kz.sabyrzhan.rssnewsfeed.facade;

import kz.sabyrzhan.rssnewsfeed.model.Models.User;
import kz.sabyrzhan.rssnewsfeed.service.UsersService;
import kz.sabyrzhan.rssnewsfeed.servlets.handlers.Request;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserFacade {
    private final UsersService usersService;

    public User findByUsername(Request request) {
        var username = request.getParam("username");
        return usersService.findByUsername(username);
    }

    public User findById(Request request) {
        var id = Integer.parseInt(request.getPathParams("id"));
        return usersService.findById(id);
    }

    public User save(Request request) {
        var user = request.mapBody(User.class);
        return usersService.save(user);
    }
}
