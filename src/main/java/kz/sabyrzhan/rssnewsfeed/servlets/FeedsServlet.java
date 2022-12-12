package kz.sabyrzhan.rssnewsfeed.servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.sabyrzhan.rssnewsfeed.service.FeedService;

import java.io.IOException;

public class FeedsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var feedService = Register.getObject(FeedService.class);
        var feeds = feedService.getFeeds(1, 1);
        resp.getWriter().print(new Gson().toJson(feeds));
    }
}
