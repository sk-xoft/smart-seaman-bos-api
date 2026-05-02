package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.GroupRequest;
import com.seaman.model.request.GroupRoleRq;
import com.seaman.model.response.GroupRs;
import com.seaman.service.GroupService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class GroupController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final GroupService groupService;

    private final MessageCodeService messageCodeService;
    @PostMapping(Routes.LIST_GROUP)
    public ResponseEntity<SuccessResponse<GroupRs>> login(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody GroupRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                groupService.getAllGroup(request)
        ).build());
    }

    @GetMapping(Routes.LIST_GROUP_BY_ID)
    public ResponseEntity<SuccessResponse<GroupRs>> listCourseById(HttpServletRequest httpServletRequest, @PathVariable String id) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                groupService.getGroupById(id)
        ).build());
    }

    @PostMapping(Routes.CREATE_GROUP)
    public ResponseEntity<SuccessResponse<GroupRs>> createCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody GroupRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                groupService.createGroup(request)
        ).build());
    }

    @PostMapping(Routes.UPDATE_GROUP)
    public ResponseEntity<SuccessResponse<GroupRs>> updateCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody GroupRoleRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                groupService.updateGroup(request)
        ).build());
    }

    @PostMapping(Routes.DELETE_GROUP)
    public ResponseEntity<SuccessResponse<GroupRs>> deleteCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody GroupRequest request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                groupService.deleteGroup(request.getGroupId().toString())
        ).build());
    }

    @PostMapping(Routes.CREATE_GROUP_ROLE)
    public ResponseEntity<SuccessResponse<GroupRs>> createGroupRole(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody GroupRoleRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                groupService.createGroupRole(request)
        ).build());
    }
}
