package com.seaman.service;

import com.seaman.repository.DocumentRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "document.delivery.tracking", name = "enabled", havingValue = "true")
public class DocumentDeliveryTrackingScheduler {

    private static final Logger log = LoggerFactory.getLogger(DocumentDeliveryTrackingScheduler.class);
    private static final int MAX_TRACKING_NUMBERS_PER_REQUEST = 100;

    private final DocumentRequestRepository documentRequestRepository;
    private final DocumentRenewalDetailService documentRenewalDetailService;
    private final ThailandPostTrackingService thailandPostTrackingService;

    @Scheduled(
            cron = "${document.delivery.tracking.cron}",
            zone = "${document.delivery.tracking.timezone}"
    )
    public void updateDeliveryStatuses() {
        List<Map<String, Object>> deliveries = documentRequestRepository.findDeliveringRequestsWithTrackingNo();
        Map<String, List<String>> requestNosByTrackingNo = groupRequestNosByTrackingNo(deliveries);
        List<String> trackingNos = new ArrayList<>(requestNosByTrackingNo.keySet());
        int checkedCount = 0;

        log.info("Thailand Post delivery tracking started: {} delivery request(s), {} unique tracking number(s)",
            deliveries.size(), trackingNos.size());
        log.info("Thailand Post delivery tracking loaded from database: {}", trackingNos);

        if (trackingNos.isEmpty()) {
            log.info("Thailand Post delivery tracking finished: no in-transit deliveries with tracking numbers found");
            return;
        }

        for (int start = 0; start < trackingNos.size(); start += MAX_TRACKING_NUMBERS_PER_REQUEST) {
            List<String> batch = trackingNos.subList(start,
                    Math.min(start + MAX_TRACKING_NUMBERS_PER_REQUEST, trackingNos.size()));
            log.info("Thailand Post delivery tracking requesting batch {} with {} tracking number(s): {}",
                (start / MAX_TRACKING_NUMBERS_PER_REQUEST) + 1, batch.size(), batch);
            checkedCount += updateDeliveryStatusBatch(batch, requestNosByTrackingNo);
        }

        log.info("Thailand Post delivery tracking finished: checked {} delivery request(s)", checkedCount);
    }

    private int updateDeliveryStatusBatch(List<String> trackingNos, Map<String, List<String>> requestNosByTrackingNo) {
        try {
            Map<String, Map<String, Object>> trackingResults = thailandPostTrackingService.track(trackingNos);
            int checkedCount = 0;
            for (String trackingNo : trackingNos) {
                Map<String, Object> trackingResult = trackingResults.get(trackingNo);
                for (String requestNo : requestNosByTrackingNo.get(trackingNo)) {
                    documentRenewalDetailService.markDeliveredIfSuccessful(requestNo, trackingResult);
                    checkedCount++;
                }
            }
            log.info("Thailand Post delivery tracking completed batch with {} tracking number(s)", trackingNos.size());
            return checkedCount;
        } catch (Exception ex) {
            log.error("Unable to update Thailand Post tracking for {} tracking number(s)", trackingNos.size(), ex);
            return 0;
        }
    }

    private Map<String, List<String>> groupRequestNosByTrackingNo(List<Map<String, Object>> deliveries) {
        Map<String, List<String>> requestNosByTrackingNo = new LinkedHashMap<>();
        for (Map<String, Object> delivery : deliveries) {
            String requestNo = value(delivery.get("request_no"));
            String trackingNo = value(delivery.get("tracking_no"));
            if (requestNo == null || trackingNo == null) {
                continue;
            }
            if ("0".equals(trackingNo)) {
                log.warn("Skipping document request {} because its tracking number is a placeholder: {}",
                        requestNo, trackingNo);
                continue;
            }

            requestNosByTrackingNo.computeIfAbsent(trackingNo, ignored -> new ArrayList<>()).add(requestNo);
        }
        return requestNosByTrackingNo;
    }

    private String value(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        return String.valueOf(value).trim();
    }
}
