package com.urlshortener.url_shortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String msg) {
        super(msg);
    }
}
