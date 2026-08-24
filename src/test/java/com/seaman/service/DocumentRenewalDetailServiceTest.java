package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.repository.DocumentRenewalRepository;
import com.seaman.repository.DocumentRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

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
    private AmazonS3 amazonS3;

    private DocumentRenewalDetailService service;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalDetailService(
                repository,
                documentRequestRepository,
                profileService,
                thailandPostTrackingService,
                amazonS3
        );
    }

    @Test
    void trackingMarksRequestDeliveredWhenThailandPostReportsSuccessfulDelivery() {
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
                        "status", "นำจ่ายสำเร็จ",
                        "statusCode", "501"
                ))
        ));
        when(documentRequestRepository.findDocumentStatusIdByCode("DELIVERED"))
                .thenReturn("delivered-status-id");

        service.tracking(requestNo);

        verify(documentRequestRepository).updateDocumentRequestStatus(
                requestNo,
                "delivered-status-id",
                false
        );
        verify(documentRequestRepository).markDeliveryDeliveredByRequestNo(requestNo);
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

        verify(documentRequestRepository, never()).updateDocumentRequestStatus(
                requestNo,
                "delivered-status-id",
                false
        );
        verify(documentRequestRepository, never()).markDeliveryDeliveredByRequestNo(requestNo);
    }
}