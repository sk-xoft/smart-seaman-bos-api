package com.seaman.utils;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FrameworkUtils {

    public String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }

    public String toObjectToJson(Object obj) {
        return  new Gson().toJson(obj);
    }

}

