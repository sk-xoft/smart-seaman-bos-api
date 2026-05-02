package com.seaman.utils;

import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

@Component
public class HttpsUtils {

    public String getClientIp(HttpServletRequest httpServletRequest) {

        String remoteAddr = "";

        if (httpServletRequest != null) {
            remoteAddr = httpServletRequest.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = httpServletRequest.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
}
