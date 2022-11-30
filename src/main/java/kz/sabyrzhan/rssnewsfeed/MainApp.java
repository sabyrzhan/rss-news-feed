package kz.sabyrzhan.rssnewsfeed;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.sabyrzhan.rssnewsfeed.facade.FeedsFacade;
import kz.sabyrzhan.rssnewsfeed.facade.UserFacade;
import kz.sabyrzhan.rssnewsfeed.repository.FeedRepository;
import kz.sabyrzhan.rssnewsfeed.repository.UsersRepository;
import kz.sabyrzhan.rssnewsfeed.service.FeedService;
import kz.sabyrzhan.rssnewsfeed.service.UsersService;
import kz.sabyrzhan.rssnewsfeed.servlets.FeedsServlet;
import kz.sabyrzhan.rssnewsfeed.servlets.Register;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static kz.sabyrzhan.rssnewsfeed.servlets.handlers.Response.EMPTY_RESPONSE;

public class MainApp {
    public static void main(String[] args) throws Exception {
        var properties = new Properties();
        try (var inputStream = MainApp.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        }

        for(String envName: System.getenv().keySet()) {
            var value = System.getenv().get(envName);
            for(String propKey: properties.stringPropertyNames()) {
                var propValue = properties.getProperty(propKey);
                if (propValue.contains(envName)) {
                    properties.setProperty(propKey, value);
                }
            }
        }

        var hikariCp = new HikariConfig();
        hikariCp.setUsername(properties.getProperty("username"));
        hikariCp.setJdbcUrl(properties.getProperty("url"));
        hikariCp.setPassword(properties.getProperty("password"));
        var dataSource = new HikariDataSource(hikariCp);

        if (args.length > 0) {
            switch (args[0]) {
                case "--migrate":
                    DBMigration.migrate(dataSource);
                    break;
                default:
                    System.out.println("Invalid parameter");
            }
        } else {
            runCustomServer(dataSource);
//        runJetty();
        }
    }

    private static void runCustomServer(DataSource dataSource) throws Exception {
        var jdbcTemplate = new JdbcTemplate(dataSource);

        var feedRepository = new FeedRepository(jdbcTemplate);
        var userRepository = new UsersRepository(jdbcTemplate);

        var feedService = new FeedService(feedRepository);
        var userService = new UsersService(userRepository);

        var feedFacade = new FeedsFacade(feedService);
        var usersFacade = new UserFacade(userService);

        Register.registerGet("/api/feeds", request -> feedFacade.getFeeds(request));
        Register.registerGet("/api/users/user_by_username", request -> usersFacade.findByUsername(request));
        Register.registerPost("/api/users", request -> {
            usersFacade.save(request);
            return EMPTY_RESPONSE;
        });

        var service = new Service();
        service.run();
    }

    private static void runJetty() throws Exception {
//        ExecutorThreadPool threadPool = new ExecutorThreadPool();
//        threadPool.setUseVirtualThreads(true);

        var feedRepository = new FeedRepository(null);
        var feedService = new FeedService(feedRepository);
//        Register.register(feedService);
//
//        Register.register(new FeedsServlet());

        ThreadPool threadPool = new VirtualThreadPool();
        Server server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.setConnectors(new Connector[] {connector});
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(FeedsServlet.class, "/api/feeds");
        server.setHandler(servletHandler);
        server.start();
        server.join();
    }

    public static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("application/json");
            resp.setStatus(200);
            var data = new HashMap<>();
            data.put("status", "OK");
            resp.getWriter().println(new Gson().toJson(data));
        }
    }

    public static class VirtualThreadPool implements ThreadPool {
        private ExecutorService virtualExecutorService;
        public VirtualThreadPool() {
            virtualExecutorService = Executors.newVirtualThreadPerTaskExecutor();
        }

        @Override
        public void join() throws InterruptedException {
            virtualExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        }

        @Override
        public int getThreads() {
            return -1;
        }

        @Override
        public int getIdleThreads() {
            return -1;
        }

        @Override
        public boolean isLowOnThreads() {
            return false;
        }

        @Override
        public void execute(Runnable command) {
            virtualExecutorService.submit(command);
        }
    }
}
