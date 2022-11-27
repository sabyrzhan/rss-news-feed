package kz.sabyrzhan.rssnewsfeed.exception;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException() {
        super("User not found", 404);
    }
}
