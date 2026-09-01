package com.seaman.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ThailandPostTrackingServiceTest {

    private final ThailandPostTrackingService service =
            new ThailandPostTrackingService(mock(RestTemplate.class));

    @Test
    void removesQuotesAndTrailingSlashFromApiUrl() {
        ReflectionTestUtils.setField(service, "apiUrl", "\"https://trackapi.thailandpost.co.th/\"");

        String apiUrl = ReflectionTestUtils.invokeMethod(service, "getApiUrl");

        assertEquals("https://trackapi.thailandpost.co.th", apiUrl);
    }

    @Test
    void removesSingleQuotesFromApiUrl() {
        ReflectionTestUtils.setField(service, "apiUrl", "'https://trackapi.thailandpost.co.th'");

        String apiUrl = ReflectionTestUtils.invokeMethod(service, "getApiUrl");

        assertEquals("https://trackapi.thailandpost.co.th", apiUrl);
    }
}