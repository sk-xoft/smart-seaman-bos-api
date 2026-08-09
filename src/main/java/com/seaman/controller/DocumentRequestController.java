package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.DocumentDeptResultSaveRequest;
import com.seaman.model.request.DocumentInspectionSaveRequest;
import com.seaman.model.request.DocumentPickupActionRequest;
import com.seaman.model.response.DocumentRequestDetailRs;
import com.seaman.model.response.DocumentRequestRs;
import com.seaman.service.DocumentRequestService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class DocumentRequestController extends BaseController {

    private final DocumentRequestService documentRequestService;
    private final MessageCodeService messageCodeService;

    @GetMapping(Routes.DOCUMENT_REQUEST)
    public ResponseEntity<SuccessResponse<DocumentRequestRs>> getDocumentRequest(
            HttpServletRequest httpServletRequest,
            @RequestParam("size") Integer size,
            @RequestParam("lastNum") Integer lastNum,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "mobile_user_smart_seaman_id", required = false) String mobileUserSmartSeamanId,
            @RequestParam(value = "mobile_user_first_name", required = false) String mobileUserFirstName,
            @RequestParam(value = "requestNo", required = false) String requestNo
    ) {

        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE)
        );

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
            documentRequestService.getAllDocumentRequest(size, lastNum, status, mobileUserSmartSeamanId, mobileUserFirstName, requestNo)
        ).build());
    }

    @GetMapping(Routes.DOCUMENT_REQUEST_DETAIL)
    public ResponseEntity<SuccessResponse<DocumentRequestDetailRs>> getDocumentRequestDetail(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "requestNo") String requestNo
    ) {

        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE)
        );

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
            documentRequestService.getDocumentRequestDetail(requestNo)
        ).build());
    }

    @PostMapping(Routes.DOCUMENT_REQUEST_INSPECTION)
    public ResponseEntity<SuccessResponse<Map<String, Object>>> saveDocumentRequestInspection(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody DocumentInspectionSaveRequest request
    ) {

        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE)
        );

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentRequestService.saveDocumentRequestInspection(request)
        ).build());
    }

    @PostMapping(Routes.DOCUMENT_REQUEST_ACTION + "/{requestNo}/{action}")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> updateDocumentRequestStatus(
            HttpServletRequest httpServletRequest,
            @PathVariable("requestNo") String requestNo,
            @PathVariable("action") String action
    ) {

        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE)
        );

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentRequestService.updateDocumentRequestStatus(requestNo, action)
        ).build());
    }

    @PostMapping(Routes.DOCUMENT_REQUEST_DEPT_RESULT)
    public ResponseEntity<SuccessResponse<Map<String, Object>>> saveDocumentDeptResult(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody DocumentDeptResultSaveRequest request
    ) {

        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE)
        );

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentRequestService.saveDocumentDeptResult(request)
        ).build());
    }

    @PostMapping(Routes.DOCUMENT_REQUEST_PICKUP_ACTION)
    public ResponseEntity<SuccessResponse<Map<String, Object>>> saveDocumentPickupAction(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody DocumentPickupActionRequest request
    ) {

        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE)
        );

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentRequestService.saveDocumentPickupAction(request)
        ).build());
    }

    @PostMapping(value = Routes.DOCUMENT_REQUEST_ATTACHMENT_UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<Map<String, Object>>> uploadDocumentRequestAttachment(
            HttpServletRequest httpServletRequest,
            @RequestParam("requestNo") String requestNo,
            @RequestParam("sortOrder") Integer sortOrder,
            @RequestParam("file") MultipartFile file
    ) {

        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE)
        );

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentRequestService.uploadDocumentRequestAttachment(requestNo, sortOrder, file)
        ).build());
    }

        @GetMapping(Routes.DOCUMENT_REQUEST_ATTACHMENT_FILE)
        public ResponseEntity<byte[]> getDocumentRequestAttachmentFile(
                        @RequestParam("requestNo") String requestNo,
                        @RequestParam("sortOrder") Integer sortOrder,
                        @RequestParam(value = "download", required = false, defaultValue = "false") boolean download
        ) {
                Map<String, Object> fileData = documentRequestService.getDocumentRequestAttachmentFile(requestNo, sortOrder);

                byte[] content = (byte[]) fileData.get("content");
                String contentType = fileData.get("contentType") == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : String.valueOf(fileData.get("contentType"));
                String fileName = fileData.get("fileName") == null ? "document" : String.valueOf(fileData.get("fileName"));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setContentDisposition(ContentDisposition.builder(download ? "attachment" : "inline").filename(fileName).build());

                return new ResponseEntity<>(content, headers, HttpStatus.OK);
        }
}
