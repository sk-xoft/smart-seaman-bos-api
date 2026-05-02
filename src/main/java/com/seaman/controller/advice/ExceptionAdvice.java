package com.seaman.controller.advice;

import com.seaman.constant.AppStatus;
import com.seaman.model.common.ErrorMessage;
import com.seaman.service.MessageCodeService;
import com.seaman.constant.AppSys;
import com.seaman.exception.CommonException;
import com.seaman.model.response.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionAdvice  extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final MessageCodeService messageCodeService;

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(CommonException ex) {

        if(ex.getCauseException() != null){
            log.error(ExceptionUtils.getStackTrace(ex.getCauseException()));
        }

        String code = ex.getCode();
        String message = messageCodeService.getMessageDescription(code, AppSys.LANG_TH);

        return this.handleExceptionResponse(code, message, ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(CommonException.class)
    protected ResponseEntity<Object> handleCommonException(CommonException ex) {

        if(ex.getCauseException() != null){
            log.error(ExceptionUtils.getStackTrace(ex.getCauseException()));
        }

        String code = ex.getCode();
        String message = messageCodeService.getMessageDescription(code, AppSys.LANG_TH);

        if(null != ex.getData()) {
            ErrorMessage errorMessage = (ErrorMessage) ex.getData();
            message = message.replace(errorMessage.getCharReplace(), errorMessage.getValReplace());
        } else {

            if (null != ex.getMessage() || !"".equals(ex.getMessage())) {
                message += ex.getMessage();
            }
        }

        return this.handleExceptionResponse(code, message, ex.getStatus(), ex.getData());
    }

    private ResponseEntity<Object> handleExceptionResponse(String errorCode, String errorMessage, HttpStatus status, Object data) {
        ExceptionResponse response = new ExceptionResponse();
        response.setCode(errorCode);
        response.setDescription(errorMessage);
        response.setData(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.status(status).headers(headers).body(response);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        pageNotFoundLogger.warn(ex.getMessage());
        Set<HttpMethod> supportedMethods = ex.getSupportedHttpMethods();
        if (!CollectionUtils.isEmpty(supportedMethods)) {
            headers.setAllow(supportedMethods);
        }

        //  TODO Message code
        String code = "MA405";
        String message = messageCodeService.getMessageDescription(code, AppSys.LANG_TH);
        return this.handleExceptionResponse(code, message, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String code = AppStatus.ATTRIBUTE_IS_REQUIRE;
        String message = messageCodeService.getMessageDescription(code, AppSys.LANG_TH);
        log.info("validate date : {}" , ex.getParameterName());
        return this.handleExceptionResponse(code, message, status, request);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request){
        String code = AppStatus.ATTRIBUTE_IS_REQUIRE;
        String message = messageCodeService.getMessageDescription(code, request.getHeader(AppSys.LANGUAGE));

        /**
         * get attribute is missing parameter
         */
        Map<String, List<String>> body = new HashMap<>();
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        body.put("errors", errors);
        log.error("Request body error : {}", body);
        return this.handleExceptionResponse(code, message, status, body);
    }

}
