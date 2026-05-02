package com.seaman.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends CommonException{

    public BusinessException(String code, String message){
        super(HttpStatus.OK, code, message);
    }


    public BusinessException(String code, Object data){
        super(HttpStatus.OK, code, data);
    }


    public BusinessException(String code, String message, Object data){
        super(HttpStatus.OK, code, message, data);
    }
}
