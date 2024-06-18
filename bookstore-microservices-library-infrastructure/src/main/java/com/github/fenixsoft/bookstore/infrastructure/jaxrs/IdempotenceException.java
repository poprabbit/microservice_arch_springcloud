package com.github.fenixsoft.bookstore.infrastructure.jaxrs;

public class IdempotenceException extends RuntimeException {

    public IdempotenceException(String message) {
        super(message);
    }
}
