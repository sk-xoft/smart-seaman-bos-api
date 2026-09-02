package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppSys;
import com.seaman.model.request.DocumentDeptResultSaveRequest;
import com.seaman.model.request.DocumentInspectionItemRequest;
import com.seaman.model.request.DocumentInspectionSaveRequest;
import com.seaman.model.request.DocumentPickupActionRequest;
import com.seaman.model.response.DocumentAttachment;
import com.seaman.repository.DocumentRequestRepository;
import com.seaman.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentRequestServiceTest {

    private static final String REQUEST_NO = "260700046";
    private static final String REQUEST_ID = "request-id";
    private static final String MOBILE_USER_UUID = "mobile-user-uuid";

    @Mock
    private DocumentRequestRepository documentRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private AmazonS3 amazonS3;
    @Mock
    private SendNotificationService sendNotificationService;

    private DocumentRequestService service;

    @BeforeEach
    void setUp() {
        service = new DocumentRequestService(
                documentRequestRepository,
                userRepository,
                httpServletRequest,
                amazonS3,
                sendNotificationService
        );
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
        SecurityContextHolder.clearContext();
    }

    @Test
    void inspectionNotificationIncludesAllCompletedDocumentsAfterCommit() {
        DocumentAttachment passport = attachment(1, "สำเนาหนังสือเดินทาง", "pass", "");
        DocumentAttachment certificate = attachment(2, "ใบรับรองแพทย์", "", "");
        DocumentInspectionSaveRequest request = inspectionRequest(2, "fix", "ภาพไม่ชัด");
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("WAITING", MOBILE_USER_UUID));
        when(documentRequestRepository.findDetailItemsByRequestNo(REQUEST_NO))
                .thenReturn(List.of(passport, certificate));
        when(documentRequestRepository.updateInspectionResults(
                org.mockito.ArgumentMatchers.eq(REQUEST_NO), anyList())).thenReturn(1);
        when(documentRequestRepository.areAllRequiredDocumentItemsPassed(REQUEST_NO)).thenReturn(false);
        beginTransactionSynchronization();

        service.saveDocumentRequestInspection(request);

        verify(sendNotificationService, never()).sendNotification(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        commitSynchronization();

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendNotificationService).sendNotification(
                org.mockito.ArgumentMatchers.eq(MOBILE_USER_UUID),
                org.mockito.ArgumentMatchers.eq(AppSys.NOTI_TYPE_DOCUMENT_INSPECTION_RESULT),
                bodyCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(REQUEST_NO),
                org.mockito.ArgumentMatchers.eq("Smart Seaman"),
                org.mockito.ArgumentMatchers.isNull()
        );
        assertTrue(bodyCaptor.getValue().contains("สำเนาหนังสือเดินทาง: ผ่าน"));
        assertTrue(bodyCaptor.getValue().contains("ใบรับรองแพทย์: ไม่ผ่าน - ภาพไม่ชัด"));
    }

    @Test
    void unchangedInspectionDoesNotNotify() {
        DocumentInspectionSaveRequest request = inspectionRequest(1, "pass", "ตรวจสอบแล้ว");
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("WAITING", MOBILE_USER_UUID));
        when(documentRequestRepository.findDetailItemsByRequestNo(REQUEST_NO))
                .thenReturn(List.of(attachment(1, "สำเนาหนังสือเดินทาง", "PASS", "ตรวจสอบแล้ว")));
        when(documentRequestRepository.updateInspectionResults(
                org.mockito.ArgumentMatchers.eq(REQUEST_NO), anyList())).thenReturn(1);
        when(documentRequestRepository.areAllRequiredDocumentItemsPassed(REQUEST_NO)).thenReturn(true);

        service.saveDocumentRequestInspection(request);

        verify(sendNotificationService, never()).sendNotification(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void passInspectionPreservesTrimmedNote() {
        DocumentInspectionSaveRequest request = inspectionRequest(1, "pass", "  ตรวจสอบแล้ว  ");
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("WAITING", MOBILE_USER_UUID));
        when(documentRequestRepository.findDetailItemsByRequestNo(REQUEST_NO))
                .thenReturn(List.of(attachment(1, "สำเนาหนังสือเดินทาง", "PASS", "ตรวจสอบแล้ว")));
        when(documentRequestRepository.updateInspectionResults(
                org.mockito.ArgumentMatchers.eq(REQUEST_NO), anyList())).thenReturn(1);
        when(documentRequestRepository.areAllRequiredDocumentItemsPassed(REQUEST_NO)).thenReturn(true);

        service.saveDocumentRequestInspection(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentInspectionItemRequest>> inspectionsCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(documentRequestRepository).updateInspectionResults(
                org.mockito.ArgumentMatchers.eq(REQUEST_NO), inspectionsCaptor.capture());
        assertEquals("ตรวจสอบแล้ว", inspectionsCaptor.getValue().get(0).getCheckNote());
    }

    @Test
    void inspectionNotificationBodyIsBounded() {
        List<DocumentAttachment> attachments = new java.util.ArrayList<>();
        List<DocumentInspectionItemRequest> inspections = new java.util.ArrayList<>();
        for (int index = 1; index <= 100; index++) {
            attachments.add(attachment(index, "เอกสารประกอบคำขอชื่อยาวมากหมายเลข " + index, "", ""));
            DocumentInspectionItemRequest item = new DocumentInspectionItemRequest();
            item.setSortOrder(index);
            item.setCheckResult("fix");
            item.setCheckNote("กรุณาอัปโหลดเอกสารใหม่เนื่องจากรายละเอียดไม่ชัดเจน");
            inspections.add(item);
        }
        DocumentInspectionSaveRequest request = new DocumentInspectionSaveRequest();
        request.setRequestNo(REQUEST_NO);
        request.setInspections(inspections);
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("WAITING", MOBILE_USER_UUID));
        when(documentRequestRepository.findDetailItemsByRequestNo(REQUEST_NO)).thenReturn(attachments);
        when(documentRequestRepository.updateInspectionResults(
                org.mockito.ArgumentMatchers.eq(REQUEST_NO), anyList())).thenReturn(100);
        when(documentRequestRepository.areAllRequiredDocumentItemsPassed(REQUEST_NO)).thenReturn(false);
        beginTransactionSynchronization();

        service.saveDocumentRequestInspection(request);
        commitSynchronization();

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendNotificationService).sendNotification(
                org.mockito.ArgumentMatchers.eq(MOBILE_USER_UUID),
                org.mockito.ArgumentMatchers.eq(AppSys.NOTI_TYPE_DOCUMENT_INSPECTION_RESULT),
                bodyCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(REQUEST_NO),
                org.mockito.ArgumentMatchers.eq("Smart Seaman"),
                org.mockito.ArgumentMatchers.isNull()
        );
        assertTrue(bodyCaptor.getValue().length() <= 1500);
        assertTrue(bodyCaptor.getValue().endsWith("เปิดแอปเพื่อดูรายละเอียดทั้งหมด"));
    }

    @Test
    void departmentResultNotificationIsSentAfterCommitWithoutDate() {
        DocumentDeptResultSaveRequest request = new DocumentDeptResultSaveRequest();
        request.setRequestNo(REQUEST_NO);
        request.setAvailablePickupDate("2026-09-01");
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("PENDING_DEPARTMENT_RESULT", MOBILE_USER_UUID));
        when(documentRequestRepository.findDocumentStatusIdByThaiName("รอรับเอกสารจากกรม"))
                .thenReturn("PENDING_DEPARTMENT_PICKUP");
        when(documentRequestRepository.findLatestDepartmentResultInfo(REQUEST_ID)).thenReturn(null);
        when(documentRequestRepository.updateDocumentRequestStatus(
                REQUEST_NO, "PENDING_DEPARTMENT_PICKUP", false)).thenReturn(1);
        beginTransactionSynchronization();

        service.saveDocumentDeptResult(request);

        verify(sendNotificationService, never()).sendNotification(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        commitSynchronization();
        verify(sendNotificationService).sendNotification(
                MOBILE_USER_UUID,
                AppSys.NOTI_TYPE_DOCUMENT_PENDING_DEPARTMENT_PICKUP,
                "รอรับเอกสารจากกรมเจ้าท่า",
                REQUEST_NO,
                "Smart Seaman",
                null
        );
    }

    @Test
    void unchangedDepartmentResultDoesNotNotify() {
        DocumentDeptResultSaveRequest request = new DocumentDeptResultSaveRequest();
        request.setRequestNo(REQUEST_NO);
        request.setAvailablePickupDate("2026-09-01");
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("PENDING_DEPARTMENT_PICKUP", MOBILE_USER_UUID));
        when(documentRequestRepository.findDocumentStatusIdByThaiName("รอรับเอกสารจากกรม"))
                .thenReturn("PENDING_DEPARTMENT_PICKUP");
        when(documentRequestRepository.findLatestDepartmentResultInfo(REQUEST_ID))
                .thenReturn(Map.of("note", "2026-09-01"));
        when(documentRequestRepository.updateDocumentRequestStatus(
                REQUEST_NO, "PENDING_DEPARTMENT_PICKUP", false)).thenReturn(1);

        service.saveDocumentDeptResult(request);

        verify(sendNotificationService, never()).sendNotification(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void deliveryNotificationIncludesTrackingNumberAfterCommit() {
        DocumentPickupActionRequest request = deliveryRequest();
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("PENDING_DELIVERY", MOBILE_USER_UUID));
        when(documentRequestRepository.findLatestDeliveryInfo(REQUEST_ID)).thenReturn(null);
        when(documentRequestRepository.upsertDeliveryInfoByRequestNo(
                REQUEST_NO, "ED363095983TH", "2026-09-01", "00000000-0000-0000-0000-000000000000"))
                .thenReturn(1);
        when(documentRequestRepository.findDocumentStatusIdByThaiName("กำลังจัดส่ง"))
                .thenReturn("DELIVERING");
        when(documentRequestRepository.updateDocumentRequestStatus(REQUEST_NO, "DELIVERING", false))
                .thenReturn(1);
        beginTransactionSynchronization();

        service.saveDocumentPickupAction(request);

        verify(sendNotificationService, never()).sendNotification(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        commitSynchronization();
        verify(sendNotificationService).sendNotification(
                MOBILE_USER_UUID,
                AppSys.NOTI_TYPE_DOCUMENT_DELIVERY_STARTED,
                "เอกสารของคุณอยู่ระหว่างจัดส่ง\nเลขพัสดุ: ED363095983TH",
                REQUEST_NO,
                "Smart Seaman",
                "ED363095983TH"
        );
    }

    @Test
    void unchangedDeliveryDoesNotNotify() {
        DocumentPickupActionRequest request = deliveryRequest();
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("DELIVERING", MOBILE_USER_UUID));
        when(documentRequestRepository.findLatestDeliveryInfo(REQUEST_ID)).thenReturn(Map.of(
                "tracking_no", "ED363095983TH",
                "shipped_date", Date.valueOf("2026-09-01")
        ));
        when(documentRequestRepository.upsertDeliveryInfoByRequestNo(
                REQUEST_NO, "ED363095983TH", "2026-09-01", "00000000-0000-0000-0000-000000000000"))
                .thenReturn(1);
        when(documentRequestRepository.findDocumentStatusIdByThaiName("กำลังจัดส่ง"))
                .thenReturn("DELIVERING");
        when(documentRequestRepository.updateDocumentRequestStatus(REQUEST_NO, "DELIVERING", false))
                .thenReturn(1);

        service.saveDocumentPickupAction(request);

        verify(sendNotificationService, never()).sendNotification(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void rolledBackDepartmentResultDoesNotNotify() {
        DocumentDeptResultSaveRequest request = new DocumentDeptResultSaveRequest();
        request.setRequestNo(REQUEST_NO);
        request.setAvailablePickupDate("2026-09-01");
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("PENDING_DEPARTMENT_RESULT", MOBILE_USER_UUID));
        when(documentRequestRepository.findDocumentStatusIdByThaiName("รอรับเอกสารจากกรม"))
                .thenReturn("PENDING_DEPARTMENT_PICKUP");
        when(documentRequestRepository.findLatestDepartmentResultInfo(REQUEST_ID)).thenReturn(null);
        when(documentRequestRepository.updateDocumentRequestStatus(
                REQUEST_NO, "PENDING_DEPARTMENT_PICKUP", false)).thenReturn(1);
        beginTransactionSynchronization();

        service.saveDocumentDeptResult(request);
        rollbackSynchronization();

        verify(sendNotificationService, never()).sendNotification(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void missingMobileUserUuidDoesNotNotify() {
        DocumentInspectionSaveRequest request = inspectionRequest(1, "pass", "");
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("WAITING", null));
        when(documentRequestRepository.findDetailItemsByRequestNo(REQUEST_NO))
                .thenReturn(List.of(attachment(1, "สำเนาหนังสือเดินทาง", "", "")));
        when(documentRequestRepository.updateInspectionResults(
                org.mockito.ArgumentMatchers.eq(REQUEST_NO), anyList())).thenReturn(1);
        when(documentRequestRepository.areAllRequiredDocumentItemsPassed(REQUEST_NO)).thenReturn(true);

        service.saveDocumentRequestInspection(request);

        verify(sendNotificationService, never()).sendNotification(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendbackNotifiesCorrectionDetailsAfterCommit() {
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("PENDING_DOCUMENT_REVIEW", MOBILE_USER_UUID));
        when(documentRequestRepository.findDocumentStatusIdByThaiName("รอผู้ยื่นแก้ไข"))
                .thenReturn("PENDING_CORRECTION");
        when(documentRequestRepository.updateDocumentRequestStatus(
                REQUEST_NO, "PENDING_CORRECTION", true)).thenReturn(1);
        when(documentRequestRepository.findDetailItemsByRequestNo(REQUEST_NO)).thenReturn(List.of(
                attachment(1, "สำเนาหนังสือเดินทาง", "pass", ""),
                attachment(2, "ใบรับรองแพทย์", "fix", "ภาพไม่ชัด")
        ));
        beginTransactionSynchronization();

        service.updateDocumentRequestStatus(REQUEST_NO, "sendback");

        verify(sendNotificationService, never()).sendNotification(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        commitSynchronization();
        verify(sendNotificationService).sendNotification(
                MOBILE_USER_UUID,
                AppSys.NOTI_TYPE_DOCUMENT_REQUEST_CORRECTION_REQUIRED,
                "กรุณาแก้ไขเอกสารคำขอ " + REQUEST_NO + "\nใบรับรองแพทย์: ภาพไม่ชัด",
                REQUEST_NO,
                "Smart Seaman",
                null
        );
    }

    @Test
    void cancellationNotifiesAfterCommit() {
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("PENDING_DOCUMENT_REVIEW", MOBILE_USER_UUID));
        when(documentRequestRepository.findDocumentStatusIdByThaiName("ยกเลิก"))
                .thenReturn("CANCELLED");
        when(documentRequestRepository.updateDocumentRequestStatus(
                REQUEST_NO, "CANCELLED", true)).thenReturn(1);
        beginTransactionSynchronization();

        service.updateDocumentRequestStatus(REQUEST_NO, "cancel");

        verify(sendNotificationService, never()).sendNotification(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        commitSynchronization();
        verify(sendNotificationService).sendNotification(
                MOBILE_USER_UUID,
                AppSys.NOTI_TYPE_DOCUMENT_REQUEST_CANCELLED,
                "คำขอต่ออายุเอกสาร " + REQUEST_NO + " ถูกยกเลิกแล้ว",
                REQUEST_NO,
                "Smart Seaman",
                null
        );
    }

    @Test
    void repeatedCancellationDoesNotNotify() {
        when(documentRequestRepository.findDocumentRequestSummaryByRequestNo(REQUEST_NO))
                .thenReturn(summary("CANCELLED", MOBILE_USER_UUID));
        when(documentRequestRepository.findDocumentStatusIdByThaiName("ยกเลิก"))
                .thenReturn("CANCELLED");
        when(documentRequestRepository.updateDocumentRequestStatus(
                REQUEST_NO, "CANCELLED", true)).thenReturn(1);

        service.updateDocumentRequestStatus(REQUEST_NO, "cancel");

        verify(sendNotificationService, never()).sendNotification(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    private Map<String, Object> summary(String statusId, String mobileUserUuid) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("request_id", REQUEST_ID);
        summary.put("request_no", REQUEST_NO);
        summary.put("document_status_id", statusId);
        summary.put("document_status_name_th", "สถานะ");
        summary.put("mobile_user_uuid", mobileUserUuid);
        return summary;
    }

    private DocumentAttachment attachment(Integer sortOrder, String name, String result, String note) {
        DocumentAttachment attachment = new DocumentAttachment();
        attachment.setSortOrder(sortOrder);
        attachment.setDocumentName(name);
        attachment.setCheckResult(result);
        attachment.setCheckNote(note);
        return attachment;
    }

    private DocumentInspectionSaveRequest inspectionRequest(Integer sortOrder, String result, String note) {
        DocumentInspectionItemRequest item = new DocumentInspectionItemRequest();
        item.setSortOrder(sortOrder);
        item.setCheckResult(result);
        item.setCheckNote(note);
        DocumentInspectionSaveRequest request = new DocumentInspectionSaveRequest();
        request.setRequestNo(REQUEST_NO);
        request.setInspections(List.of(item));
        return request;
    }

    private DocumentPickupActionRequest deliveryRequest() {
        DocumentPickupActionRequest request = new DocumentPickupActionRequest();
        request.setRequestNo(REQUEST_NO);
        request.setAction("save_delivery_info");
        request.setTrackingNo("ED363095983TH");
        request.setShippedDate("2026-09-01");
        return request;
    }

    private void beginTransactionSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    private void commitSynchronization() {
        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        synchronizations.forEach(TransactionSynchronization::afterCommit);
        synchronizations.forEach(synchronization ->
                synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    private void rollbackSynchronization() {
        TransactionSynchronizationManager.getSynchronizations().forEach(synchronization ->
                synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }
}