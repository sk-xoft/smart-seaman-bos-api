package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.CompanyEntity;
import com.seaman.entity.UserMobileEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.DashboardRequest;
import com.seaman.model.request.MobileUserModel;
import com.seaman.model.request.UserMobileRequest;
import com.seaman.model.response.MobileUserRs;
import com.seaman.model.response.SmartManCodeResponse;
import com.seaman.model.response.UserMobileDashboardResponse;
import com.seaman.model.response.UserModelResponse;
import com.seaman.repository.CompanyRepository;
import com.seaman.repository.UserMobileRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMobileService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final HttpServletRequest httpServletRequest;
    private final TransactionLogsService transactionLogsService;
    private final UserMobileRepository userMobileRepository;
    private final CompanyRepository companyRepository;

    public UserModelResponse listUserMobile(UserMobileRequest request) {
        UserModelResponse response = new UserModelResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET ALL USER MOBILE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());
            Integer startNum = request.getLastNum() - request.getSize();

            if (startNum < 0){
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL," {size} can not more than {lastNum}.");
            }

            // Get company
            String companyCode = null;
            CompanyEntity companyEntity =  companyRepository.getCompanyCode(usersEntity.getUsername());
            if(null  != companyEntity && "Shipping Company".equals(companyEntity.getCompanyType())){
                companyCode = usersEntity.getCompanyCode();
            }

            boolean isSelectUserMobileNull = false;

            if("Training School".equals(companyEntity.getCompanyType()) || "App Owner".equals(companyEntity.getCompanyType())){
                isSelectUserMobileNull = true;
            }

            if(null == request.getFirstName()) {
                request.setFirstName("");
            }

            if(null == request.getLastName()){
                request.setLastName("");
            }


            List<UserMobileEntity> userMobileList = userMobileRepository.findUserMobileAll(startNum, request.getSize(), request, companyCode, isSelectUserMobileNull);
            List<UserMobileEntity> userMobileListCount = userMobileRepository.findUserMobileAllCount(startNum, request.getSize(), request, companyCode, isSelectUserMobileNull);

            Integer num = 0;
            List<UserMobileEntity> userListResult = new ArrayList<>();

            for(UserMobileEntity item :userMobileList){
                ++num;
                item.setRowId(String.valueOf(startNum + num));
                userListResult.add(item);
            }

            Integer totalData = userMobileList.size();
            response.setSize(request.getSize());
            response.setLastNum(request.getLastNum());
            response.setTotalData(totalData);
            response.setUsers(userListResult);
            response.setCountList(userMobileListCount.size());

            log.info("Get all user mobile list.");

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get user mobile -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public MobileUserRs getCountAllUser() {
        MobileUserRs response = new MobileUserRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            String companyType = companyRepository.getCompanyType(username);
            CompanyEntity com = companyRepository.getCompanyCode(username);

            if(companyType.equals("Shipping Company")){ //sumary by company_code
                Integer count = userMobileRepository.countAllUserByCompanyCode(com.getCompanyCode());
                response.setCount(count);
            }else{ //for all
                Integer count = userMobileRepository.countAllUser();
                response.setCount(count);
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Document create Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());

        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, "count all user");
        }

        return response;
    }

    public MobileUserRs getLessThanThree() {
        MobileUserRs response = new MobileUserRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String username ="";
        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            String companyType = companyRepository.getCompanyType(username);
            CompanyEntity com = companyRepository.getCompanyCode(username);

            if(companyType.equals("Shipping Company")){ ///sumary by company_code
                Long count = userMobileRepository.getLessThanThreeMonthByCompanyCode(com.getCompanyCode());
                response.setCount(count.intValue());
            }else{ //for all
                Long count = userMobileRepository.getLessThanThreeMonth();
                response.setCount(count.intValue());
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get less than three month Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());

        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, "get less than three month");
        }

        return response;
    }

    public MobileUserRs getLessThanSix() {
        MobileUserRs response = new MobileUserRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String username="";
        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();
            CompanyEntity com = new CompanyEntity();

            String companyType = companyRepository.getCompanyType(username);
            com = companyRepository.getCompanyCode(username);


            if(companyType.equals("Shipping Company")){ ///sumary by company_code
                Long count = userMobileRepository.getLessThanSixMonthByCompanyCode(com.getCompanyCode());
                response.setCount(count.intValue());
            }else{ //for all
                Long count = userMobileRepository.getLessThanSixMonth();
                response.setCount(count.intValue());
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get less than six month Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());

        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, "get less than six month");
        }

        return response;
    }

    public MobileUserRs getLessThanYear() {
        MobileUserRs response = new MobileUserRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String username="";
        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();
            CompanyEntity com = new CompanyEntity();

            String companyType = companyRepository.getCompanyType(username);
            com = companyRepository.getCompanyCode(username);


            if(companyType.equals("Shipping Company")){ ///sumary by company_code

                Long count = userMobileRepository.getLessThanYearByCompanyCode(com.getCompanyCode());
                response.setCount(count.intValue());
            }else{ //for all

                Long count = userMobileRepository.getLessThanYear();
                response.setCount(count.intValue());
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get less than year Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());

        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, "Get less than year");
        }

        return response;
    }

    public UserMobileDashboardResponse listUserMobileSummary(String month, DashboardRequest request) {
        UserMobileDashboardResponse response = new UserMobileDashboardResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "List User Mobile";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            Integer startNum = request.getLastNum() - request.getSize();
            Integer totalRecord = 0;

            if (startNum < 0) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, " {size} can not more than {lastNum}.");
            }

            String companyType = companyRepository.getCompanyType(username);
            CompanyEntity com = companyRepository.getCompanyCode(username);

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());
            List<MobileUserModel> rsList = new ArrayList<>();

            if(companyType.equals("Shipping Company")){
                // summary by company_code
                rsList = userMobileRepository.listMobileUser(month,com.getCompanyCode(), startNum, request.getSize());
                totalRecord = userMobileRepository.mobileUserCount(month, com.getCompanyCode(), startNum, request.getSize());
            }else{ //for all
                rsList = userMobileRepository.listMobileUser(month,null, startNum, request.getSize());
                totalRecord = userMobileRepository.mobileUserCount(month, null, startNum, request.getSize());
            }

            Integer num = 0;

            List<MobileUserModel> userMobileList = new ArrayList<>();
            for(MobileUserModel item :rsList){

                MobileUserModel rs = new MobileUserModel();

                num++;
                rs.setNum(startNum + num);
                rs.setCertMobileUuid(item.getMobileUuid());
                rs.setDays(item.getDays());
                rs.setCertId(item.getCertId());
                rs.setCertEndDate(item.getCertEndDate());
                rs.setCount(item.getCount());
                rs.setCertStartDate(item.getCertStartDate());
                rs.setLastName(item.getLastName());
                rs.setFirstName(item.getFirstName());
                rs.setCompanyNameEn(item.getCompanyNameEn());
                rs.setDocumentNameTh(item.getDocumentNameTh());
                rs.setSmartSeamanId(item.getSmartSeamanId());
                rs.setMobileNumber(item.getMobileNumber());
                rs.setEmail(item.getEmail());
                rs.setPositionNameEn(item.getPositionNameEn());
                userMobileList.add(rs);
            }

            Integer totalData = totalRecord;
            response.setSize(request.getSize());
            response.setLastNum(request.getLastNum());
            response.setTotalData(totalData);
            response.setItems(userMobileList);

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

    public List<SmartManCodeResponse> searchSmartSeamanCode(String search) {
        List<SmartManCodeResponse> response = new ArrayList<>();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "List User Mobile Smartseaman code.";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());
            List<MobileUserRs> rsList = userMobileRepository.listSmartSeamanCode(search);

            for(MobileUserRs item :rsList){
                SmartManCodeResponse model =  new SmartManCodeResponse();
                model.setSmartseamanId(item.getSmartSeamanId());
                model.setFullName(item.getFirstName() + " " + item.getLastName());
                response.add(model);
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get all smartseaman id/code Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

}
