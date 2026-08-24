package com.seaman.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRenewalDeliveryResponse;
import com.seaman.model.response.DocumentRenewalDeptSubmissionResponse;
import com.seaman.model.response.DocumentRenewalDetailFileResponse;
import com.seaman.model.response.DocumentRenewalDetailItemResponse;
import com.seaman.model.response.DocumentRenewalDetailResponse;
import com.seaman.model.response.DocumentRenewalSummaryStatusResponse;
import com.seaman.repository.DocumentRenewalRepository;
import com.seaman.repository.DocumentRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentRenewalDetailService {

    private final DocumentRenewalRepository repository;
    private final DocumentRequestRepository documentRequestRepository;
    private final ProfileService profileService;
    private final ThailandPostTrackingService thailandPostTrackingService;
    private final AmazonS3 getS3;

    @Value("${object.store.bucket}")
    private String bucketName;

    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(BANGKOK);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Map<String, Integer> STATUS_STEP = Map.of(
            "PAYMENT_PENDING", 1,
            "PENDING_DOCUMENT_REVIEW", 2,
            "PENDING_APPLICANT_CORRECTION", 2,
            "PENDING_MARINE_DEPARTMENT_RESULT", 3,
            "PENDING_DEPARTMENT_DOCUMENT_PICKUP", 4,
            "DELIVERING", 5,
            "DELIVERED", 5
    );

    private static final Set<String> DEPT_SUBMISSION_STATUSES = Set.of(
            "PENDING_MARINE_DEPARTMENT_RESULT",
            "PENDING_DEPARTMENT_DOCUMENT_PICKUP",
            "DELIVERING",
            "DELIVERED"
    );

    private static final Set<String> DELIVERY_STATUSES = Set.of(
            "DELIVERING",
            "DELIVERED"
    );

    public DocumentRenewalDetailResponse detail(String requestNo) {
        Map<String, Object> row = repository.findByRequestNo(requestNo);
        if (row == null) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "Request not found: " + requestNo);
        }

        String requestId = str(row, "id");
        String documentCode = str(row, "document_code");
        String statusCode = str(row, "document_status_code");

        List<Map<String, Object>> itemRows = repository.findItemsByRequestId(requestId, documentCode);
        List<Map<String, Object>> fileRows = repository.findFilesByRequestId(requestId);

        Map<String, List<Map<String, Object>>> filesByItemId = fileRows.stream()
                .collect(Collectors.groupingBy(f -> str(f, "request_item_id")));

        DocumentRenewalSummaryStatusResponse status = new DocumentRenewalSummaryStatusResponse();
        status.setId(str(row, "status_id"));
        status.setDocumentStatusCode(statusCode);
        status.setNameTh(str(row, "status_name_th"));
        status.setNameEn(str(row, "status_name_en"));
        status.setCssColor(str(row, "status_css_color"));
        status.setStep(STATUS_STEP.get(statusCode));

        DocumentRenewalDetailResponse response = new DocumentRenewalDetailResponse();
        response.setRequestId(requestId);
        response.setRequestNo(str(row, "request_no"));
        response.setMobileUserUuid(str(row, "mobile_user_uuid"));
        response.setDocumentCode(documentCode);
        response.setDocumentName(str(row, "document_name_th"));
        response.setStatus(status);
        response.setSubmittedAt(formatDatetime(row.get("submitted_at")));
        response.setAmount((BigDecimal) row.get("amount"));
        response.setIsResubmit(toBoolean(row.get("is_resubmit")));
        response.setProfile(profileService.getProfile(response.getMobileUserUuid()));
        response.setDeliverAddress(documentRequestRepository.ensureDeliveryAddress(response.getMobileUserUuid()));
        response.setItems(itemRows.stream()
                .map(item -> mapItem(item, filesByItemId))
                .collect(Collectors.toList()));

        if (DEPT_SUBMISSION_STATUSES.contains(statusCode)) {
            Map<String, Object> deptRow = documentRequestRepository.findLatestDepartmentSubmissionInfo(requestId);
            if (deptRow != null) response.setDeptSubmission(mapDeptSubmission(deptRow));
            response.setDeptResult(buildDeptResult(requestId));
        }
        if (DELIVERY_STATUSES.contains(statusCode)) {
            Map<String, Object> deliveryRow = repository.findDeliveryByRequestId(requestId);
            if (deliveryRow != null) response.setDelivery(mapDelivery(deliveryRow));
        }

        return response;
    }

    @Transactional
    public Map<String, Object> tracking(String requestNo) {
        Map<String, Object> row = repository.findByRequestNo(requestNo);
        if (row == null) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "Request not found: " + requestNo);
        }

        String statusCode = str(row, "document_status_code");
        if (!DELIVERY_STATUSES.contains(statusCode)) {
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Tracking is not available for this request status.");
        }

        Map<String, Object> delivery = repository.findDeliveryByRequestId(str(row, "id"));
        String trackingNo = delivery == null ? null : str(delivery, "tracking_no");
        if (trackingNo == null || trackingNo.trim().isEmpty()) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "Tracking number not found.");
        }

        Map<String, Object> trackingResult = thailandPostTrackingService.track(trackingNo.trim());
        if (!"DELIVERED".equals(statusCode) && hasSuccessfulDelivery(trackingResult)) {
            String deliveredStatusId = documentRequestRepository.findDocumentStatusIdByCode("DELIVERED");
            if (deliveredStatusId == null) {
                throw new BusinessException(AppStatus.DATA_NOT_FOUND, "Document status DELIVERED not found.");
            }

            documentRequestRepository.updateDocumentRequestStatus(requestNo, deliveredStatusId, false);
            documentRequestRepository.markDeliveryDeliveredByRequestNo(requestNo);
        }

        return trackingResult;
    }

    private boolean hasSuccessfulDelivery(Map<String, Object> trackingResult) {
        Object rawEvents = trackingResult == null ? null : trackingResult.get("events");
        if (!(rawEvents instanceof List)) {
            return false;
        }

        for (Object rawEvent : (List<?>) rawEvents) {
            if (!(rawEvent instanceof Map)) {
                continue;
            }

            Map<?, ?> event = (Map<?, ?>) rawEvent;
            String statusCode = String.valueOf(event.get("statusCode"));
            String status = String.valueOf(event.get("status")).toLowerCase(Locale.ROOT);
            if ("501".equals(statusCode)
                    || status.contains("นำจ่ายสำเร็จ")
                    || status.contains("นำส่งสำเร็จ")
                    || status.contains("จัดส่งสำเร็จ")
                    || status.contains("delivered")) {
                return true;
            }
        }

        return false;
    }

    private DocumentRenewalDetailItemResponse mapItem(
            Map<String, Object> item,
            Map<String, List<Map<String, Object>>> filesByItemId) {

        String itemId = str(item, "id");
        String approveStatus = str(item, "approve_status");
        List<Map<String, Object>> files = filesByItemId.getOrDefault(itemId, List.of());

        DocumentRenewalDetailItemResponse r = new DocumentRenewalDetailItemResponse();
        r.setItemId(itemId);
        r.setDocumentRequestItemCode(str(item, "document_master_request_item_code"));
        r.setStorageScope(str(item, "storage_scope"));
        r.setDocumentName(str(item, "document_master_items_name"));
        r.setSortOrder(toInt(item.get("sort_order")));
        r.setFileUploaded(!files.isEmpty());
        r.setCheckResult(approveStatus == null ? null : approveStatus.toLowerCase());
        r.setCheckNote("FIX".equalsIgnoreCase(approveStatus) ? str(item, "check_note") : null);
        r.setIsUpdated(files.stream().anyMatch(f -> toBoolean(f.get("is_updated"))));
        r.setFiles(files.stream().map(this::mapFile).collect(Collectors.toList()));
        return r;
    }

    private DocumentRenewalDetailFileResponse mapFile(Map<String, Object> f) {
        DocumentRenewalDetailFileResponse r = new DocumentRenewalDetailFileResponse();
        r.setFileId(str(f, "id"));
        r.setDocumentType(str(f, "document_type"));
        r.setSlotCode(str(f, "slot_code"));
        r.setOriginalFileName(str(f, "original_file_name"));
        r.setMimeType(str(f, "mime_type"));
        r.setFileSize(toLong(f.get("file_size")));
        r.setFileUploadedAt(formatDatetime(f.get("file_uploaded_at")));
        r.setFileUrl(buildSignedUrl(str(f, "file_path")));
        r.setIsUpdated(toBoolean(f.get("is_updated")));
        return r;
    }

    private DocumentRenewalDeptSubmissionResponse mapDeptSubmission(Map<String, Object> row) {
        DocumentRenewalDeptSubmissionResponse r = new DocumentRenewalDeptSubmissionResponse();
        r.setAction(str(row, "action"));
        r.setActionedAt(formatDatetime(row.get("actioned_at")));
        r.setActionedBy(str(row, "actioned_by"));
        r.setActionedByUsername(str(row, "actioned_by_username"));
        r.setActionedByFirstName(str(row, "actioned_by_first_name"));
        r.setActionedByLastName(str(row, "actioned_by_last_name"));
        r.setActionedByMobileNumber(str(row, "actioned_by_mobile_number"));
        return r;
    }

    private DocumentRenewalDeliveryResponse mapDelivery(Map<String, Object> row) {
        DocumentRenewalDeliveryResponse r = new DocumentRenewalDeliveryResponse();
        r.setTrackingNo(str(row, "tracking_no"));
        r.setCarrier(str(row, "carrier"));
        r.setShippedDate(formatDate(row.get("shipped_date")));
        r.setShippedDateValue(str(row, "shipped_date"));
        r.setDeliveryStatus(str(row, "delivery_status"));
        r.setShippedRecordedAt(formatDatetime(row.get("shipped_recorded_at")));
        r.setShippedBy(str(row, "shipped_by"));
        r.setShippedByUsername(str(row, "shipped_by_username"));
        r.setShippedByFirstName(str(row, "shipped_by_first_name"));
        r.setShippedByLastName(str(row, "shipped_by_last_name"));
        r.setShippedByMobileNumber(str(row, "shipped_by_mobile_number"));
        r.setDeliveredAt(formatDatetime(row.get("delivered_at")));
        return r;
    }

    private Map<String, Object> buildDeptResult(String requestId) {
        Map<String, Object> resultInfo = documentRequestRepository.findLatestDepartmentResultInfo(requestId);
        Map<String, Object> receivedInfo = documentRequestRepository.findLatestDepartmentReceiveInfo(requestId);
        if (resultInfo == null && receivedInfo == null) {
            return null;
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        if (resultInfo != null) {
            result.put("note", str(resultInfo, "note"));
            result.put("actioned_at", formatDatetime(resultInfo.get("actioned_at")));
            result.put("actioned_by", str(resultInfo, "actioned_by"));
            result.put("actioned_by_username", str(resultInfo, "actioned_by_username"));
            result.put("actioned_by_first_name", str(resultInfo, "actioned_by_first_name"));
            result.put("actioned_by_last_name", str(resultInfo, "actioned_by_last_name"));
            result.put("actioned_by_mobile_number", str(resultInfo, "actioned_by_mobile_number"));
        }
        if (receivedInfo != null) {
            result.put("received_date", str(receivedInfo, "note"));
            result.put("received_actioned_at", formatDatetime(receivedInfo.get("actioned_at")));
            result.put("received_actioned_by", str(receivedInfo, "actioned_by"));
            result.put("received_actioned_by_username", str(receivedInfo, "actioned_by_username"));
            result.put("received_actioned_by_first_name", str(receivedInfo, "actioned_by_first_name"));
            result.put("received_actioned_by_last_name", str(receivedInfo, "actioned_by_last_name"));
            result.put("received_actioned_by_mobile_number", str(receivedInfo, "actioned_by_mobile_number"));
        }
        return result;
    }

    private String buildSignedUrl(String filePath) {
        if (filePath == null) return null;
        try {
            Date expiration = new Date(System.currentTimeMillis() + 10 * 60 * 1000L);
            return getS3.generatePresignedUrl(bucketName, filePath, expiration, HttpMethod.GET).toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String formatDatetime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) {
            return DATETIME_FMT.format(((LocalDateTime) value).atZone(ZoneOffset.UTC));
        }
        if (value instanceof java.sql.Timestamp) {
            return DATETIME_FMT.format(((java.sql.Timestamp) value).toInstant());
        }
        if (value instanceof java.util.Date) {
            return DATETIME_FMT.format(((java.util.Date) value).toInstant());
        }
        return null;
    }

    private String formatDate(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Date) {
            LocalDate ld = ((java.sql.Date) value).toLocalDate();
            return ld.format(DATE_FMT);
        }
        return formatDatetime(value);
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        return false;
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }
}
