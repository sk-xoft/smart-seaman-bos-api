package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.entity.VoucherEntity;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.VoucherRequest;
import com.seaman.model.response.VoucherResponse;
import com.seaman.service.MessageCodeService;
import com.seaman.service.VoucherService;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
public class VoucherController extends BaseController {

    private final MessageCodeService messageCodeService;
    private final VoucherService voucherService;

    @PostMapping(Routes.VOUCHERS)
    public ResponseEntity<SuccessResponse<VoucherResponse>> voucherListAll(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody VoucherRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                voucherService.voucherAll(request)
        ).build());
    }


    @PostMapping(Routes.VOUCHERS_CREATE)
    public ResponseEntity<SuccessResponse<VoucherEntity>> voucherCreate(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody VoucherRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                voucherService.insertVoucher(request)
        ).build());
    }

    @DeleteMapping(Routes.VOUCHERS_DELETE)
    public ResponseEntity<SuccessResponse<VoucherEntity>> voucherDelete(
            HttpServletRequest httpServletRequest, @RequestParam("voucherId") String voucherId) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                voucherService.deleteVoucher(voucherId)
        ).build());
    }




//
//    @PostMapping(Routes.VOUCHERS_UPDATE)
//    public ResponseEntity<SuccessResponse<NewsRs>> updateCourse(
//            HttpServletRequest httpServletRequest,
//            @Valid @RequestBody NewsRq request) {
//
//        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
//
//        return ok(SuccessResponse.builder(
//                AppStatus.SUCCESS_CODE,
//                description,
//                null
//                // newsService.updateNews(request)
//        ).build());
//    }
//
//    @PostMapping(Routes.VOUCHERS_DELETE)
//    public ResponseEntity<SuccessResponse<NewsRs>> deleteCourse(
//            HttpServletRequest httpServletRequest,
//            @Valid @RequestBody NewsRq request) {
//
//        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
//
//        return ok(SuccessResponse.builder(
//                AppStatus.SUCCESS_CODE,
//                description,
//                null
//                // newsService.deleteNews(request.getNewsId())
//        ).build());
//    }
//
    @GetMapping(Routes.VOUCHERS_BY_ID)
    public ResponseEntity<SuccessResponse<VoucherEntity>> listVoucherById(HttpServletRequest httpServletRequest, @PathVariable String id) {
        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                voucherService

        ).build());
    }


//    @GetMapping(Routes.PREVIEW_NEWS)
//    @ResponseStatus(HttpStatus.OK)
//    public HttpEntity<byte[]> previewNews(@PathVariable String id) throws MagicMatchNotFoundException, MagicException, MagicParseException {
//
//        String fileBase64 = newsService.previewNews(id);
//
//        // 1. download img your location...
//        byte[] content = Base64.getDecoder().decode(fileBase64);
//
//        MagicMatch match = Magic.getMagicMatch(content);
//        String mimeType = match.getMimeType();
//        HttpHeaders headers = new HttpHeaders();
//
//        if("image/png".equals(mimeType)) {
//            headers.setContentType(MediaType.IMAGE_PNG);
//        }
//
//        if("image/jpeg".equals(mimeType)) {
//            headers.setContentType(MediaType.IMAGE_JPEG);
//        }
//
//        if("application/pdf".equals(mimeType)) {
//            headers.setContentType(MediaType.APPLICATION_PDF);
//        }
//
//        headers.setContentLength(content.length);
//
//        return new HttpEntity<byte[]>(content, headers);
//    }

}
