package com.seaman.service;

import com.seaman.repository.DocumentRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentDeliveryTrackingSchedulerTest {

    @Mock
    private DocumentRequestRepository documentRequestRepository;
    @Mock
    private DocumentRenewalDetailService documentRenewalDetailService;
        @Mock
        private ThailandPostTrackingService thailandPostTrackingService;
    @InjectMocks
    private DocumentDeliveryTrackingScheduler scheduler;

    @Test
        void sendsDeliveringTrackingNumbersInOneBatch() {
        when(documentRequestRepository.findDeliveringRequestsWithTrackingNo()).thenReturn(List.of(
                Map.of("request_no", "260700046", "tracking_no", "ED363095983TH"),
                Map.of("request_no", "260700047", "tracking_no", "ED363095984TH")
        ));
        when(thailandPostTrackingService.track(List.of("ED363095983TH", "ED363095984TH"))).thenReturn(Map.of(
            "ED363095983TH", Map.of("events", List.of()),
            "ED363095984TH", Map.of("events", List.of())
        ));

        scheduler.updateDeliveryStatuses();

        verify(thailandPostTrackingService).track(List.of("ED363095983TH", "ED363095984TH"));
        verify(documentRenewalDetailService).markDeliveredIfSuccessful("260700046", Map.of("events", List.of()));
        verify(documentRenewalDetailService).markDeliveredIfSuccessful("260700047", Map.of("events", List.of()));
    }

    @Test
    void skipsPlaceholderTrackingNumber() {
        when(documentRequestRepository.findDeliveringRequestsWithTrackingNo()).thenReturn(List.of(
                Map.of("request_no", "260700046", "tracking_no", "0"),
                Map.of("request_no", "260700047", "tracking_no", "ED363095984TH")
        ));
        when(thailandPostTrackingService.track(List.of("ED363095984TH"))).thenReturn(Map.of(
                "ED363095984TH", Map.of("events", List.of())
        ));

        scheduler.updateDeliveryStatuses();

        verify(thailandPostTrackingService).track(List.of("ED363095984TH"));
        verify(documentRenewalDetailService).markDeliveredIfSuccessful("260700047", Map.of("events", List.of()));
    }

    @Test
        void continuesWhenOneTrackingBatchFails() {
        when(documentRequestRepository.findDeliveringRequestsWithTrackingNo()).thenReturn(List.of(
            Map.of("request_no", "260700046", "tracking_no", "ED363095983TH"),
            Map.of("request_no", "260700047", "tracking_no", "ED363095984TH")
        ));
        doThrow(new RuntimeException("Thailand Post unavailable"))
            .when(thailandPostTrackingService).track(anyList());

        scheduler.updateDeliveryStatuses();

        verify(thailandPostTrackingService).track(List.of("ED363095983TH", "ED363095984TH"));
        verifyNoInteractions(documentRenewalDetailService);
        }

        @Test
        void splitsMoreThanOneHundredTrackingNumbersIntoSeparateBatches() {
        List<Map<String, Object>> deliveries = IntStream.range(0, 101)
            .mapToObj(index -> Map.<String, Object>of(
                "request_no", "request-" + index,
                "tracking_no", "tracking-" + index
            ))
            .collect(Collectors.toList());
        when(documentRequestRepository.findDeliveringRequestsWithTrackingNo()).thenReturn(deliveries);
        List<Integer> batchSizes = new ArrayList<>();
        when(thailandPostTrackingService.track(anyList())).thenAnswer(invocation -> {
            batchSizes.add(((List<?>) invocation.getArgument(0)).size());
            return Map.of();
        });

        scheduler.updateDeliveryStatuses();

        verify(thailandPostTrackingService, times(2)).track(anyList());
        assertEquals(List.of(100, 1), batchSizes);
    }
}
