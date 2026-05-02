package com.seaman.interceptor;

import com.seaman.constant.AppSys;
import com.seaman.exception.MissingParameterException;
import com.seaman.utils.HttpsUtils;
import com.seaman.utils.ObjectValidatorUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.UUID;

/**
 *  this default interceptor first validate common header.
 */
@Component
@RequiredArgsConstructor
public class APIInterceptor implements HandlerInterceptor {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpsUtils httpsUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Mark trace id
        request.setAttribute(AppSys.TRACE_ID, UUID.randomUUID().toString().toLowerCase());

        // Mark time api start
        long startTime = Instant.now().toEpochMilli();
        request.setAttribute(AppSys.API_EXECUTIME, startTime);

        // Get client ip
        request.setAttribute(AppSys.CLIENT_IP, httpsUtils.getClientIp(request));

        // Set Language is default EN
        request.setAttribute(AppSys.LANGUAGE,  request.getHeader(AppSys.HEADER_LANGUAGE) == null ? AppSys.LANG_EN : request.getHeader(AppSys.HEADER_LANGUAGE));

        // Validate Header
        String language = request.getHeader(AppSys.HEADER_LANGUAGE);
        if(!ObjectValidatorUtils.validateMandatory(language)) {
            throw new MissingParameterException("MA20004", " Missing header parameter Language.");
        }

        String deviceModel = request.getHeader(AppSys.HEADER_DEVICE_MODEL);
        if(null == deviceModel || !ObjectValidatorUtils.validateMandatory(deviceModel)) {
            throw new MissingParameterException("MA20004", " Missing header parameter device model.");
        }

        String correlationid = request.getHeader(AppSys.HEADER_CORRELATION_ID);
        if(null == correlationid || !ObjectValidatorUtils.validateMandatory(correlationid)) {
            throw new MissingParameterException("MA20004", " Missing header parameter correlation id.");
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

        // Mark log response

        // Mark time api end.
        long startTime = (Long)request.getAttribute(AppSys.API_EXECUTIME);
        long endTime = Instant.now().toEpochMilli();
        long timeElapsed = endTime - startTime;
        log.info("Service process ended, Path name : {} , Using time : {} (ms).", request.getRequestURI(), timeElapsed);
    }

}
