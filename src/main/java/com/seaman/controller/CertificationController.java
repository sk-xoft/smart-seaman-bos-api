package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.exception.BusinessException;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.CertificationRequest;
import com.seaman.model.request.DocumentCreateRequest;
import com.seaman.model.request.DocumentUpdateRequest;
import com.seaman.model.request.UserMobileRequest;
import com.seaman.model.response.DocumentCreateResponse;
import com.seaman.model.response.PageDocumentResponse;
import com.seaman.model.response.UserModelResponse;
import com.seaman.service.*;
import com.seaman.utils.ObjectValidatorUtils;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Base64;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class CertificationController  extends BaseController{

    private final MessageCodeService messageCodeService;
    private final UserMobileService userMobileService;

    private final CertificationService certificationService;

    @PostMapping(Routes.USER_MOBILE_LIST)
    public ResponseEntity<SuccessResponse<UserModelResponse>> listNews(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody UserMobileRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                userMobileService.listUserMobile(request)
        ).build());
    }

    @PostMapping(Routes.CERT_TYPE_COT)
    public ResponseEntity<SuccessResponse<PageDocumentResponse>> documentListCot(HttpServletRequest httpServletRequest,
                                                                                 @Valid @RequestBody CertificationRequest request
                                                                                 ) {
        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                certificationService.listCot(request, "COT")
        ).build());
    }

    @PostMapping(Routes.CERT_TYPE_DOCUMENT)
    public ResponseEntity<SuccessResponse<PageDocumentResponse>> documentListDocument(HttpServletRequest httpServletRequest,
                                                                                 @Valid @RequestBody CertificationRequest request
    ) {
        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                certificationService.listCot(request, "Document")
        ).build());
    }

    @GetMapping(Routes.CERT_USER_MOBILE_PREVIEW)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> getImage(@RequestParam("certCode") String certCode, @RequestParam("mobileUserUuid") String mobileUserUuid) throws MagicMatchNotFoundException, MagicException, MagicParseException {

        String fileBase64 = certificationService.veiwCertImage(mobileUserUuid, certCode);

        // 1. download img your location...
        byte[] content = Base64.getDecoder().decode(fileBase64);

        MagicMatch match = Magic.getMagicMatch(content);
        String mimeType = match.getMimeType();
        HttpHeaders headers = new HttpHeaders();

        if("image/png".equals(mimeType)) {
            headers.setContentType(MediaType.IMAGE_PNG);
        }

        if("image/jpeg".equals(mimeType)) {
            headers.setContentType(MediaType.IMAGE_JPEG);
        }

        if("application/pdf".equals(mimeType)) {
            headers.setContentType(MediaType.APPLICATION_PDF);
        }

        headers.setContentLength(content.length);

        return new HttpEntity<byte[]>(content, headers);
    }


    @PostMapping(Routes.CREATE_CERT)
    public ResponseEntity<SuccessResponse<DocumentCreateResponse>> documentCreate(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody DocumentCreateRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        // Validate body request
        // This validation format
        if(!ObjectValidatorUtils.verifyDateFormat(request.getCertStartDate())) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, request.getCertStartDate());
        }

        if("9999-99-99".equals(request.getCertEndDate())) {
            // this case is expire.
            request.setCertEndDate(null);
        } else {

            if (!ObjectValidatorUtils.verifyDateFormat(request.getCertEndDate())) {
                throw new BusinessException(AppStatus.INVALID_FORMAT, request.getCertEndDate());
            }
        }

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                certificationService.documentCreate(request)
        ).build());
    }

    @PostMapping(Routes.UPDATE_CERT)
    public ResponseEntity<SuccessResponse<DocumentCreateResponse>> documentUpdate(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody DocumentUpdateRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        // Validate body request
        // This validation format
        if(!ObjectValidatorUtils.verifyDateFormat(request.getCertStartDate())) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, request.getCertStartDate());
        }

        if("9999-99-99".equals(request.getCertEndDate())) {
            // this case is expire.
            request.setCertEndDate(null);
        } else {

            if (!ObjectValidatorUtils.verifyDateFormat(request.getCertEndDate())) {
                throw new BusinessException(AppStatus.INVALID_FORMAT, request.getCertEndDate());
            }
        }

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                certificationService.documentUpdate(request)
        ).build());
    }

    @DeleteMapping(Routes.DELETE_CERT)
    public ResponseEntity<SuccessResponse<DocumentCreateResponse>> documentDelete(
            HttpServletRequest httpServletRequest, @RequestParam("certCode") String certCode, @RequestParam("uUid") String uUid) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                certificationService.documentDelete(certCode, uUid)
        ).build());
    }

    @GetMapping(Routes.EDIT_CERT)
    public ResponseEntity<SuccessResponse<DocumentCreateResponse>> documentEdit(
            HttpServletRequest httpServletRequest, @RequestParam("certCode") String certCode, @RequestParam("uUid") String uUid) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                certificationService.documentEdit(certCode, uUid)
        ).build());
    }

}
