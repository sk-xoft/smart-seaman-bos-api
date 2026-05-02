package com.seaman.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.seaman.constant.AppSys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Component
public class LoggerService {

    private final Logger logger = LoggerFactory.getLogger(LoggerService.class);

    public void displayReq(HttpServletRequest request, Object body) {
        StringBuilder reqMessage = new StringBuilder();
        Map<String,String> parameters = getParameters(request);
        Map<String,String> reqHeaders = this.getHeaders(request);

        reqMessage.append("REQUEST ");
        reqMessage.append("method = [").append(request.getMethod()).append("]");
        reqMessage.append(" path = [").append(request.getRequestURI()).append("] ");

        if(!reqHeaders.isEmpty()) {
            reqMessage.append(" ReqHeaders = [").append(reqHeaders).append("]");
        }

        if(!parameters.isEmpty()) {
            reqMessage.append(" parameters = [").append(parameters).append("] ");
        }

        if(!Objects.isNull(body)) {
            Gson gson = new Gson();
            String bodyJson = gson.toJson(body);

            // Parse the JSON string into a JsonObject
            JsonObject jsonObject = gson.fromJson(bodyJson, JsonObject.class);

            // Remove the "password" field
            if(jsonObject.has("password")) {
                jsonObject.remove("password");
                jsonObject.addProperty("password", "XXXXXXX");
            }

            if(jsonObject.has("oldPassword")){
                jsonObject.remove("oldPassword");
                jsonObject.addProperty("oldPassword", "XXXXXXX");
            }

            if(jsonObject.has("confirmPassword")){
                jsonObject.remove("confirmPassword");
                jsonObject.addProperty("confirmPassword", "XXXXXXX");
            }

            if(jsonObject.has("newPassword")){
                jsonObject.remove("newPassword");
                jsonObject.addProperty("newPassword", "XXXXXXX");
            }

            // Add Cert "fileCert"
            if(jsonObject.has("bannerFromFile")){
                jsonObject.remove("bannerFromFile");
                jsonObject.addProperty("bannerFromFile", "FileBase64");
            }

            if(jsonObject.has("fileCert")){
                jsonObject.remove("fileCert");
                jsonObject.addProperty("fileCert", "FileBase64");
            }

            if(jsonObject.has("formFileName")){
                jsonObject.remove("formFileName");
                jsonObject.addProperty("formFileName", "FileBase64");
            }

            if(jsonObject.has("newsPictureFromFile")){
                jsonObject.remove("newsPictureFromFile");
                jsonObject.addProperty("newsPictureFromFile", "FileBase64");
            }

            if(jsonObject.has("voucherPicture")){
                jsonObject.remove("voucherPicture");
                jsonObject.addProperty("voucherPicture", "FileBase64");
            }

            if(jsonObject.has("voucherQrcode")){
                jsonObject.remove("voucherQrcode");
                jsonObject.addProperty("voucherQrcode", "FileBase64");
            }

            String modifiedJsonString = gson.toJson(jsonObject);

            reqMessage.append(" ReqBody = [").append(modifiedJsonString).append("]");

            // Set request body to attribute
            request.setAttribute(AppSys.REQUEST_BODY, modifiedJsonString);
        }

        logger.info("TraceId={}, -> {}", request.getAttribute(AppSys.TRACE_ID), reqMessage);
    }

    public void displayRes(HttpServletRequest request, HttpServletResponse response, Object body) {
        StringBuilder respMessage = new StringBuilder();
        Map<String,String> headers = getHeaders(response);
        respMessage.append("RESPONSE ");
        respMessage.append(" method = [").append(request.getMethod()).append("]");
        if(!headers.isEmpty()) {
            respMessage.append(" ResHeaders = [").append(headers).append("]");
        }
        respMessage.append(" ResBody = [").append(body).append("]");

        logger.info("TraceId={}, -> {}", request.getAttribute(AppSys.TRACE_ID), respMessage);
    }

    private Map<String,String> getHeaders(HttpServletResponse response) {
        Map<String,String> headers = new HashMap<>();
        Collection<String> headerMap = response.getHeaderNames();
        for(String str : headerMap) {
            headers.put(str,response.getHeader(str));
        }
        return headers;
    }

    private Map<String,String> getHeaders(HttpServletRequest request) {

        Map<String, String> map = new HashMap<String, String>();

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

    private Map<String,String> getParameters(HttpServletRequest request) {
        Map<String,String> parameters = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while(params.hasMoreElements()) {
            String paramName = params.nextElement();
            String paramValue = request.getParameter(paramName);
            parameters.put(paramName,paramValue);
        }

        return parameters;
    }

}
