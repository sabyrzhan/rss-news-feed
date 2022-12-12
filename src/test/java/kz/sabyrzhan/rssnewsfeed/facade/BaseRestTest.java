package kz.sabyrzhan.rssnewsfeed.facade;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import kz.sabyrzhan.rssnewsfeed.MainApp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Testcontainers
@Slf4j
public class BaseRestTest {
    protected static final String API_BASE_URL = "http://localhost:8080";

    protected JdbcTemplate jdbcTemplate;

    @Container
    static PostgreSQLContainer pgContainer = new PostgreSQLContainer("postgres")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    static HttpClient client = HttpClient.newHttpClient();

    static ExecutorService executorService;

    @BeforeEach
    void setUp() throws Exception {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(pgContainer.getJdbcUrl());
        hikariConfig.setUsername(pgContainer.getUsername());
        hikariConfig.setPassword(pgContainer.getPassword());
        hikariConfig.setDriverClassName(pgContainer.getDriverClassName());
        var dataSource = new HikariDataSource(hikariConfig);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @BeforeAll
    static void setUpAll() throws Exception {
        executorService = Executors.newFixedThreadPool(1);
        executorService.submit(() -> {
            try {
                var port = pgContainer.getMappedPort(5432);

                var properties = new Properties();
                properties.setProperty("url", "jdbc:postgresql://localhost:" + pgContainer.getMappedPort(5432) + "/test");
                properties.setProperty("password", "test");
                properties.setProperty("username", "test");

                log.info("Migration starting...");
                String appPath = MainApp.class.getClassLoader().getResource("application.properties").getPath();
                var migrationPath = "filesystem:" + Paths.get(appPath).getParent().toAbsolutePath() + "/db/migration";
                log.info("Migration path: {}", migrationPath);

                MainApp.runApp(new String[] {"--migrate", "--migrationPath", migrationPath}, properties);
                log.info("Migration finished");
                log.info("App starting...");
                MainApp.runApp(new String[] {}, properties);
                log.info("App started");
            } catch (Exception e) {
                log.error("Error in setup", e, e);
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(5_000);
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        System.out.println("Shutting down executor");
        executorService.shutdownNow();
        Thread.sleep(2_000);
        System.out.println("Executor shut down complete");
    }

    protected  <T> Pair<T, HttpStatus> makeGetRequest(String url, Class<T> clazz) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(API_BASE_URL + url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String json = response.body();

            log.info("Received GET response: {}", json);

            var gson = new Gson();

            var result = gson.fromJson(json, clazz);
            return Pair.of(result, HttpStatus.valueOf(response.statusCode()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected  <T> Pair<T, HttpStatus> makePostRequest(String url, Object params, Class<T> clazz) {
        try {
            var gson = new Gson();
            String paramsString = gson.toJson(params);

            var request = HttpRequest.newBuilder()
                    .uri(new URI(API_BASE_URL + url))
                    .headers("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(paramsString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Received POST response: {}", response.body());

            String json = response.body();

            var result = gson.fromJson(json, clazz);
            return Pair.of(result, HttpStatus.valueOf(response.statusCode()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
