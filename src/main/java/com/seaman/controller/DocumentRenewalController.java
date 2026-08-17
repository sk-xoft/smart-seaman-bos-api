package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.DocumentRenewalDetailResponse;
import com.seaman.service.DocumentRenewalDetailService;
import com.seaman.service.MessageCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Document Renewals", description = "Document renewal APIs")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class DocumentRenewalController extends BaseController {

    private final DocumentRenewalDetailService detailService;
    private final MessageCodeService messageCodeService;

    @Operation(summary = "Get the current user's document renewal request detail")
    @GetMapping(Routes.DOCUMENT_RENEWAL_DETAIL)
    public ResponseEntity<SuccessResponse<DocumentRenewalDetailResponse>> detail(
            HttpServletRequest request, @PathVariable String requestNo) {
        return ok(success(request, detailService.detail(requestNo)));
    }

    private <T> SuccessResponse<T> success(HttpServletRequest request, T data) {
        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE, (String) request.getAttribute(AppSys.LANGUAGE));
        return SuccessResponse.<T>builder(AppStatus.SUCCESS_CODE, description, data).build();
    }
}
