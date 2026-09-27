package org.images.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidOperationException extends RuntimeException {
    private HttpStatus status;

    public InvalidOperationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
