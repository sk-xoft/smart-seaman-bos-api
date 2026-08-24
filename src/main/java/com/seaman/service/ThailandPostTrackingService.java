package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ThailandPostTrackingService {

    private static final String TOKEN_PATH = "/post/api/v1/authenticate/token";
    private static final String TRACK_PATH = "/post/api/v1/track";

    private final RestTemplate restTemplate;

    @Value("${THAILAND_POST_API_URL:${thailand.post.api-url:https://trackapi.thailandpost.co.th}}")
    private String apiUrl;

    @Value("${THAILAND_POST_TOKEN_KEY:${thailand.post.token-key:}}")
    private String tokenKey;

    private String accessToken;
    private Instant accessTokenExpiresAt = Instant.EPOCH;

    public Map<String, Object> track(String trackingNo) {
        try {
            Map<String, Object> providerResponse = requestTracking(trackingNo, getAccessToken());
            return normalize(trackingNo, providerResponse);
        } catch (RestClientException ex) {
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Thailand Post tracking service is unavailable.");
        }
    }

    private synchronized String getAccessToken() {
        if (accessToken != null && Instant.now().isBefore(accessTokenExpiresAt)) {
            return accessToken;
        }
        String configuredTokenKey = tokenKey;
        if (configuredTokenKey == null || configuredTokenKey.trim().isEmpty()) {
            configuredTokenKey = System.getenv("THAILAND_POST_TOKEN_KEY");
        }
        if (configuredTokenKey == null || configuredTokenKey.trim().isEmpty()) {
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Thailand Post token key is not configured.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + configuredTokenKey.trim());
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + TOKEN_PATH,
                HttpMethod.POST,
                new HttpEntity<>(Collections.emptyMap(), headers),
                Map.class
        );
        Map body = response.getBody();
        Object token = body == null ? null : body.get("token");
        if (token == null || String.valueOf(token).trim().isEmpty()) {
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Thailand Post authentication failed.");
        }

        accessToken = String.valueOf(token);
        accessTokenExpiresAt = Instant.now().plusSeconds(29L * 24 * 60 * 60);
        return accessToken;
    }

    private Map<String, Object> requestTracking(String trackingNo, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "all");
        payload.put("language", "TH");
        payload.put("barcode", Collections.singletonList(trackingNo));

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + TRACK_PATH,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                Map.class
        );
        return response.getBody();
    }

    private Map<String, Object> normalize(String trackingNo, Map<String, Object> providerResponse) {
        List<Map<String, Object>> events = new ArrayList<>();
        Map<String, Object> response = map(providerResponse == null ? null : providerResponse.get("response"));
        Map<String, Object> items = map(response == null ? null : response.get("items"));
        Object rawEvents = items == null ? null : items.get(trackingNo);

        if (rawEvents instanceof List) {
            for (Object rawEvent : (List<?>) rawEvents) {
                Map<String, Object> event = map(rawEvent);
                if (event == null) {
                    continue;
                }

                Map<String, Object> normalizedEvent = new LinkedHashMap<>();
                normalizedEvent.put("time", value(event, "status_date"));
                normalizedEvent.put("status", value(event, "status_description"));
                normalizedEvent.put("location", value(event, "location"));
                normalizedEvent.put("postcode", value(event, "postcode"));
                normalizedEvent.put("statusCode", value(event, "status"));
                events.add(normalizedEvent);
            }
        }

        events.sort(Comparator.comparing(event -> String.valueOf(event.get("time")), Comparator.reverseOrder()));
        for (int index = 0; index < events.size(); index++) {
            events.get(index).put("current", index == 0);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("trackingNo", trackingNo);
        result.put("provider", "Thailand Post");
        result.put("events", events);
        result.put("lastUpdated", Instant.now().toString());
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    private Object value(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "-" : value;
    }
}