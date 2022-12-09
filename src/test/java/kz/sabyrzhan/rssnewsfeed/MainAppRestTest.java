package kz.sabyrzhan.rssnewsfeed;

import com.google.gson.Gson;
import kz.sabyrzhan.rssnewsfeed.model.Models;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@Slf4j
class MainAppRestTest {

    @Container
    static PostgreSQLContainer pgContainer = new PostgreSQLContainer("postgres")
                .withDatabaseName("test")
                .withUsername("test")
                .withPassword("test");

    static ExecutorService executorService = Executors.newFixedThreadPool(1);

    @BeforeAll
    static void setUp() throws Exception {
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
    static void tearDown() {
        System.out.println("Shutting down init");
        executorService.shutdown();
        System.out.println("Shutting down complete");
    }

    @Test
    void testCreateUser_success() {
        var username = RandomStringUtils.randomAlphabetic(5);
        var password = RandomStringUtils.randomAlphanumeric(10);
        var newUser = new Models.User(username, password);

        var result = makePostRequest("http://localhost:8080/api/users", newUser, Models.User.class);

        assertTrue(result.id() > 0);
        var existingUser = makeGetRequest("http://localhost:8080/api/users/" + result.id(), Models.User.class);
        assertEquals(result.id(), existingUser.id());
        assertEquals(username, result.username());
        assertEquals(password, result.password());
    }

    @Test
    void testGetByUsername_success() {
        var username = RandomStringUtils.randomAlphabetic(5);
        var password = RandomStringUtils.randomAlphanumeric(10);
        var newUser = new Models.User(username, password);

        var result = makePostRequest("http://localhost:8080/api/users", newUser, Models.User.class);

        assertTrue(result.id() > 0);
        var existingUser = makeGetRequest("http://localhost:8080/api/users/user_by_username?username=" + result.username(), Models.User.class);
        assertEquals(result.id(), existingUser.id());
        assertEquals(username, result.username());
        assertEquals(password, result.password());
    }

    private <T> T makeGetRequest(String url, Class<T> clazz) {
        try {
            var client = HttpClient.newHttpClient();

            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String json = response.body();

            log.info("Received GET response: {}", json);

            var gson = new Gson();

            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T makePostRequest(String url, Object params, Class<T> clazz) {
        try {
            var client = HttpClient.newHttpClient();

            var gson = new Gson();
            String paramsString = gson.toJson(params);

            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .headers("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(paramsString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Received POST response: {}", response.body());

            String json = response.body();

            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}