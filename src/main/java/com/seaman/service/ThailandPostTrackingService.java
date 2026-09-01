package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ThailandPostTrackingService {

    private static final Logger log = LoggerFactory.getLogger(ThailandPostTrackingService.class);
    private static final String TOKEN_PATH = "/post/api/v1/authenticate/token";
    private static final String TRACK_PATH = "/post/api/v1/track";

    private final RestTemplate restTemplate;

    @Value("${THAILAND_POST_API_URL:${thailand.post.api-url:https://trackapi.thailandpost.co.th}}")
    private String apiUrl;

    @Value("${THAILAND_POST_TOKEN_KEY:${thailand.post.token-key:}}")
    private String tokenKey;

    @Value("${thailand.post.token-cache-file:${java.io.tmpdir}/smart-seaman-thailand-post-token.cache}")
    private String tokenCacheFilePath;

    private String accessToken;
    private Instant accessTokenExpiresAt = Instant.EPOCH;

    // Restores the cached token across application restarts so we don't re-authenticate every startup.
    @PostConstruct
    private void loadCachedAccessToken() {
        Path cacheFile = Paths.get(tokenCacheFilePath);
        if (!Files.isReadable(cacheFile)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(cacheFile);
            if (lines.size() < 2) {
                return;
            }
            Instant expiresAt = Instant.parse(lines.get(1).trim());
            if (Instant.now().isBefore(expiresAt)) {
                accessToken = lines.get(0).trim();
                accessTokenExpiresAt = expiresAt;
            }
        } catch (IOException | java.time.format.DateTimeParseException ex) {
            log.warn("Unable to read cached Thailand Post access token: {}", ex.getMessage());
        }
    }

    private void saveCachedAccessToken() {
        Path cacheFile = Paths.get(tokenCacheFilePath);
        try {
            Files.write(cacheFile, List.of(accessToken, accessTokenExpiresAt.toString()));
            Set<PosixFilePermission> ownerOnly = PosixFilePermissions.fromString("rw-------");
            Files.setPosixFilePermissions(cacheFile, ownerOnly);
        } catch (UnsupportedOperationException ex) {
            // Non-POSIX filesystem (e.g. Windows); skip permission hardening.
        } catch (IOException ex) {
            log.warn("Unable to persist Thailand Post access token cache: {}", ex.getMessage());
        }
    }

    public Map<String, Object> track(String trackingNo) {
        return track(Collections.singletonList(trackingNo)).get(trackingNo);
    }

    public Map<String, Map<String, Object>> track(List<String> trackingNos) {
        if (trackingNos == null || trackingNos.isEmpty()) {
            return Collections.emptyMap();
        }
        if (trackingNos.size() > 100) {
            throw new IllegalArgumentException("Thailand Post accepts up to 100 tracking numbers per request.");
        }

        try {
            Map<String, Object> providerResponse = requestTracking(trackingNos, getAccessToken());
            Map<String, Map<String, Object>> results = new LinkedHashMap<>();
            for (String trackingNo : trackingNos) {
                results.put(trackingNo, normalize(trackingNo, providerResponse));
            }
            return results;
        } catch (RestClientException ex) {
            log.error("Thailand Post tracking request failed for {} tracking number(s): {}",
                    trackingNos.size(), ex.getMessage(), ex);
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Thailand Post tracking service is unavailable.");
        }
    }

    private synchronized String getAccessToken() {
        if (accessToken != null && Instant.now().isBefore(accessTokenExpiresAt)) {
            return accessToken;
        }
        String configuredTokenKey = normalizeConfigValue(tokenKey);
        if (configuredTokenKey == null || configuredTokenKey.trim().isEmpty()) {
            configuredTokenKey = normalizeConfigValue(System.getenv("THAILAND_POST_TOKEN_KEY"));
        }
        if (configuredTokenKey == null || configuredTokenKey.trim().isEmpty()) {
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Thailand Post token key is not configured.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + configuredTokenKey.trim());
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<?> response = restTemplate.exchange(
        getApiUrl() + TOKEN_PATH,
                HttpMethod.POST,
                new HttpEntity<>(Collections.emptyMap(), headers),
                Map.class
        );
        Map<String, Object> body = map(response.getBody());
        Object token = body == null ? null : body.get("token");
        if (token == null || String.valueOf(token).trim().isEmpty()) {
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Thailand Post authentication failed.");
        }

        accessToken = String.valueOf(token);
        accessTokenExpiresAt = Instant.now().plusSeconds(29L * 24 * 60 * 60);
        saveCachedAccessToken();
        return accessToken;
    }

    private Map<String, Object> requestTracking(List<String> trackingNos, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "all");
        payload.put("language", "TH");
        payload.put("barcode", trackingNos);

        ResponseEntity<?> response = restTemplate.exchange(
                getApiUrl() + TRACK_PATH,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                Map.class
        );
        return map(response.getBody());
    }

    private String getApiUrl() {
        String configuredApiUrl = normalizeConfigValue(apiUrl);
        if (configuredApiUrl == null || configuredApiUrl.isEmpty()) {
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Thailand Post API URL is not configured.");
        }
        return configuredApiUrl.endsWith("/")
                ? configuredApiUrl.substring(0, configuredApiUrl.length() - 1)
                : configuredApiUrl;
    }

    private String normalizeConfigValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() >= 2) {
            char first = normalized.charAt(0);
            char last = normalized.charAt(normalized.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
            }
        }
        return normalized;
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