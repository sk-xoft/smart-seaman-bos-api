package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.CourseEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.CourseRq;
import com.seaman.model.response.*;
import com.seaman.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest httpServletRequest;

    private final CourseRepository courseRepository;

    private  final TransactionLogsService transactionLogsService;

    public CourseRs getAllCourse(CourseRq courseRequest) {
        CourseRs response = new CourseRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET ALL COURSE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            Integer lastNum = courseRequest.getLastNum() - courseRequest.getSize();

            if (lastNum < 0){
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL," {size} can not more than {lastNum}.");
            }
            List<CourseList> courseList = courseRepository.listAllCourse(lastNum,courseRequest.getSize(),courseRequest.getCourseCode(),courseRequest.getCourseSchoolCode());
            List<CourseList> courseListCount = courseRepository.listAllCourseCount(lastNum,courseRequest.getSize(),courseRequest.getCourseCode(),courseRequest.getCourseSchoolCode());
           // log.info("last num : {}. ", lastNum );

            Integer num = 0;

            List<CourseList> courseLists = new ArrayList<>();
            for(CourseList item :courseList){
                ++num;
                CourseList course = new CourseList();
                course.setCourseNum(lastNum + num);
                course.setCourseColour(item.getCourseColour());
                course.setCourseName(item.getCourseName());
                course.setCourseOnlineDate(item.getCourseOnlineDate());
                course.setCourseOnsiteDate(item.getCourseOnsiteDate());
                course.setCourseCode(item.getCourseCode());
                course.setCoursePrice(item.getCoursePrice());
                course.setCourseSchoolName(item.getCourseSchoolName());
                course.setCourseSchoolCode(item.getCourseSchoolCode());
                course.setCourseStatus(item.getCourseStatus());
                course.setCourseTotalDays(item.getCourseTotalDays());
                course.setCourseType(item.getCourseType());
                course.setCourseId(item.getCourseId());
                course.setCourseNameEn(item.getCourseNameEn());
                course.setCourseNameTh(item.getCourseNameTh());
                courseLists.add(course);
            }

            Integer totalData = courseRepository.getTotalData();
            response.setSize(courseRequest.getSize());
            response.setLastNum(courseRequest.getLastNum());
            response.setTotalData(totalData);
            response.setCourseList(courseLists);
            response.setCountList(courseListCount.size());

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get all course Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public CourseRs getCourseById(String courseId) {
        CourseRs response = new CourseRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET COURSE BY ID";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            CourseList course = new CourseList();
            List<CourseList> courseLists = new ArrayList<>();
            course = courseRepository.listCourseById(courseId);
            course.setCourseNum(1);
            courseLists.add(course);
            response.setSize(1);
            response.setLastNum(1);
            response.setTotalData(1);
            response.setCourseList(courseLists);

            log.info("Get course by id. Is success");

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get course by id -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public CourseRs createCourse(CourseRq request) {

        CourseRs response = new CourseRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "CREATE COURSE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // Insert table
            CourseEntity entity  = new CourseEntity();
            entity.setCourseDocumentCode(request.getCourseCode());
            entity.setCourseCompanyCode(request.getCourseSchoolCode());
            entity.setCourseType(request.getCourseType());
            entity.setCourseOnlineDate(request.getCourseOnlineDate());
            entity.setCourseOnsiteDate(request.getCourseOnsiteDate());
            entity.setCourseTotalDays(request.getCourseTotalDays());
            entity.setCourseColour(request.getCourseColour());
            entity.setCoursePrise(request.getCoursePrice());
            entity.setCourseStatus(request.getCourseStatus());
            entity.setCreateDate(new Date());
            entity.setCreateBy(username);
            if(courseRepository.insert(entity)) {
                response.setCourseId(courseRepository.getMaxId().toString());
            }

            log.info("Insert course is success.");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Create course Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public String deleteCourse(String courseId) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "DELETE_COURSE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            if(courseRepository.listCourseById(courseId)!=null){
                courseRepository.delete(courseId);
            }

            log.info("Delete course is success.");

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Delete course Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";
    }

    public String updateCourse(CourseRq request) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "UPDATE_COURSE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            courseRepository.update(request,username);

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Update course -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";
    }

}
