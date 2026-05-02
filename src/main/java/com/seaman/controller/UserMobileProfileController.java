package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.DashboardRequest;
import com.seaman.model.response.MobileUserRs;
import com.seaman.model.response.ProfileResponse;
import com.seaman.model.response.SmartManCodeResponse;
import com.seaman.service.MessageCodeService;
import com.seaman.service.ProfileService;
import com.seaman.service.UserMobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class UserMobileProfileController extends BaseController {

    private final MessageCodeService messageCodeService;
    private final ProfileService profileService;
    private final UserMobileService userMobileService;

    @GetMapping(Routes.USER_MOBILE_PROFILE)
    public ResponseEntity<SuccessResponse<ProfileResponse>> profile(HttpServletRequest httpServletRequest, @RequestParam("mobileUUID") String mobileUUID) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                profileService.getProfile(mobileUUID)
        ).build());
    }
    @GetMapping(Routes.USER_MOBILE_PROFILE_PIC)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> getImage(@RequestParam("mobileUUID") String mobileUUID) {

        String fileBase64 = profileService.getProfileImage(mobileUUID);

        byte[] imageBytes = Base64.getDecoder().decode(fileBase64);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
    
    @GetMapping(Routes.ALL_USER)
    public ResponseEntity<SuccessResponse<MobileUserRs>> countAllUser(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,userMobileService.getCountAllUser()
        ).build());
    }

    @GetMapping(Routes.LESS_THAN_THREE)
    public ResponseEntity<SuccessResponse<MobileUserRs>> getLessThanThree(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,userMobileService.getLessThanThree()
        ).build());
    }

    @GetMapping(Routes.LESS_THAN_SIX)
    public ResponseEntity<SuccessResponse<MobileUserRs>> getLessThanSix(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,userMobileService.getLessThanSix()
        ).build());
    }

    @GetMapping(Routes.LESS_THAN_YEAR)
    public ResponseEntity<SuccessResponse<MobileUserRs>> getLessThanYear(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,userMobileService.getLessThanYear()
        ).build());
    }

    @PostMapping(Routes.LIST_USER_MOBILE)
    public ResponseEntity<SuccessResponse<MobileUserRs>> listCourseById(HttpServletRequest httpServletRequest, @PathVariable String id,  @RequestBody DashboardRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                userMobileService.listUserMobileSummary(id, request)
        ).build());
    }

    @GetMapping(Routes.ALL_SMART_CODE)
    public ResponseEntity<SuccessResponse<SmartManCodeResponse>> listSmartSeaman(HttpServletRequest httpServletRequest, @RequestParam(value = "search", required = false) String search) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                userMobileService.searchSmartSeamanCode(search)
        ).build());
    }
}