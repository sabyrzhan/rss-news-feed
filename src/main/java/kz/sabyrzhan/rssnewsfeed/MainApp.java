package kz.sabyrzhan.rssnewsfeed;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.sabyrzhan.rssnewsfeed.repository.FeedRepository;
import kz.sabyrzhan.rssnewsfeed.service.FeedService;
import kz.sabyrzhan.rssnewsfeed.servlets.FeedsServlet;
import kz.sabyrzhan.rssnewsfeed.servlets.Register;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.ThreadPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainApp {
    public static void main(String[] args) throws Exception {
        runCustomServer();
        //runJetty();
    }

    private static void runCustomServer() throws Exception {
        var service = new Service();
        service.run();
    }

    private static void runJetty() throws Exception {
//        ExecutorThreadPool threadPool = new ExecutorThreadPool();
//        threadPool.setUseVirtualThreads(true);

        var feedRepository = new FeedRepository();
        var feedService = new FeedService(feedRepository);
        Register.register(feedService);

        Register.register(new FeedsServlet());

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
