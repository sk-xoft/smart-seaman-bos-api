package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.BannerRq;
import com.seaman.model.response.BannerRs;
import com.seaman.service.BannerService;
import com.seaman.service.MessageCodeService;
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
public class BannerController extends BaseController {

    private final MessageCodeService messageCodeService;
    private final BannerService bannerService;

    @PostMapping(Routes.LIST_BANNER)
    public ResponseEntity<SuccessResponse<BannerRs>> listBanner(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody BannerRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                bannerService.getAllBanner(request)
        ).build());
    }

    @PostMapping(Routes.CREATE_BANNER)
    public ResponseEntity<SuccessResponse<BannerRs>> createBanner(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody BannerRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                bannerService.insertBanner(request)
        ).build());
    }

    @PostMapping(Routes.DELETE_BANNER)
    public ResponseEntity<SuccessResponse<BannerRs>> deleteCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody BannerRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                bannerService.deleteBanner(request.getBannerId())
        ).build());
    }


    @GetMapping(Routes.PREVIEW_BANNER)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> previewBanner(@PathVariable String id) throws MagicMatchNotFoundException, MagicException, MagicParseException {

        String fileBase64 = bannerService.previewBanner(id);

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

}
