package kz.sabyrzhan.rssnewsfeed.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends ApiException {
    public InternalServerException() {
        super("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
