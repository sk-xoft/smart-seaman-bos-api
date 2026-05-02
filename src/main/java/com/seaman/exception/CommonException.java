package com.seaman.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.io.Serializable;

@Getter
public abstract class CommonException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 2405172041950251807L;

    protected final HttpStatus status;
    protected final String code;
    protected final Exception causeException;
    protected final Object data;

    CommonException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
        this.causeException = null;
        this.data = null;
    }

    CommonException(HttpStatus status, String code, Object data) {
        this.status = status;
        this.code = code;
        this.causeException = null;
        this.data = data;
    }

    CommonException(HttpStatus status, String code, String message,Exception causeException) {
        super(message);
        this.status = status;
        this.code = code;
        this.causeException = causeException;
        this.data = null;
    }

    CommonException(HttpStatus status, String code, String message, Object data) {
        super(message);
        this.status = status;
        this.code = code;
        this.causeException = null;
        this.data = data;
    }
}