package com.MonitoreoViru.exceptions;

/** Mapped to HTTP 404 by GlobalExceptionHandler. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
