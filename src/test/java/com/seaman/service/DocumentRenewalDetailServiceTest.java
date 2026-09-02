package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.model.response.DocumentRenewalDetailResponse;
import com.seaman.repository.DocumentRenewalRepository;
import com.seaman.repository.DocumentRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static com.seaman.constant.AppSys.NOTI_TYPE_DOCUMENT_RENEWAL_DELIVERED;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalDetailServiceTest {

    @Mock
    private DocumentRenewalRepository repository;
    @Mock
    private DocumentRequestRepository documentRequestRepository;
    @Mock
    private ProfileService profileService;
    @Mock
    private ThailandPostTrackingService thailandPostTrackingService;
    @Mock
        private SendNotificationService sendNotificationService;
        @Mock
    private AmazonS3 amazonS3;

    private DocumentRenewalDetailService service;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalDetailService(
                repository,
                documentRequestRepository,
                profileService,
                thailandPostTrackingService,
                sendNotificationService,
                amazonS3
        );
    }

        @Test
        void detailMapsLatestResubmissionSeparatelyFromOriginalSubmission() {
                String requestNo = "260700046";
                String requestId = "request-id";
                Map<String, Object> row = new HashMap<>();
                row.put("id", requestId);
                row.put("request_no", requestNo);
                row.put("document_status_code", "PENDING_DOCUMENT_REVIEW");
                row.put("is_resubmit", 1);
                row.put("submitted_at", Timestamp.valueOf("2026-05-01 08:30:00"));
                row.put("resubmitted_at", Timestamp.valueOf("2026-05-03 09:00:00"));
                row.put("cancelled_at", Timestamp.valueOf("2026-05-04 10:15:00"));
                when(repository.findByRequestNo(requestNo)).thenReturn(row);
                when(repository.findItemsByRequestId(requestId, null)).thenReturn(List.of());
                when(repository.findFilesByRequestId(requestId)).thenReturn(List.of());

                DocumentRenewalDetailResponse response = service.detail(requestNo);

                assertEquals("01/05/2026 08:30", response.getSubmittedAt());
                assertEquals("03/05/2026 09:00", response.getResubmittedAt());
                assertEquals("04/05/2026 10:15", response.getCancelledAt());
                assertEquals(true, response.getIsResubmit());
        }

        @Test
        void detailLeavesResubmissionTimestampNullWhenRequestWasNeverResubmitted() {
                String requestNo = "260700047";
                String requestId = "request-id-2";
                Map<String, Object> row = new HashMap<>();
                row.put("id", requestId);
                row.put("request_no", requestNo);
                row.put("document_status_code", "PENDING_DOCUMENT_REVIEW");
                row.put("is_resubmit", 0);
                row.put("submitted_at", Timestamp.valueOf("2026-05-01 08:30:00"));
                when(repository.findByRequestNo(requestNo)).thenReturn(row);
                when(repository.findItemsByRequestId(requestId, null)).thenReturn(List.of());
                when(repository.findFilesByRequestId(requestId)).thenReturn(List.of());

                DocumentRenewalDetailResponse response = service.detail(requestNo);

                assertNull(response.getResubmittedAt());
        }

        @Test
        void detailIncludesNoteForPassedInspectionItem() {
                String requestNo = "MOCK-BOS-260902-02";
                String requestId = "request-id-pass-note";
                Map<String, Object> row = new HashMap<>();
                row.put("id", requestId);
                row.put("request_no", requestNo);
                row.put("document_code", "DOC007");
                row.put("document_status_code", "PENDING_DOCUMENT_REVIEW");
                Map<String, Object> item = new HashMap<>();
                item.put("id", "item-id");
                item.put("document_master_request_item_code", "MRI001");
                item.put("document_master_items_name", "สำเนาบัตรประชาชน");
                item.put("approve_status", "PASS");
                item.put("check_note", "กก");
                item.put("sort_order", 1);
                when(repository.findByRequestNo(requestNo)).thenReturn(row);
                when(repository.findItemsByRequestId(requestId, "DOC007")).thenReturn(List.of(item));
                when(repository.findFilesByRequestId(requestId)).thenReturn(List.of());

                DocumentRenewalDetailResponse response = service.detail(requestNo);

                assertEquals("pass", response.getItems().get(0).getCheckResult());
                assertEquals("กก", response.getItems().get(0).getCheckNote());
        }

        @Test
        void detailDoesNotApplyBangkokOffsetTwiceToCancellationDatetime() {
                String requestNo = "260700048";
                String requestId = "request-id-3";
                Map<String, Object> row = new HashMap<>();
                row.put("id", requestId);
                row.put("request_no", requestNo);
                row.put("document_status_code", "CANCELLED");
                row.put("cancelled_at", LocalDateTime.of(2026, 9, 1, 23, 41));
                when(repository.findByRequestNo(requestNo)).thenReturn(row);
                when(repository.findItemsByRequestId(requestId, null)).thenReturn(List.of());
                when(repository.findFilesByRequestId(requestId)).thenReturn(List.of());

                DocumentRenewalDetailResponse response = service.detail(requestNo);

                assertEquals("01/09/2026 23:41", response.getCancelledAt());
        }

        @Test
        void detailUsesSelectedShipmentDatetimeFromTransactionNote() {
                String requestNo = "260700049";
                String requestId = "request-id-delivery-time";
                Map<String, Object> row = new HashMap<>();
                row.put("id", requestId);
                row.put("request_no", requestNo);
                row.put("document_status_code", "DELIVERING");
                when(repository.findByRequestNo(requestNo)).thenReturn(row);
                when(repository.findItemsByRequestId(requestId, null)).thenReturn(List.of());
                when(repository.findFilesByRequestId(requestId)).thenReturn(List.of());
                when(repository.findDeliveryByRequestId(requestId)).thenReturn(Map.of(
                        "tracking_no", "ED363095983TH",
                        "shipped_date", Date.valueOf("2026-09-01")
                ));
                when(documentRequestRepository.findLatestDeliveryTransactionInfo(requestId)).thenReturn(Map.of(
                        "note", "trackingNo=ED363095983TH; shippedDate=2026-09-01 14:35:00"
                ));

                DocumentRenewalDetailResponse response = service.detail(requestNo);

                assertEquals("2026-09-01 14:35:00", response.getDelivery().getShippedDate());
                assertEquals("2026-09-01 14:35:00", response.getDelivery().getShippedDateValue());
        }

    @Test
    void trackingMarksRequestDeliveredWhenThailandPostReportsSuccessfulDelivery() {
        String requestNo = "260700046";
        String requestId = "request-id";
        when(repository.findByRequestNo(requestNo)).thenReturn(Map.of(
                "id", requestId,
                "document_status_code", "DELIVERING",
                "mobile_user_uuid", "mobile-user-uuid"
        ));
        when(repository.findDeliveryByRequestId(requestId)).thenReturn(Map.of(
                "tracking_no", "ED363095983TH"
        ));
        when(thailandPostTrackingService.track("ED363095983TH")).thenReturn(Map.of(
                "trackingNo", "ED363095983TH",
                "events", List.of(Map.of(
                        "status", "นำจ่ายสำเร็จ",
                        "statusCode", "501"
                ))
        ));
        when(documentRequestRepository.findDocumentStatusIdByCode("DELIVERED"))
                .thenReturn("delivered-status-id");
        when(documentRequestRepository.markDocumentRequestDeliveredIfDelivering(
                requestNo,
                "delivered-status-id"
        )).thenReturn(1);

        service.tracking(requestNo);

        verify(documentRequestRepository).markDocumentRequestDeliveredIfDelivering(
                requestNo,
                "delivered-status-id"
        );
        verify(documentRequestRepository).markDeliveryDeliveredByRequestNo(requestNo);
        verify(sendNotificationService).sendNotification(
                "mobile-user-uuid",
                NOTI_TYPE_DOCUMENT_RENEWAL_DELIVERED,
                "เอกสารของคุณจัดส่งสำเร็จแล้ว\nเลขพัสดุ: ED363095983TH",
                requestNo,
                "Smart Seaman",
                "ED363095983TH"
        );
    }

    @Test
    void trackingDoesNotChangeRequestStatusWhileDeliveryIsInTransit() {
        String requestNo = "260700046";
        String requestId = "request-id";
        when(repository.findByRequestNo(requestNo)).thenReturn(Map.of(
                "id", requestId,
                "document_status_code", "DELIVERING"
        ));
        when(repository.findDeliveryByRequestId(requestId)).thenReturn(Map.of(
                "tracking_no", "ED363095983TH"
        ));
        when(thailandPostTrackingService.track("ED363095983TH")).thenReturn(Map.of(
                "events", List.of(Map.of(
                        "status", "อยู่ระหว่างการขนส่ง",
                        "statusCode", "201"
                ))
        ));

        service.tracking(requestNo);

        verify(documentRequestRepository, never()).markDocumentRequestDeliveredIfDelivering(
                requestNo,
                "delivered-status-id"
        );
        verify(documentRequestRepository, never()).markDeliveryDeliveredByRequestNo(requestNo);
        verify(sendNotificationService, never()).sendNotification(
                "mobile-user-uuid",
                NOTI_TYPE_DOCUMENT_RENEWAL_DELIVERED,
                "เอกสารของคุณจัดส่งสำเร็จแล้ว",
                requestNo,
                "Smart Seaman",
                "ED363095983TH"
        );
    }

    @Test
    void doesNotSendDeliveryNotificationWhenAnotherRequestAlreadyUpdatedTheStatus() {
        String requestNo = "260700046";
        Map<String, Object> deliveredResult = Map.of(
                "events", List.of(Map.of("statusCode", "501"))
        );
        when(documentRequestRepository.findDocumentStatusIdByCode("DELIVERED"))
                .thenReturn("delivered-status-id");
        when(documentRequestRepository.markDocumentRequestDeliveredIfDelivering(
                requestNo,
                "delivered-status-id"
        )).thenReturn(0);

        service.markDeliveredIfSuccessful(requestNo, deliveredResult);

        verify(documentRequestRepository, never()).markDeliveryDeliveredByRequestNo(requestNo);
        verify(sendNotificationService, never()).sendNotification(
                "mobile-user-uuid",
                NOTI_TYPE_DOCUMENT_RENEWAL_DELIVERED,
                "เอกสารของคุณจัดส่งสำเร็จแล้ว",
                requestNo,
                "Smart Seaman",
                "ED363095983TH"
        );
    }
}