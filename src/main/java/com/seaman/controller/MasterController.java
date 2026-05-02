package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.*;
import com.seaman.service.MasterDataService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class MasterController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final MasterDataService masterDataService;
    private final MessageCodeService messageCodeService;

    @GetMapping(Routes.MASTER)
    public ResponseEntity<SuccessResponse<MasterDataResponse>> master(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.list()
        ).build());
    }

    @GetMapping(Routes.MASTER_SCHOOLS)
    public ResponseEntity<SuccessResponse<MasterDataResponse>> masterSchools(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.listSchools()
        ).build());
    }

    @GetMapping(Routes.MASTER_COMPANYS)
    public ResponseEntity<SuccessResponse<MasterDataResponse>> masterCompanys(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.listCompanys()
        ).build());
    }

    @GetMapping(Routes.MASTER_COURSE_NAME)
    public ResponseEntity<SuccessResponse<MasterDataResponse>> masterCourse(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.getCourseName()
        ).build());
    }

    @GetMapping(Routes.MASTER_GROUP_NAME)
    public ResponseEntity<SuccessResponse<MasterDataResponse>> masterGroup(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.getGroupName()
        ).build());
    }

    @GetMapping(Routes.MASTER_MENU)
    public ResponseEntity<SuccessResponse<MasterDataResponse>> masterMenu(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.getMenuName()
        ).build());
    }

    @GetMapping(Routes.MASTER_DOCUMENTS)
    public ResponseEntity<SuccessResponse<MasterDataDocumentResponse>> masterDocumentsName(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.masterDataDocuments()
        ).build());
    }

    @GetMapping(Routes.MASTER_COMPANY)
    public ResponseEntity<SuccessResponse<MasterCompanyResponse>> masterCompany(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.masterCompany()
        ).build());
    }

}
