package com.seaman.controller;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.MenuAuthorizedRequest;
import com.seaman.model.request.MenuRequest;
import com.seaman.model.response.MenuAuthorizedResponse;
import com.seaman.model.response.MenuResponse;
import com.seaman.service.*;
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
public class MenuController extends BaseController {

    private final MenuService menuService;
    private final MessageCodeService messageCodeService;

    @PostMapping(Routes.MENU)
    public ResponseEntity<SuccessResponse<MenuResponse>> login(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody MenuRequest menuRequest) {


        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                menuService.menuList(menuRequest)
        ).build());
    }

    @PostMapping(Routes.AUTHORISED)
    public ResponseEntity<SuccessResponse<MenuAuthorizedResponse>> getAuthorised(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody MenuAuthorizedRequest menuAuthorizedRequest) {


        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                menuService.getAuthorised(menuAuthorizedRequest)
        ).build());
    }



}
