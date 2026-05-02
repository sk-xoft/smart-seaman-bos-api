package com.seaman.controller;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.CourseRq;
import com.seaman.model.response.CourseRs;
import com.seaman.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class CourseController extends BaseController {

    private final CourseService courseService;
    private final MessageCodeService messageCodeService;

    @PostMapping(Routes.ALLCOURSE)
    public ResponseEntity<SuccessResponse<CourseRs>> login(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CourseRq courseRq) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE,
                (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                courseService.getAllCourse(courseRq)
        ).build());
    }

    @PostMapping(Routes.CREATE_COURSE)
    public ResponseEntity<SuccessResponse<CourseRs>> createCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CourseRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                courseService.createCourse(request)
        ).build());
    }

    @PostMapping(Routes.DELETE_COURSE)
    public ResponseEntity<SuccessResponse<CourseRs>> deleteCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CourseRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                courseService.deleteCourse(request.getCourseId())
        ).build());
    }
    @PostMapping(Routes.UPDATE_COURSE)
    public ResponseEntity<SuccessResponse<CourseRs>> updateCourse(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CourseRq request) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                courseService.updateCourse(request)
        ).build());
    }

    @GetMapping(Routes.LIST_COURSE_BY_ID)
    public ResponseEntity<SuccessResponse<CourseRs>> listCourseById(HttpServletRequest httpServletRequest, @PathVariable String id) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                courseService.getCourseById(id)
        ).build());
    }

}
