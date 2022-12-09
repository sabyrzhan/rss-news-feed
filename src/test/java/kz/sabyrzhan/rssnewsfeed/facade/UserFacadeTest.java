package kz.sabyrzhan.rssnewsfeed.facade;

import kz.sabyrzhan.rssnewsfeed.model.Models;
import kz.sabyrzhan.rssnewsfeed.servlets.handlers.Response.ErrorResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class UserFacadeTest extends BaseRestTest {
    @Test
    void testCreateUser_success() {
        var username = RandomStringUtils.randomAlphabetic(5);
        var password = RandomStringUtils.randomAlphanumeric(10);
        var newUser = new Models.User(username, password);

        var result = makePostRequest("http://localhost:8080/api/users", newUser, Models.User.class);

        assertTrue(result.id() > 0);
        var existingUserResponse = makeGetRequest("http://localhost:8080/api/users/" + result.id(), Models.User.class);
        assertEquals(HttpStatus.OK, existingUserResponse.getValue());
        var existingUser = existingUserResponse.getKey();
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
        var existingUserResponse = makeGetRequest("http://localhost:8080/api/users/user_by_username?username=" + result.username(), Models.User.class);
        assertEquals(HttpStatus.OK, existingUserResponse.getValue());
        var existingUser = existingUserResponse.getKey();
        assertEquals(result.id(), existingUser.id());
        assertEquals(username, result.username());
        assertEquals(password, result.password());
    }

    @Test
    void testGetByUsername_userNotFound() {
        var error = makeGetRequest("http://localhost:8080/api/users/user_by_username?username=blablabla", ErrorResponse.class);
        assertEquals("User not found", error.getKey().error());
        assertEquals(HttpStatus.NOT_FOUND, error.getValue());
    }

    @Test
    void testGetById_userNotFound() {
        var error = makeGetRequest("http://localhost:8080/api/users/100", ErrorResponse.class);
        assertEquals("User not found", error.getKey().error());
        assertEquals(HttpStatus.NOT_FOUND, error.getValue());
    }
}