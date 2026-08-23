package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.seaman.constant.AppStatus;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.DocumentDeptResultSaveRequest;
import com.seaman.model.request.DocumentInspectionItemRequest;
import com.seaman.model.request.DocumentInspectionSaveRequest;
import com.seaman.model.request.DocumentPickupActionRequest;
import com.seaman.model.response.DocumentAttachment;
import com.seaman.model.response.DocumentRequestDetailRs;
import com.seaman.model.response.DocumentRequestRs;
import com.seaman.model.response.DocumentRequestStepperRs;
import com.seaman.repository.DocumentRequestRepository;
import com.seaman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.Magic;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentRequestService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String SYSTEM_ACTION_UUID = "00000000-0000-0000-0000-000000000000";
    private static final long MAX_ATTACHMENT_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_ATTACHMENT_MIME_TYPES =
            Set.of("image/jpeg", "image/png", "application/pdf");
    private final DocumentRequestRepository documentRequestRepository;
        private final UserRepository userRepository;
    private final HttpServletRequest httpServletRequest;
    private final AmazonS3 getS3;

    @Value("${object.store.bucket}")
    private String bucketName;

    @Value("${object.store.path.root}")
    private String objectRootPath;

    @Value("${object.store.path.request.items}")
    private String requestItemPathTemplate;

    public DocumentRequestRs getAllDocumentRequest(
            Integer size,
            Integer lastNum,
            String status,
            String smartSeamanId,
            String firstName,
            String requestNo
    ) {
        DocumentRequestRs response = new DocumentRequestRs();

        try {
            if (size == null || lastNum == null || size <= 0 || lastNum <= 0) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "{size} and {lastNum} must be greater than 0.");
            }

            Integer startNum = lastNum - size;
            if (startNum < 0) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "{size} can not more than {lastNum}.");
            }

            String statusFilter = status == null ? null : status.trim();
            if (statusFilter != null && statusFilter.isEmpty()) {
                statusFilter = null;
            }

            String smartSeamanIdFilter = smartSeamanId == null ? null : smartSeamanId.trim();
            if (smartSeamanIdFilter != null && smartSeamanIdFilter.isEmpty()) {
                smartSeamanIdFilter = null;
            }

            String firstNameFilter = firstName == null ? null : firstName.trim();
            if (firstNameFilter != null && firstNameFilter.isEmpty()) {
                firstNameFilter = null;
            }

            String requestNoFilter = requestNo == null ? null : requestNo.trim();
            if (requestNoFilter != null && requestNoFilter.isEmpty()) {
                requestNoFilter = null;
            }

            List<Map<String, Object>> items = documentRequestRepository.findAll(
                    startNum,
                    size,
                    statusFilter,
                    smartSeamanIdFilter,
                        firstNameFilter,
                    requestNoFilter
            );
            Integer totalData = documentRequestRepository.countAll(
                    statusFilter,
                    smartSeamanIdFilter,
                        firstNameFilter,
                    requestNoFilter
            );
            List<Map<String, Object>> statusCounts = documentRequestRepository.countByStatus(
                    smartSeamanIdFilter,
                        firstNameFilter,
                    requestNoFilter
            );

            response.setDocumentRequestList(items);
            response.setTotalData(totalData);
            response.setSize(size);
            response.setLastNum(lastNum);
            response.setCountList(items.size());
            response.setStatusCounts(statusCounts);
            log.info("Get all document request is success.");
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }

        return response;
    }

    public DocumentRequestDetailRs getDocumentRequestDetail(String requestNo) {
        try {
            String requestNoFilter = requestNo == null ? null : requestNo.trim();
            if (requestNoFilter != null && requestNoFilter.isEmpty()) {
                requestNoFilter = null;
            }

            if (requestNoFilter == null) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {requestNo}.");
            }

            Map<String, Object> requestSummary = resolveRequestSummary(requestNoFilter);

            String resolvedMobileUserUuid = null;
            if (requestSummary != null) {
                Object summaryMobileUuid = requestSummary.get("mobile_user_uuid");
                if (summaryMobileUuid != null) {
                    resolvedMobileUserUuid = String.valueOf(summaryMobileUuid);
                }
            }

            List<DocumentAttachment> detailItems = documentRequestRepository.findDetailItemsByRequestNo(requestNoFilter);
            DocumentRequestDetailRs response = new DocumentRequestDetailRs();
            response.setDocumentAttachments(detailItems);

            String profileMobileUserUuid = resolvedMobileUserUuid;
            if ((profileMobileUserUuid == null || profileMobileUserUuid.isEmpty()) && !detailItems.isEmpty()) {
                profileMobileUserUuid = detailItems.get(0).getMobileUserUuid();
            }

            if (requestSummary != null) {
                Object summaryRequestId = requestSummary.get("request_id");
                if (summaryRequestId != null) {
                    String requestId = String.valueOf(summaryRequestId);
                    response.setDeptSubmission(documentRequestRepository.findLatestDepartmentSubmissionInfo(requestId));

                    Map<String, Object> deptResult = documentRequestRepository.findLatestDepartmentResultInfo(requestId);
                    Map<String, Object> receivedInfo = documentRequestRepository.findLatestDepartmentReceiveInfo(requestId);
                    if (receivedInfo != null) {
                        if (deptResult == null) {
                            deptResult = new HashMap<>();
                        }
                        deptResult.put("received_date", receivedInfo.get("note"));
                        deptResult.put("received_actioned_at", receivedInfo.get("actioned_at"));
                        deptResult.put("received_actioned_by", receivedInfo.get("actioned_by"));
                        deptResult.put("received_actioned_by_username", receivedInfo.get("actioned_by_username"));
                        deptResult.put("received_actioned_by_first_name", receivedInfo.get("actioned_by_first_name"));
                        deptResult.put("received_actioned_by_last_name", receivedInfo.get("actioned_by_last_name"));
                        deptResult.put("received_actioned_by_mobile_number", receivedInfo.get("actioned_by_mobile_number"));
                    }

                    response.setDeptResult(deptResult);

                    Map<String, Object> deliveryInfo = documentRequestRepository.findLatestDeliveryInfo(requestId);
                    Map<String, Object> deliveryTx = documentRequestRepository.findLatestDeliveryTransactionInfo(requestId);
                    response.setDeliveryInfo(mergeDeliveryInfo(deliveryInfo, deliveryTx));
                }

                Object summaryRequestNo = requestSummary.get("request_no");
                if (summaryRequestNo != null) {
                    response.setRequestNo(String.valueOf(summaryRequestNo));
                }

                Object createdAt = requestSummary.get("created_at");
                Date createdAtDate = toDate(createdAt);
                if (createdAtDate != null) {
                    response.setDateOfSubmission(createdAtDate);
                }

                Object documentName = requestSummary.get("document_name");
                if (documentName != null) {
                    response.setDocumentName(String.valueOf(documentName));
                }

                Map<String, Object> documentStatus = new LinkedHashMap<>();
                documentStatus.put("id", requestSummary.get("document_status_id"));
                documentStatus.put("nameTh", requestSummary.get("document_status_name_th"));
                documentStatus.put("nameEn", requestSummary.get("document_status_name_en"));
                response.setDocumentStatus(documentStatus);

                response.setStepper(buildStepper(
                        requestSummary.get("document_status_id"),
                        requestSummary.get("document_status_name_en"),
                        requestSummary.get("document_status_name_th")
                ));
            }

            if (profileMobileUserUuid != null && !profileMobileUserUuid.isEmpty()) {
                response.setProfile(documentRequestRepository.findMobileUserProfile(profileMobileUserUuid));
                response.setDeliverAddress(documentRequestRepository.ensureDeliveryAddress(profileMobileUserUuid));
            }

            log.info("Get document request detail is success.");
            return response;
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Transactional
    public Map<String, Object> saveDocumentRequestInspection(DocumentInspectionSaveRequest request) {
        try {
            String requestNo = request == null || request.getRequestNo() == null ? null : request.getRequestNo().trim();
            if (requestNo == null || requestNo.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {requestNo}.");
            }

            List<DocumentInspectionItemRequest> inspections = request.getInspections();
            if (inspections == null || inspections.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide inspection list.");
            }

            Map<String, Object> requestSummary = resolveRequestSummary(requestNo);
            if (requestSummary == null) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Document request not found.");
            }

            List<DocumentInspectionItemRequest> normalizedInspections = normalizeInspections(inspections);
            String checkedBy = resolveCheckedBy();

            int updatedRows = documentRequestRepository.updateInspectionResults(requestNo, normalizedInspections);
            if (updatedRows != normalizedInspections.size()) {
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "Can not save all inspection results.");
            }
            boolean allRequiredPassed = documentRequestRepository.areAllRequiredDocumentItemsPassed(requestNo);

            Map<String, Object> response = new HashMap<>();
            response.put("requestNo", requestNo);
            response.put("updatedRows", updatedRows);
            response.put("checkedBy", checkedBy);
            response.put("allRequiredPassed", allRequiredPassed);
            response.put("statusUpdated", false);
            response.put("statusId", requestSummary.get("document_status_id"));
            return response;
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Transactional
    public Map<String, Object> saveDocumentDeptResult(DocumentDeptResultSaveRequest request) {
        try {
            String requestNo = request == null || request.getRequestNo() == null ? null : request.getRequestNo().trim();
            if (requestNo == null || requestNo.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {requestNo}.");
            }

            String availablePickupDate = request.getAvailablePickupDate() == null ? null : request.getAvailablePickupDate().trim();
            if (availablePickupDate == null || availablePickupDate.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {availablePickupDate}.");
            }

            Map<String, Object> requestSummary = resolveRequestSummary(requestNo);
            if (requestSummary == null) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Document request not found.");
            }

            String targetStatusId = documentRequestRepository.findDocumentStatusIdByThaiName("รอรับเอกสารจากกรม");
            if (targetStatusId == null || targetStatusId.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Can not resolve status id for department pickup.");
            }

            String fromStatus = requestSummary.get("document_status_id") == null ? null : String.valueOf(requestSummary.get("document_status_id"));
            int updatedRows = documentRequestRepository.updateDocumentRequestStatus(requestNo, targetStatusId, false);
            if (updatedRows <= 0) {
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "Can not update document request status.");
            }

            String requestId = requestSummary.get("request_id") == null ? null : String.valueOf(requestSummary.get("request_id"));
            String actionedBy = resolveActionedBy();
            if (requestId != null && !requestId.trim().isEmpty()) {
                documentRequestRepository.insertDocumentTransaction(
                        requestId,
                        "RECORD_DEPT_RESULT",
                        fromStatus,
                        targetStatusId,
                        availablePickupDate,
                        actionedBy
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("requestNo", requestNo);
            response.put("availablePickupDate", availablePickupDate);
            response.put("statusId", targetStatusId);
            response.put("statusNameTh", "รอรับเอกสารจากกรม");
            response.put("updatedRows", updatedRows);
            return response;
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Transactional
    public Map<String, Object> updateDocumentRequestStatus(String requestNo, String action) {
        try {
            String requestNoFilter = requestNo == null ? null : requestNo.trim();
            if (requestNoFilter == null || requestNoFilter.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {requestNo}.");
            }

            String actionFilter = action == null ? null : action.trim().toLowerCase(Locale.ROOT);
            if (actionFilter == null || actionFilter.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {action}.");
            }

            Map<String, Object> requestSummary = resolveRequestSummary(requestNoFilter);
            if (requestSummary == null) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Document request not found.");
            }

            String targetStatusLabelTh;
            String transactionAction;
            boolean resetResubmit;
            switch (actionFilter) {
                case "sendback":
                    targetStatusLabelTh = "รอผู้ยื่นแก้ไข";
                    transactionAction = "SEND_BACK";
                    resetResubmit = true;
                    break;
                case "submit":
                    targetStatusLabelTh = "รอผลกรมเจ้าท่า";
                    transactionAction = "SUBMIT_TO_DEPT";
                    resetResubmit = false;
                    break;
                case "cancel":
                    targetStatusLabelTh = "ยกเลิก";
                    transactionAction = "CANCEL";
                    resetResubmit = true;
                    break;
                default:
                    throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Unsupported {action}. Supported: sendback, submit, cancel.");
            }

            String statusId = documentRequestRepository.findDocumentStatusIdByThaiName(targetStatusLabelTh);
            if (statusId == null || statusId.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Can not resolve status id for action: " + actionFilter);
            }

            int updatedRows = documentRequestRepository.updateDocumentRequestStatus(requestNoFilter, statusId, resetResubmit);
            if (updatedRows <= 0) {
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "Can not update document request status.");
            }

            String requestId = requestSummary.get("request_id") == null ? null : String.valueOf(requestSummary.get("request_id"));
            String fromStatus = requestSummary.get("document_status_id") == null ? null : String.valueOf(requestSummary.get("document_status_id"));
            String actionedBy = resolveActionedBy();

            if (requestId != null && !requestId.trim().isEmpty()) {
                documentRequestRepository.insertDocumentTransaction(
                        requestId,
                        transactionAction,
                        fromStatus,
                        statusId,
                        null,
                        actionedBy
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("requestNo", requestNoFilter);
            response.put("action", actionFilter);
            response.put("statusId", statusId);
            response.put("statusNameTh", targetStatusLabelTh);
            response.put("updatedRows", updatedRows);
            return response;
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Transactional
    public Map<String, Object> saveDocumentPickupAction(DocumentPickupActionRequest request) {
        try {
            String requestNo = request == null || request.getRequestNo() == null ? null : request.getRequestNo().trim();
            if (requestNo == null || requestNo.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {requestNo}.");
            }

            String action = request.getAction() == null ? null : request.getAction().trim().toLowerCase(Locale.ROOT);
            if (action == null || action.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {action}.");
            }

            Map<String, Object> requestSummary = resolveRequestSummary(requestNo);
            if (requestSummary == null) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Document request not found.");
            }

            String fromStatus = requestSummary.get("document_status_id") == null ? null : String.valueOf(requestSummary.get("document_status_id"));
            String toStatus = fromStatus;
            String statusNameTh = requestSummary.get("document_status_name_th") == null
                    ? null
                    : String.valueOf(requestSummary.get("document_status_name_th"));
            int updatedRows = 0;

            String note;
            String transactionAction;

            switch (action) {
                case "update_pickup_date":
                    note = request.getAvailablePickupDate() == null ? null : request.getAvailablePickupDate().trim();
                    if (note == null || note.isEmpty()) {
                        throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {availablePickupDate}.");
                    }
                    transactionAction = "RECORD_DEPT_RESULT";
                    break;
                case "receive_doc":
                    note = request.getReceivedDate() == null ? null : request.getReceivedDate().trim();
                    if (note == null || note.isEmpty()) {
                        throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {receivedDate}.");
                    }
                    transactionAction = "RECEIVE_FROM_DEPT";
                    break;
                case "save_delivery_info":
                    String trackingNo = request.getTrackingNo() == null ? null : request.getTrackingNo().trim();
                    String shippedDate = request.getShippedDate() == null ? null : request.getShippedDate().trim();
                    if (trackingNo == null || trackingNo.isEmpty()) {
                        throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {trackingNo}.");
                    }
                    if (shippedDate == null || shippedDate.isEmpty()) {
                        throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {shippedDate}.");
                    }

                    int deliveryRows = documentRequestRepository.upsertDeliveryInfoByRequestNo(
                            requestNo,
                            trackingNo,
                            shippedDate,
                            resolveActionedByOrSystem()
                    );
                    if (deliveryRows <= 0) {
                        throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "Can not save delivery info.");
                    }

                    note = "trackingNo=" + trackingNo + "; shippedDate=" + shippedDate;
                    transactionAction = "RECORD_DELIVERY";

                    String deliveringStatusId = documentRequestRepository.findDocumentStatusIdByThaiName("กำลังจัดส่ง");
                    if (deliveringStatusId == null || deliveringStatusId.isEmpty()) {
                        throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Can not resolve status id for delivering.");
                    }

                    updatedRows = documentRequestRepository.updateDocumentRequestStatus(requestNo, deliveringStatusId, false);
                    if (updatedRows <= 0) {
                        throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "Can not update document request status.");
                    }

                    toStatus = deliveringStatusId;
                    statusNameTh = "กำลังจัดส่ง";
                    break;
                default:
                    throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Unsupported {action}. Supported: update_pickup_date, receive_doc, save_delivery_info.");
            }

            if (toStatus == null || toStatus.trim().isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Can not resolve target status.");
            }

            String requestId = requestSummary.get("request_id") == null ? null : String.valueOf(requestSummary.get("request_id"));
            String actionedBy = resolveActionedBy();
            if (requestId != null && !requestId.trim().isEmpty()) {
                documentRequestRepository.insertDocumentTransaction(
                        requestId,
                        transactionAction,
                        fromStatus,
                        toStatus,
                        note,
                        actionedBy
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("requestNo", requestNo);
            response.put("action", action);
            response.put("statusId", toStatus);
            response.put("statusNameTh", statusNameTh);
            response.put("statusUpdated", "save_delivery_info".equals(action));
            response.put("updatedRows", updatedRows);
            return response;
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Transactional
    public Map<String, Object> uploadDocumentRequestAttachment(String requestNo, Integer sortOrder, MultipartFile file) {
        try {
            String requestNoFilter = requestNo == null ? null : requestNo.trim();
            if (requestNoFilter == null || requestNoFilter.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {requestNo}.");
            }

            if (sortOrder == null || sortOrder <= 0) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide valid {sortOrder}.");
            }

            if (file == null || file.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide upload {file}.");
            }

            String originalFilename = safeOriginalFileName(file.getOriginalFilename());
            byte[] content = validateAttachmentFile(file);
            String mimeType = detectAttachmentMimeType(content);

            Map<String, Object> requestSummary = resolveRequestSummary(requestNoFilter);
            if (requestSummary == null) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Document request not found.");
            }

            Map<String, Object> attachmentTarget = documentRequestRepository.findAttachmentTargetByRequestNoAndSortOrder(requestNoFilter, sortOrder);
            if (attachmentTarget == null) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Attachment target not found for {sortOrder}.");
            }

            String documentName = String.valueOf(attachmentTarget.get("document_name"));
            String requestItemId = String.valueOf(attachmentTarget.get("request_item_id"));
            String documentMasterRequestItemCode = String.valueOf(
                    attachmentTarget.get("document_master_request_item_code")
            );

            String keyName = buildAttachmentObjectKey(requestItemId);
            Map<String, Object> previousAttachment =
                    documentRequestRepository.findLatestUploadedAttachmentByRequestNoAndSortOrder(
                            requestNoFilter,
                            sortOrder
                    );
            Object previousFilePath = previousAttachment == null ? null : previousAttachment.get("file_path");

            uploadToObjectStorage(keyName, content, mimeType);
            registerAttachmentStorageCleanup(
                    keyName,
                    previousFilePath == null ? null : String.valueOf(previousFilePath)
            );

            int affectedRows = documentRequestRepository.upsertRequestItemFile(
                    requestItemId,
                    documentMasterRequestItemCode,
                    sortOrder,
                    keyName,
                    originalFilename,
                    mimeType,
                    content.length
            );

            if (affectedRows <= 0) {
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "Can not save uploaded file path.");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("requestNo", requestNoFilter);
            response.put("sortOrder", sortOrder);
            response.put("filePath", keyName);
            response.put("fileUploaded", true);
            response.put("documentName", documentName);
            return response;
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public Map<String, Object> getDocumentRequestAttachmentFile(String requestNo, Integer sortOrder) {
        try {
            String requestNoFilter = requestNo == null ? null : requestNo.trim();
            if (requestNoFilter == null || requestNoFilter.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide {requestNo}.");
            }

            if (sortOrder == null || sortOrder <= 0) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Please provide valid {sortOrder}.");
            }

            Map<String, Object> attachment = documentRequestRepository.findLatestUploadedAttachmentByRequestNoAndSortOrder(requestNoFilter, sortOrder);
            if (attachment == null) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Attachment file not found.");
            }

            String filePath = attachment.get("file_path") == null ? null : String.valueOf(attachment.get("file_path"));
            if (filePath == null || filePath.trim().isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Attachment file path not found.");
            }

            StoredObjectLocation location = parseStoredObjectLocation(filePath);
                String originalFileName = attachment.get("original_file_name") == null
                    ? null
                    : String.valueOf(attachment.get("original_file_name")).trim();
                String resolvedFileName = originalFileName == null || originalFileName.isEmpty()
                    ? extractFileName(location.key)
                    : originalFileName;
                String storedMimeType = attachment.get("mime_type") == null
                    ? null
                    : String.valueOf(attachment.get("mime_type")).trim();
                String contentType = storedMimeType == null || storedMimeType.isEmpty()
                    ? inferContentType(resolvedFileName)
                    : storedMimeType;

            try (S3Object object = getS3.getObject(location.bucket, location.key);
                 InputStream inputStream = object.getObjectContent()) {

                ObjectMetadata metadata = object.getObjectMetadata();
                if (metadata != null && metadata.getContentType() != null && !metadata.getContentType().trim().isEmpty()) {
                    contentType = metadata.getContentType().trim();
                }

                byte[] content = inputStream.readAllBytes();

                Map<String, Object> response = new HashMap<>();
                response.put("content", content);
                response.put("contentType", contentType);
                response.put("fileName", resolvedFileName);
                return response;
            }
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            log.error("Can not load attachment file: {}", ex.getMessage(), ex);
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Can not load attachment file.");
        }
    }

    private List<DocumentInspectionItemRequest> normalizeInspections(List<DocumentInspectionItemRequest> inspections) {
        List<DocumentInspectionItemRequest> normalizedItems = new ArrayList<>();

        for (DocumentInspectionItemRequest item : inspections) {
            if (item == null || item.getSortOrder() == null) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Each inspection item requires {sortOrder}.");
            }

            String checkResult = item.getCheckResult() == null ? "" : item.getCheckResult().trim().toLowerCase(Locale.ROOT);
            if (!checkResult.isEmpty() && !"pass".equals(checkResult) && !"fix".equals(checkResult)) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "{checkResult} supports only pass/fix.");
            }

            String checkNote = item.getCheckNote() == null ? "" : item.getCheckNote().trim();
            if ("fix".equals(checkResult) && checkNote.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "{checkNote} is required when {checkResult}=fix.");
            }

            DocumentInspectionItemRequest normalized = new DocumentInspectionItemRequest();
            normalized.setSortOrder(item.getSortOrder());
            normalized.setCheckResult(checkResult);
            normalized.setCheckNote("fix".equals(checkResult) ? checkNote : "");
            normalizedItems.add(normalized);
        }

        return normalizedItems;
    }

    private String resolveCheckedBy() {
        UsersEntity user = resolveAuthenticatedUser();
        if (user != null) {
            if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                return user.getUsername().trim();
            }
            if (user.getAdminUuid() != null && !user.getAdminUuid().trim().isEmpty()) {
                return user.getAdminUuid().trim();
            }
        }

        return "SYSTEM";
    }

    private String resolveActionedBy() {
        UsersEntity user = resolveAuthenticatedUser();
        if (user != null) {
            if (user.getAdminUuid() != null && !user.getAdminUuid().trim().isEmpty()) {
                return user.getAdminUuid().trim();
            }
        }

        return null;
    }

    private UsersEntity resolveAuthenticatedUser() {
        Object userObj = httpServletRequest.getAttribute("userObject");
        if (userObj instanceof UsersEntity) {
            return (UsersEntity) userObj;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        if (username == null || username.trim().isEmpty() || "anonymousUser".equals(username)) {
            return null;
        }

        return userRepository.findByUsername(username.trim());
    }

    private String resolveActionedByOrSystem() {
        String actionedBy = resolveActionedBy();
        if (actionedBy == null || actionedBy.trim().isEmpty()) {
            return SYSTEM_ACTION_UUID;
        }
        return actionedBy;
    }

    private Map<String, Object> resolveRequestSummary(String requestNo) {
        return documentRequestRepository.findDocumentRequestSummaryByRequestNo(requestNo);
    }

    private DocumentRequestStepperRs buildStepper(Object statusIdValue, Object statusNameEnValue, Object statusNameThValue) {
        String currentStatusId = statusIdValue == null ? "" : String.valueOf(statusIdValue).trim();
        String currentStatusNameEn = statusNameEnValue == null ? "" : String.valueOf(statusNameEnValue).trim();
        String currentStatusNameTh = statusNameThValue == null ? "" : String.valueOf(statusNameThValue).trim();
        String statusSignal = (currentStatusId + " " + currentStatusNameEn + " " + currentStatusNameTh).toUpperCase(Locale.ROOT);

        List<Map<String, Object>> statusMaster = documentRequestRepository.findDocumentStatusMasterForStepper();
        int currentIndex = findCurrentStatusIndex(statusMaster, currentStatusId, currentStatusNameEn, currentStatusNameTh);

        DocumentRequestStepperRs stepper = new DocumentRequestStepperRs();
        List<Integer> completedSteps = new ArrayList<>();

        int currentStep = currentIndex >= 0 ? currentIndex + 1 : 1;
        if (containsAny(statusSignal, "จัดส่งสำเร็จ", "DELIVERED", "DELIVERY_SUCCESS", "SHIPPING_SUCCESS")) {
            currentStep = 5;
        } else if (containsAny(statusSignal, "กำลังจัดส่ง", "อยู่ระหว่างจัดส่ง", "OUT_FOR_DELIVERY", "IN_TRANSIT", "SHIPPING")) {
            currentStep = 4;
        } else if (containsAny(statusSignal, "รอรับเอกสารจากกรม", "PENDING_DEPARTMENT_PICKUP")) {
            currentStep = 3;
        } else if (containsAny(statusSignal, "รอผลกรมเจ้าท่า", "PENDING_DEPARTMENT_RESULT")) {
            currentStep = 2;
        }

        for (int i = 1; i < currentStep; i++) {
            completedSteps.add(i);
        }

        String baseStatusForCode = !currentStatusId.isEmpty() ? currentStatusId : currentStatusNameEn;
        String statusCode = toUpperSnakeCase(baseStatusForCode);
        if (statusCode.isEmpty()) {
            statusCode = "UNKNOWN";
        }

        boolean isCancelled = containsAny(statusSignal, "CANCEL", "CANCELLED", "ยกเลิก");

        stepper.setStatusCode(statusCode);
        stepper.setCurrentStep(currentStep);
        stepper.setIsCancelled(isCancelled);
        stepper.setCompletedSteps(completedSteps);
        return stepper;
    }

    private int findCurrentStatusIndex(List<Map<String, Object>> statusMaster, String currentStatusId, String currentStatusNameEn, String currentStatusNameTh) {
        if (statusMaster == null || statusMaster.isEmpty()) {
            return -1;
        }

        for (int i = 0; i < statusMaster.size(); i++) {
            Object statusIdObj = statusMaster.get(i).get("status_id");
            if (statusIdObj != null && String.valueOf(statusIdObj).equalsIgnoreCase(currentStatusId)) {
                return i;
            }
        }

        for (int i = 0; i < statusMaster.size(); i++) {
            Object statusNameEnObj = statusMaster.get(i).get("status_name_en");
            if (statusNameEnObj != null && String.valueOf(statusNameEnObj).equalsIgnoreCase(currentStatusNameEn)) {
                return i;
            }
        }

        for (int i = 0; i < statusMaster.size(); i++) {
            Object statusNameThObj = statusMaster.get(i).get("status_name_th");
            if (statusNameThObj != null && String.valueOf(statusNameThObj).equalsIgnoreCase(currentStatusNameTh)) {
                return i;
            }
        }

        return -1;
    }

    private String toUpperSnakeCase(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return "";
        }

        normalized = normalized.replaceAll("([a-z])([A-Z])", "$1_$2");
        normalized = normalized.replaceAll("[^A-Za-z0-9]+", "_");
        normalized = normalized.replaceAll("_+", "_");
        normalized = normalized.replaceAll("^_|_$", "");

        return normalized.toUpperCase(Locale.ROOT);
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Date toDate(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Date) {
            return (Date) value;
        }

        if (value instanceof Timestamp) {
            return new Date(((Timestamp) value).getTime());
        }

        if (value instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) value);
        }

        if (value instanceof String) {
            try {
                return Timestamp.valueOf((String) value);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        return null;
    }

    private Map<String, Object> mergeDeliveryInfo(Map<String, Object> deliveryInfo, Map<String, Object> deliveryTx) {
        if ((deliveryInfo == null || deliveryInfo.isEmpty()) && (deliveryTx == null || deliveryTx.isEmpty())) {
            return null;
        }

        Map<String, Object> merged = new HashMap<>();
        if (deliveryInfo != null) {
            merged.putAll(deliveryInfo);
        }

        if (deliveryTx != null) {
            if (isBlank(merged.get("shipped_recorded_at")) && !isBlank(deliveryTx.get("actioned_at"))) {
                merged.put("shipped_recorded_at", deliveryTx.get("actioned_at"));
            }
            if (isBlank(merged.get("shipped_by")) && !isBlank(deliveryTx.get("actioned_by"))) {
                merged.put("shipped_by", deliveryTx.get("actioned_by"));
            }
            if (isBlank(merged.get("shipped_by_username")) && !isBlank(deliveryTx.get("actioned_by_username"))) {
                merged.put("shipped_by_username", deliveryTx.get("actioned_by_username"));
            }
            if (isBlank(merged.get("shipped_by_first_name")) && !isBlank(deliveryTx.get("actioned_by_first_name"))) {
                merged.put("shipped_by_first_name", deliveryTx.get("actioned_by_first_name"));
            }
            if (isBlank(merged.get("shipped_by_last_name")) && !isBlank(deliveryTx.get("actioned_by_last_name"))) {
                merged.put("shipped_by_last_name", deliveryTx.get("actioned_by_last_name"));
            }
            if (isBlank(merged.get("shipped_by_mobile_number")) && !isBlank(deliveryTx.get("actioned_by_mobile_number"))) {
                merged.put("shipped_by_mobile_number", deliveryTx.get("actioned_by_mobile_number"));
            }

            Map<String, String> parsedNote = parseDeliveryNote(deliveryTx.get("note"));
            if (isBlank(merged.get("tracking_no")) && parsedNote.get("trackingNo") != null) {
                merged.put("tracking_no", parsedNote.get("trackingNo"));
            }
            if (isBlank(merged.get("shipped_date")) && parsedNote.get("shippedDate") != null) {
                merged.put("shipped_date", parsedNote.get("shippedDate"));
            }
        }

        return merged;
    }

    private Map<String, String> parseDeliveryNote(Object noteObj) {
        Map<String, String> parsed = new HashMap<>();
        if (noteObj == null) {
            return parsed;
        }

        String note = String.valueOf(noteObj);
        if (note.trim().isEmpty()) {
            return parsed;
        }

        String[] parts = note.split(";");
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) {
                continue;
            }

            String[] pair = part.split("=", 2);
            if (pair.length != 2) {
                continue;
            }

            String key = pair[0].trim();
            String value = pair[1].trim();
            if (key.isEmpty() || value.isEmpty()) {
                continue;
            }

            if ("trackingNo".equals(key)) {
                parsed.put("trackingNo", value);
            } else if ("shippedDate".equals(key)) {
                parsed.put("shippedDate", value);
            }
        }

        return parsed;
    }

    private boolean isBlank(Object value) {
        return value == null || String.valueOf(value).trim().isEmpty();
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "";
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).trim().toLowerCase(Locale.ROOT);
        return ext.replaceAll("[^a-z0-9]", "");
    }

    private boolean isAllowedAttachmentExtension(String extension) {
        return "pdf".equals(extension)
                || "png".equals(extension)
                || "jpg".equals(extension)
                || "jpeg".equals(extension);
    }

    private String buildAttachmentObjectKey(String requestItemId) {
        return String.format(requestItemPathTemplate, requestItemId, UUID.randomUUID());
    }

    private StoredObjectLocation parseStoredObjectLocation(String storedPath) {
        String normalized = storedPath == null ? "" : storedPath.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.isEmpty()) {
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Invalid attachment file path.");
        }

        String bucketPrefix = bucketName + "/";
        String key = normalized.startsWith(bucketPrefix)
                ? normalized.substring(bucketPrefix.length())
                : normalized;
        if (key.trim().isEmpty()) {
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Invalid attachment file path.");
        }

        return new StoredObjectLocation(bucketName, key);
    }

    private String extractFileName(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            return "document";
        }

        String normalized = objectKey.trim();
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex == -1 || slashIndex == normalized.length() - 1) {
            return normalized;
        }

        return normalized.substring(slashIndex + 1);
    }

    private String inferContentType(String fileName) {
        String extension = extractExtension(fileName);
        if ("pdf".equals(extension)) {
            return "application/pdf";
        }
        if ("png".equals(extension)) {
            return "image/png";
        }
        if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }

    private static class StoredObjectLocation {
        private final String bucket;
        private final String key;

        private StoredObjectLocation(String bucket, String key) {
            this.bucket = bucket;
            this.key = key;
        }
    }

    private byte[] validateAttachmentFile(MultipartFile file) {
        try {
            if (file.getSize() <= 0 || file.getSize() > MAX_ATTACHMENT_FILE_SIZE) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "{file} must not exceed 10 MB.");
            }
            byte[] content = file.getBytes();
            detectAttachmentMimeType(content);
            return content;
        } catch (CommonException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Can not read upload {file}.");
        }
    }

    private String detectAttachmentMimeType(byte[] content) {
        try {
            String mimeType = Magic.getMagicMatch(content).getMimeType().toLowerCase(Locale.ROOT);
            if (!ALLOWED_ATTACHMENT_MIME_TYPES.contains(mimeType)) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "{file} supports only pdf/png/jpg/jpeg.");
            }
            return mimeType;
        } catch (CommonException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "{file} supports only pdf/png/jpg/jpeg.");
        }
    }

    private String safeOriginalFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "file";
        }
        String cleaned = fileName.replace("\\", "_").replace("/", "_").trim();
        return cleaned.length() > 255 ? cleaned.substring(0, 255) : cleaned;
    }

    private void uploadToObjectStorage(String keyName, byte[] content, String mimeType) {
        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(content.length);
            metadata.setContentType(mimeType);

            getS3.putObject(bucketName, keyName, inputStream, metadata);
        } catch (IOException ex) {
            log.error("Read upload file error: {}", ex.getMessage(), ex);
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Can not read upload {file}.");
        } catch (Exception ex) {
            log.error("Object storage upload failed. bucket={}, key={}, message={}", bucketName, keyName, ex.getMessage(), ex);
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "Can not upload file to object storage: " + ex.getMessage());
        }
    }

    private void registerAttachmentStorageCleanup(String newKey, String previousStoredPath) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (previousStoredPath != null && !previousStoredPath.trim().isEmpty()) {
                    StoredObjectLocation previous = parseStoredObjectLocation(previousStoredPath);
                    if (!newKey.equals(previous.key)) {
                        safeDeleteObject(previous.bucket, previous.key);
                    }
                }
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    safeDeleteObject(bucketName, newKey);
                }
            }
        });
    }

    private void safeDeleteObject(String bucket, String key) {
        try {
            getS3.deleteObject(bucket, key);
        } catch (Exception ex) {
            log.warn("Object storage cleanup failed. bucket={}, key={}, message={}", bucket, key, ex.getMessage());
        }
    }
}
