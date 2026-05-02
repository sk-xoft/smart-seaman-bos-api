package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.google.gson.Gson;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.AdminUserEntity;
import com.seaman.entity.NewsEntity;
import com.seaman.entity.SessionEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.common.ErrorMessage;
import com.seaman.model.request.AdminRequest;
import com.seaman.model.request.UpdatePasswordRequest;
import com.seaman.model.response.AdminResponse;
import com.seaman.model.response.UpdatePasswordResponse;
import com.seaman.repository.AdminRepository;
import com.seaman.repository.SessionRepository;
import com.seaman.repository.UserRepository;
import com.seaman.utils.FrameworkUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest httpServletRequest;

    private final JwtTokenService jwtTokenUtil;

    private final FrameworkUtils frameworkUtils;

    private final AdminRepository adminRepository;

    private final SessionRepository sessionRepository;

    private final TransactionLogsService transactionLogsService;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final AmazonS3 getS3;
    @Value("${object.store.bucket}")
    private String bucketName;
    @Value("${object.store.path.profile.picture}")
    private String pathUploadProfilePicture;

    @Deprecated
    public AdminResponse listById(AdminRequest adminRequest) {
        AdminResponse response = new AdminResponse();
        String correlationId = httpServletRequest.getHeader(AppSys.HEADER_CORRELATION_ID);

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // -- 1. find username in table, And check password
            Optional<AdminUserEntity> adminEntityOptional = Optional.ofNullable(adminRepository.findById(adminRequest.getAdminUserId()));


            // -- 2. Generate JWT
            // Ref : https://www.rfc-editor.org/rfc/rfc7519#section-4.1
            String sessionId = frameworkUtils.generateUUID();
            String clientSessionId = frameworkUtils.generateUUID();
            Map<String, Object> claims = new HashMap<>();
            claims.put(AppSys.CLAIMS_ISSUER, AppSys.APPLICATION_NAME);
            claims.put(AppSys.CLAIMS_JTI, clientSessionId);
            claims.put(AppSys.CLAIMS_SUBJECT, "TEST");

            // Create JWT TOKEN
            String jwtToken = jwtTokenUtil.generateToken(claims, "TEST");

            // Store table session
            SessionEntity sessionEntity = new SessionEntity();
            sessionEntity.setClientSessionId(clientSessionId);
            sessionEntity.setToken(jwtToken);
            sessionEntity.setDeviceModel("WEB_ADMIN"); // must use form header request.
            sessionEntity.setUserId("1");
            sessionEntity.setCreateBy("Test");
            sessionEntity.setLoginTime(new Date());
            sessionEntity.setCreateDate(new Date());
            sessionEntity.setUpdateBy("test");
            sessionEntity.setUpdateDate(new Date());
            sessionEntity.setCorrelationId(correlationId);
            sessionRepository.insert(sessionEntity);


        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;

    }

    public AdminResponse getAllAdmin(AdminRequest request) {

        AdminResponse response = new AdminResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET LIST USER ADMIN";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            Integer startNum = request.getLastNum() - request.getSize();

            if (startNum < 0) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, " {size} can not more than {lastNum}.");
            }

            List<AdminUserEntity> adminLists = adminRepository.listAllAdmin(startNum, request.getSize(), request);
            List<AdminUserEntity> adminListsCount = adminRepository.listAllAdminCount(startNum, request.getSize(), request);
            Integer num = 0;
            // Company
            String keyName,base64="";
            List<AdminUserEntity> rsLists = new ArrayList<>();
            for (AdminUserEntity item : adminLists) {

                AdminUserEntity rs = new AdminUserEntity();
                ++num;
                rs.setAdminNum(startNum+num);
                rs.setAdminUserId(item.getAdminUserId());
                rs.setUsername(item.getUsername());
                rs.setGroupId(item.getGroupId());
                rs.setGroupName(item.getGroupName());
                rs.setUserStatus(item.getUserStatus());
                rs.setFirstName(item.getFirstName());
                rs.setLastName(item.getLastName());
                rs.setAdminRoleName(item.getAdminRoleName());
                rs.setEmail(item.getEmail());
                rs.setAdminCompany(item.getAdminCompany());
                rs.setAdminStatus(item.getAdminStatus());
                rs.setLastLogon(item.getLastLogon());
                rs.setCreateDate(item.getCreateDate());
                rs.setCreateBy(item.getCreateBy());
                rs.setUpdateDate(item.getUpdateDate());
                rs.setUpdateBy(item.getUpdateBy());
                rs.setProfilePicture(item.getProfilePicture());
                keyName = pathUploadProfilePicture +"/" + item.getAdminUuid() + "/" + item.getProfilePicture();
                Boolean existBool;
                if (item.getProfilePicture()!=null){
                    existBool=getS3.doesObjectExist(bucketName, keyName);
                    if(existBool){
                        base64 = getS3.getObjectAsString(bucketName, keyName);
                        rs.setPictureFromFile(base64);
                    }else{
                        base64 = getS3.getObjectAsString(bucketName, pathUploadProfilePicture + "/" + "default_pic");
                        rs.setPictureFromFile(base64);
                        rs.setProfilePicture("default_pic");
                    }
                }else{
                    base64 = getS3.getObjectAsString(bucketName, pathUploadProfilePicture + "/" + "default_pic");
                    rs.setPictureFromFile(base64);
                    rs.setProfilePicture("default_pic");
                }

                rsLists.add(rs);
            }
           Integer totalData = adminRepository.getTotalData();

            response.setSize(request.getSize());
            response.setLastNum(request.getLastNum());
            response.setTotalData(totalData);
            response.setAdminUserList(rsLists);
            response.setCountList(adminListsCount.size());
            log.info("{}", "Process list-menu is success.");

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get all user admin exception {}", ex.getMessage());
            log.error("{}",ex);
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());

        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public AdminResponse getAdminById(String adminUserId) {

        AdminResponse response = new AdminResponse();
        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET AMIN BY ID";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());
            String keyName,base64="";

            AdminUserEntity rs = new AdminUserEntity();
            List<AdminUserEntity> rsList = new ArrayList<>();
            rs = adminRepository.listAdminById(adminUserId);
            rs.setAdminNum(1);
            ////-------------- for get Profile Picture ----------////
            keyName = pathUploadProfilePicture + "/" + rs.getAdminUuid() +  "/" + rs.getProfilePicture();
            Boolean existBool;
            if (rs.getProfilePicture()!=null || !rs.getProfilePicture().equals("") ){
                existBool=getS3.doesObjectExist(bucketName, keyName);
                if(existBool){
                    base64 = getS3.getObjectAsString(bucketName, keyName);
                    rs.setPictureFromFile(base64);
                }else{
                    base64 = getS3.getObjectAsString(bucketName, pathUploadProfilePicture + "/" + "default_pic");
                    rs.setPictureFromFile(base64);
                }
            }else{
                base64 = getS3.getObjectAsString(bucketName, pathUploadProfilePicture + "/" + "default_pic");
                rs.setPictureFromFile(base64);
            }
            rsList.add(rs);
            response.setSize(1);
            response.setLastNum(1);
            response.setTotalData(1);
            response.setAdminUserList(rsList);

            log.info("Get Admin by Id is success.");

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get admin by id -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());

        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public AdminResponse insertAdmin(AdminRequest request) {
        AdminResponse response = new AdminResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "ADMIN_CREATE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // Insert table
            AdminUserEntity entity = new AdminUserEntity();
            entity.setAdminUuid(frameworkUtils.generateUUID());
            entity.setGroupId(request.getGroupId());
            entity.setUsername(request.getUsername());
            entity.setPassword(passwordEncoder.encode(request.getPassword()));
            entity.setFirstName(request.getFirstName());
            entity.setLastName(request.getLastName());
            entity.setCompanyCode(request.getCompanyCode());
            entity.setPositions(request.getPositions());
            entity.setEmail(request.getEmail());
            entity.setMobileNumber(request.getMobileNumber());
            entity.setDisplayType(request.getDisplayType());
            entity.setDisplayName(request.getDisplayName());
            entity.setProfilePicture(request.getProfilePicture());
            entity.setUserStatus(request.getUserStatus());
            entity.setCreateDate(new Date());
            entity.setCreateBy(username);
            log.info("entity before insert : {} ", entity);

            if (adminRepository.listAdminByUsername(request.getUsername())) {
                throw new BusinessException(AppStatus.USERNAME_IS_EXISTING, "");
            }

            if (adminRepository.insertAdmin(entity)) {
                if (!"".equals(request.getPictureFromFile()) || null != request.getPictureFromFile()) {
                    StringBuilder keyName = new StringBuilder();
                    keyName.append(pathUploadProfilePicture);
                    keyName.append("/");
                    keyName.append(entity.getAdminUuid());
                    keyName.append("/");
                    keyName.append(request.getProfilePicture());

                    getS3.putObject(bucketName, keyName.toString(), request.getPictureFromFile());
                    log.info("put object {} is success.", keyName);
                } else {
                    log.info("Not have send file 'Profile Picture'.");
                }
                response.setAdminUserId(adminRepository.getMaxId());
            }

            log.info("Create admin is success.");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Create user admin Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }

    public String deleteAdmin(Integer formId) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "DELETE_ADMIN";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            if (adminRepository.listAdminById(formId.toString()) != null) {
                log.info("Delete admin is success.");
                adminRepository.deleteAdmin(formId.toString());
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Delete admin -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());

        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";
    }

    public String updateAdmin(AdminRequest request) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "UPDATE_ADMIN";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            if (adminRepository.listAdminByUsername(request.getUsername())) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, " Cannot be use '" + request.getUsername() + "' because Username already exist. ");
            }

            // Get user admin details
            AdminUserEntity entity = adminRepository.listAdminById(String.valueOf(request.getAdminUserId()));

            if (adminRepository.updateAdmin(request, username)) {
                if(null != request.getPictureFromFile()){
                    if (!"".equals(request.getPictureFromFile()) ) {

                        StringBuilder keyName = new StringBuilder();
                        keyName.append(pathUploadProfilePicture);
                        keyName.append("/");
                        keyName.append(entity.getAdminUuid());
                        keyName.append("/");
                        keyName.append(request.getProfilePicture());

                        getS3.putObject(bucketName, keyName.toString(), request.getPictureFromFile());

                        log.info("put object {} is success.", keyName);
                    } else {
                        log.info("Not have send file 'Profile Picture'.");
                    }
                }else{
                    log.info("Not have json tag  'Profile Picture'.");
                }
            }
            log.info("Update admin is success.");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Update user admin has Exception {}", ex.getMessage());
            log.error("{}", ex);
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());

        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";
    }

    public String getProfilePic() {
        String imageBase64 = "";

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "PREVIEW  PROFILE PIC";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            StringBuilder keyName = new StringBuilder();
            keyName.append(pathUploadProfilePicture);
            keyName.append("/");
            keyName.append(usersEntity.getAdminUuid());
            keyName.append("/");
            keyName.append(usersEntity.getProfilePicture());

            boolean existBool = getS3.doesObjectExist(bucketName, keyName.toString());

            if(existBool){
                imageBase64 = getS3.getObjectAsString(bucketName, keyName.toString());
            }else{
                imageBase64 = getS3.getObjectAsString(bucketName, pathUploadProfilePicture + "/" + "default_pic");
            }

            log.info("Load profile pic is success.");

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw ex;
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return imageBase64;
    }

    public Object updatePassword(UpdatePasswordRequest request){

        UpdatePasswordResponse response = new UpdatePasswordResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "UPDATE PASSWORD";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // -- 1. find username in table, And check password
            Optional<UsersEntity> usersEntityOptional = Optional.ofNullable(userRepository.findByUsername(request.getUsername()));

            if (usersEntityOptional.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
            }

            // Check password is match.
            if(request.getConfirmPassword().equals(request.getNewPassword())) {

                // Find user check password.
                UsersEntity usersEntityDB = usersEntityOptional.get();
                if (!this.matchPassword(request.getOldPassword(), usersEntityDB.getPassword())) {
                    throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
                }

                usersEntityDB.setPassword(passwordEncoder.encode(request.getConfirmPassword()));
                userRepository.changePassword(usersEntityDB);

                // Set Response
                response.setUsername(usersEntityDB.getUsername());

            } else {
                throw new BusinessException(AppStatus.PASSWORD_IS_MATCH, "");
            }

            /** Case update is successfully but fore logout */
            if(username.equals(request.getUsername())){
                throw new BusinessException(AppStatus.CHANG_PASSWORD_SUCCESS,"");
            }

            log.info("Update password user admin is success.");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("{} Exception {}", serviceName, ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());

        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    private boolean matchPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}
