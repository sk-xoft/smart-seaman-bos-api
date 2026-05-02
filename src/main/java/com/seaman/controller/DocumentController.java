package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.FormRq;
import com.seaman.model.response.FormRs;
import com.seaman.service.DocumentService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class DocumentController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DocumentService documentService;
    private final MessageCodeService messageCodeService;
    @PostMapping(Routes.LIST_FORM)
    public ResponseEntity<SuccessResponse<FormRs>> login(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody FormRq formRq) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.getAllForm(formRq)
        ).build());
    }

    @PostMapping(Routes.CREATE_FORM)
    public ResponseEntity<SuccessResponse<FormRs>> createCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody FormRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.insertForm(request)
        ).build());
    }

    @PostMapping(Routes.UPDATE_FORM)
    public ResponseEntity<SuccessResponse<FormRs>> updateCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody FormRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.updateCourse(request)
        ).build());
    }

    @PostMapping(Routes.DELETE_FORM)
    public ResponseEntity<SuccessResponse<FormRs>> deleteCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody FormRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.deleteForm(request.getFormId())
        ).build());
    }

    @GetMapping(Routes.LIST_FORM_BY_ID)
    public ResponseEntity<SuccessResponse<FormRs>> listFormById(HttpServletRequest httpServletRequest, @PathVariable String id) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                documentService.getFormById(id)
        ).build());
    }

    @GetMapping(Routes.FORM_DOWNLOAD)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> getImage(HttpServletRequest httpServletRequest, @RequestParam("formId") String formId) throws MagicMatchNotFoundException, MagicException, MagicParseException {

        // 1. download img your location...
        byte[] content = documentService.downloadForm(formId);
        MagicMatch match = Magic.getMagicMatch(content);
        String mimeType = match.getMimeType();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentLength(content.length);
        return new HttpEntity<byte[]>(content, headers);
    }

}
