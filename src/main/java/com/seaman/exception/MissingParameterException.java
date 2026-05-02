package com.seaman.exception;

import org.springframework.http.HttpStatus;

public class MissingParameterException extends CommonException {

    public MissingParameterException(String code, String message){
        super(HttpStatus.BAD_REQUEST, code, message);
    }

    public MissingParameterException(String code, Object data){
        super(HttpStatus.BAD_REQUEST, code, data);
    }

    public MissingParameterException(String code, String message, Object data){
        super(HttpStatus.BAD_REQUEST, code, message, data);
    }

}
