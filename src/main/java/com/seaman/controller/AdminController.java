package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.AdminRequest;
import com.seaman.model.request.UpdatePasswordRequest;
import com.seaman.model.response.*;
import com.seaman.service.AdminService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Base64;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class AdminController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AdminService adminService;

    private final MessageCodeService messageCodeService;

    @PostMapping(Routes.ADMIN)
    public ResponseEntity<SuccessResponse<AdminResponse>> login(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody AdminRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                adminService.getAllAdmin(request)
        ).build());
    }

    @GetMapping(Routes.LIST_ADMIN_BY_ID)
    public ResponseEntity<SuccessResponse<AdminResponse>> listCourseById(HttpServletRequest httpServletRequest, @PathVariable String id) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                adminService.getAdminById(id)
        ).build());
    }

    @PostMapping(Routes.CREATE_ADMIN)
    public ResponseEntity<SuccessResponse<AdminResponse>> createCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody AdminRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                adminService.insertAdmin(request)
        ).build());
    }

    @PostMapping(Routes.UPDATE_ADMIN)
    public ResponseEntity<SuccessResponse<AdminResponse>> updateCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody AdminRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                adminService.updateAdmin(request)
        ).build());
    }

    @PostMapping(Routes.DELETE_ADMIN)
    public ResponseEntity<SuccessResponse<AdminResponse>> deleteCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody AdminRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                adminService.deleteAdmin(request.getAdminUserId())
        ).build());
    }

    @GetMapping(Routes.PROFILE_PICTURE)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> profilePicture() {

        String fileBase64 = adminService.getProfilePic();

        byte[] imageBytes = Base64.getDecoder().decode(fileBase64);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @PostMapping(Routes.UPDATE_PASSWORD)
    public ResponseEntity<SuccessResponse<UpdatePasswordResponse>> deleteCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody UpdatePasswordRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                adminService.updatePassword(request)
        ).build());
    }

}
