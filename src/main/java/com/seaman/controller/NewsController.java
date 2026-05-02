package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.NewsRq;
import com.seaman.model.request.SendNotificationReq;
import com.seaman.model.response.NewsRs;
import com.seaman.service.MessageCodeService;
import com.seaman.service.NewsService;
import com.seaman.service.SendNotificationService;
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
public class NewsController extends BaseController {

    private final MessageCodeService messageCodeService;
    private final NewsService newsService;
    private final SendNotificationService sendNotificationService;

    @PostMapping(Routes.LIST_NEWS)
    public ResponseEntity<SuccessResponse<NewsRs>> listNews(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody NewsRq newsRq) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                newsService.getAllNews(newsRq)
        ).build());
    }

    @PostMapping(Routes.CREATE_NEWS)
    public ResponseEntity<SuccessResponse<NewsRs>> createNews(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody NewsRq newsRq) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                newsService.insertNews(newsRq)
        ).build());
    }

    @PostMapping(Routes.CREATE_NEWS + "/manual")
    public ResponseEntity<SuccessResponse<NewsRs>> createNewsManual(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody NewsRq newsRq) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                newsService.insertNewsManual(newsRq)
        ).build());
    }


    @PostMapping(Routes.UPDATE_NEWS)
    public ResponseEntity<SuccessResponse<NewsRs>> updateCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody NewsRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                newsService.updateNews(request)
        ).build());
    }

    @PostMapping(Routes.DELETE_NEWS)
    public ResponseEntity<SuccessResponse<NewsRs>> deleteCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody NewsRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                newsService.deleteNews(request.getNewsId())
        ).build());
    }

    @GetMapping(Routes.LIST_NEWS_BY_ID)
    public ResponseEntity<SuccessResponse<NewsRs>> listFormById(HttpServletRequest httpServletRequest, @PathVariable String id) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                newsService.getNewsById(id)
        ).build());
    }


    /**
     * ใช้สำหรับการทดสอบการส่ง notification เป็นการส่ง manual
     * @param httpServletRequest
     * @param req
     * @return
     */
    @PostMapping(Routes.SEND_NOTI_NEWS)
    public ResponseEntity<SuccessResponse<String>> sendNotiNews(
            HttpServletRequest httpServletRequest,
            @RequestBody SendNotificationReq req
            ) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

//         sendNotificationService.senderFcmNews(AppSys.NOTI_TYPE_NEWS_GENERAL,"24",  "แจ้งเตื่อน !", "Test send noti form space.");
        sendNotificationService.sendNotificationManual(req);
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                "success"
        ).build());
    }

    @GetMapping(Routes.PREVIEW_NEWS)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> previewNews(@PathVariable String id) throws MagicMatchNotFoundException, MagicException, MagicParseException {

        String fileBase64 = newsService.previewNews(id);

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
