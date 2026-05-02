package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.LoginRequest;
import com.seaman.model.request.RegisterRequest;
import com.seaman.model.response.LoginResponse;
import com.seaman.model.response.RefreshTokenResponse;
import com.seaman.model.response.RegisterResponse;
import com.seaman.model.response.UserAdminProfileResponse;
import com.seaman.service.AuthService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuthService authService;

    private final MessageCodeService messageCodeService;

    @PostMapping(Routes.LOGIN)
    public ResponseEntity<SuccessResponse<LoginResponse>> login(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody LoginRequest loginRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.login(loginRequest)
        ).build());
    }

    @PostMapping(Routes.REGISTER)
    public ResponseEntity<SuccessResponse<RegisterResponse>> register(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody RegisterRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.register(request)
        ).build());
    }

    @GetMapping(Routes.PROFILE)
    public ResponseEntity<SuccessResponse<UserAdminProfileResponse>> profile(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                authService.getUserAdminProfile()
        ).build());
    }


//    @GetMapping(Routes.REFRESH_TOKEN)
//    public ResponseEntity<SuccessResponse<RefreshTokenResponse>> refreshToken(HttpServletRequest httpServletRequest) {
//
//        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
//
//        return ok(SuccessResponse.builder(
//                AppStatus.SUCCESS_CODE,
//                description,
//                authService.refreshToken()
//        ).build());
//    }

//    @PostMapping(Routes.CERTIFICATE)
//    public ResponseEntity<SuccessResponse<LoginResponse>> login(
//            HttpServletRequest httpServletRequest,
//            @Valid @RequestBody LoginRequest loginRequest) {
//
//        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
//
//        return ok(SuccessResponse.builder(
//                AppStatus.SUCCESS_CODE,
//                description,
//                authService.login(loginRequest)
//        ).build());
//    }
}
