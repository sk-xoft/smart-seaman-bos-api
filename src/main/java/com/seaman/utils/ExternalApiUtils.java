package com.seaman.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ExternalApiUtils {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String CONTENT_TYPE = "Content-Type";

    private final RestTemplate restTemplate;

    private static final String TEMPLATE_LOG_REQUEST_START = "REST caller start. url : {} , requestHeader: {} , requestBody: {} ";
    private static final String TEMPLATE_LOG_RESPONSE_END = "REST caller end.  url:{} , responseCode: {} , responseHeader: {} , responseBody: {} ";
    private static final String TEMPLATE_LOG_RESPONSE_EXCEPTION = "REST caller end.  url:{} , responseCode: {} , Exception : {} ";

    public Object commonCaller(String url, HttpMethod httpMethod, HttpHeaders headers,
                               Object requestObject, Class<?> requestType,
                               Class<?> responseSuccess, Class<?> responseFailed) {

//        Gson gson = new Gson();
        Gson gson = new GsonBuilder().serializeNulls().create();
        Object response = null;
        String requestStr = null;
        if (StringUtils.isNotEmpty(url) && null != httpMethod) {

            if (null != requestObject && null != requestType) {
                requestStr = gson.toJson(requestObject, requestType);
            }

            if (headers == null) {
                headers = new HttpHeaders();
            }

            headers.set(CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

            HttpEntity<String> entity = new HttpEntity<>(requestStr, headers);
            log.info(TEMPLATE_LOG_REQUEST_START, url, headers, requestStr);
            ResponseEntity<String> restResponse = (ResponseEntity<String>) this.commonRestAPICaller(restTemplate, httpMethod, url, entity);

            response = getCommonResponse(restResponse, responseSuccess, responseFailed);
        }

        return response;
    }

    private Object commonRestAPICaller(RestTemplate restTemplate, HttpMethod httpMethod, String url, HttpEntity<String> entity) {

        ResponseEntity<String> restResponse = null;

        try {
            restResponse = restTemplate.exchange(url, httpMethod, entity, String.class);
            log.info(TEMPLATE_LOG_RESPONSE_END, url, restResponse.getStatusCode(), restResponse.getHeaders(), restResponse.getBody());

        } catch (Exception ex) {

            HttpStatusCodeException httpStatusCodeException = (HttpStatusCodeException) ex;
            int statusCode = httpStatusCodeException.getRawStatusCode();

            log.error(TEMPLATE_LOG_RESPONSE_EXCEPTION, url, statusCode, ex.getMessage());
            throw ex;
        }
        return restResponse;
    }

    private Object getCommonResponse(ResponseEntity<String> restResponse, Class<?> responseSuccess, Class<?> responseFailed) {
        Gson gson = new Gson();
        Object response = null;

        if (HttpStatus.OK.equals(restResponse.getStatusCode()) || HttpStatus.ACCEPTED.equals(restResponse.getStatusCode())) {
            response = null != restResponse.getBody()
                    ? gson.fromJson(restResponse.getBody(), responseSuccess)
                    : null;

        } else {
            response = null != restResponse.getBody()
                    ? gson.fromJson(restResponse.getBody(), responseFailed)
                    : null;
        }
        return response;
    }

}
